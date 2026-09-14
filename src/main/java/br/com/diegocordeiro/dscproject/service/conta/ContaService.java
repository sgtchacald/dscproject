package br.com.diegocordeiro.dscproject.service.conta;

import br.com.diegocordeiro.dscproject.dto.conta.ContaAjusteSaldoDTO;
import br.com.diegocordeiro.dscproject.dto.conta.ContaEdicaoDTO;
import br.com.diegocordeiro.dscproject.dto.conta.ContaFormDTO;
import br.com.diegocordeiro.dscproject.dto.conta.ContaGridDTO;
import br.com.diegocordeiro.dscproject.dto.conta.ContaOpcaoDTO;
import br.com.diegocordeiro.dscproject.model.conta.Conta;
import br.com.diegocordeiro.dscproject.model.instituicaofinanceira.InstituicaoFinanceira;
import br.com.diegocordeiro.dscproject.model.usuario.Usuario;
import br.com.diegocordeiro.dscproject.config.parametro.ParametrosContaCatalogo;
import br.com.diegocordeiro.dscproject.repository.conta.ContaRepository;
import br.com.diegocordeiro.dscproject.repository.instituicaofinanceira.InstituicaoFinanceiraRepository;
import br.com.diegocordeiro.dscproject.repository.parametro.ParametroGlobalRepository;
import br.com.diegocordeiro.dscproject.repository.usuario.UsuarioRepository;
import br.com.diegocordeiro.dscproject.service.exceptions.RegistroNaoEncontradoException;
import br.com.diegocordeiro.dscproject.service.exceptions.RegraNegocioException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ContaService {

    private final ContaRepository contaRepository;
    private final InstituicaoFinanceiraRepository instituicaoFinanceiraRepository;
    private final UsuarioRepository usuarioRepository;
    private final ParametroGlobalRepository parametroGlobalRepository;
    private final JdbcTemplate jdbcTemplate;

    private final Map<Long, List<ContaOpcaoDTO>> cacheOpcoes = new ConcurrentHashMap<>();

    public ContaService(ContaRepository contaRepository, InstituicaoFinanceiraRepository instituicaoFinanceiraRepository, UsuarioRepository usuarioRepository, ParametroGlobalRepository parametroGlobalRepository, JdbcTemplate jdbcTemplate) {
        this.contaRepository = contaRepository;
        this.instituicaoFinanceiraRepository = instituicaoFinanceiraRepository;
        this.usuarioRepository = usuarioRepository;
        this.parametroGlobalRepository = parametroGlobalRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional(readOnly = true)
    public List<ContaGridDTO> listarParaGrid(Long usuarioId) {
        List<Conta> contas = contaRepository.listarPorUsuario(usuarioId);
        return contas.stream()
            .map(c -> ContaGridDTO.builder()
                .id(c.getId())
                .descricao(c.getDescricao())
                .tipo(c.getTipo())
                .instituicaoId(c.getInstituicao().getId())
                .instituicaoNome(c.getInstituicao().getNome())
                .agencia(c.getAgencia())
                .numero(c.getNumero())
                .moeda(c.getMoeda())
                .saldo(c.getSaldo())
                .saldoSincronizadoEm(c.getSaldoSincronizadoEm())
                .consideraSaldo(c.isConsideraSaldo())
                .qtdUso(contarUso(c.getId()))
                .ativo(c.isAtivo())
                .excluido(c.isExcluido())
                .build())
            .toList();
    }

    @Transactional(readOnly = true)
    public Conta buscarPorIdEUsuario(Long id, Long usuarioId) {
        return contaRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(id, usuarioId)
            .orElseThrow(() -> new RegistroNaoEncontradoException("msg.conta.nao-encontrada"));
    }

    @Transactional(readOnly = true)
    public ContaEdicaoDTO buscarParaEdicao(Long id, Long usuarioId) {
        Conta c = buscarPorIdEUsuario(id, usuarioId);
        return ContaEdicaoDTO.builder()
            .id(c.getId())
            .descricao(c.getDescricao())
            .tipo(c.getTipo())
            .instituicaoId(c.getInstituicao().getId())
            .instituicaoNome(c.getInstituicao().getNome())
            .agencia(c.getAgencia())
            .numero(c.getNumero())
            .moeda(c.getMoeda())
            .saldo(c.getSaldo())
            .nomeGerente(c.getNomeGerente())
            .telGerente(c.getTelGerente())
            .consideraSaldo(c.isConsideraSaldo())
            .ativo(c.isAtivo())
            .qtdUso(contarUso(c.getId()))
            .build();
    }

    @Transactional
    public Conta inserir(ContaFormDTO dto, Long usuarioId, String usuarioAuditoria) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
            .orElseThrow(() -> new RegistroNaoEncontradoException("msg.usuario.nao-encontrado"));

        String descricao = normalizarTexto(dto.getDescricao());
        if (contaRepository.contarPorUsuarioEDescricao(usuarioId, descricao, null) > 0) {
            throw new RegraNegocioException("descricao", "msg.conta.descricao.duplicada");
        }

        if (dto.getInstituicaoId() == null) {
            throw new RegraNegocioException("instituicaoId", "conta.validacao.instituicao.obrigatoria");
        }

        InstituicaoFinanceira instituicao = instituicaoFinanceiraRepository.findByIdAndDataExclusaoIsNull(dto.getInstituicaoId())
            .orElseThrow(() -> new RegraNegocioException("instituicaoId", "msg.conta.instituicao.invalida"));

        if (!instituicao.isAtivo()) {
            throw new RegraNegocioException("instituicaoId", "msg.conta.instituicao.invalida");
        }

        Conta entity = new Conta();
        entity.setDescricao(descricao);
        entity.setTipo(dto.getTipo());
        entity.setInstituicao(instituicao);
        entity.setAgencia(normalizarTexto(dto.getAgencia()));
        entity.setNumero(normalizarTexto(dto.getNumero()));
        entity.setMoeda(dto.getMoeda() != null && !dto.getMoeda().isBlank() ? dto.getMoeda().trim().toUpperCase() : "BRL");
        entity.setSaldo(dto.getSaldoInicial() != null ? dto.getSaldoInicial() : BigDecimal.ZERO);
        entity.setSaldoSincronizadoEm(null);
        entity.setNomeGerente(normalizarTexto(dto.getNomeGerente()));
        entity.setTelGerente(normalizarTexto(dto.getTelGerente()));
        entity.setConsideraSaldo(dto.isConsideraSaldo());
        entity.setAtivo(true);
        entity.setUsuario(usuario);

        Conta salva = contaRepository.save(entity);
        invalidarCache(usuarioId);
        return salva;
    }

    @Transactional
    public Conta editar(Long id, ContaFormDTO dto, Long usuarioId, String usuarioAuditoria) {
        Conta conta = buscarPorIdEUsuario(id, usuarioId);

        if (dto.getInstituicaoId() != null && !Objects.equals(dto.getInstituicaoId(), conta.getInstituicao().getId())) {
            throw new RegraNegocioException("instituicaoId", "msg.conta.instituicao.imutavel");
        }

        String descricao = normalizarTexto(dto.getDescricao());
        if (contaRepository.contarPorUsuarioEDescricao(usuarioId, descricao, id) > 0) {
            throw new RegraNegocioException("descricao", "msg.conta.descricao.duplicada");
        }

        conta.setDescricao(descricao);
        conta.setTipo(dto.getTipo());
        conta.setAgencia(normalizarTexto(dto.getAgencia()));
        conta.setNumero(normalizarTexto(dto.getNumero()));
        if (dto.getMoeda() != null && !dto.getMoeda().isBlank()) {
            conta.setMoeda(dto.getMoeda().trim().toUpperCase());
        }
        conta.setNomeGerente(normalizarTexto(dto.getNomeGerente()));
        conta.setTelGerente(normalizarTexto(dto.getTelGerente()));
        conta.setConsideraSaldo(dto.isConsideraSaldo());
        conta.setAtivo(dto.isAtivo());

        Conta salva = contaRepository.save(conta);
        invalidarCache(usuarioId);
        return salva;
    }

    @Transactional
    public void ajustarSaldo(Long id, ContaAjusteSaldoDTO dto, Long usuarioId, String usuarioAuditoria) {
        Conta conta = buscarPorIdEUsuario(id, usuarioId);

        if (dto.getNovoSaldo() == null) {
            throw new RegraNegocioException("novoSaldo", "conta.validacao.novoSaldo.obrigatorio");
        }

        conta.setSaldo(dto.getNovoSaldo());
        contaRepository.save(conta);
        invalidarCache(usuarioId);
    }

    @Transactional
    public void desativar(Long id, Long usuarioId) {
        Conta conta = buscarPorIdEUsuario(id, usuarioId);
        conta.setAtivo(false);
        contaRepository.save(conta);
        invalidarCache(usuarioId);
    }

    @Transactional
    public void excluir(Long id, Long usuarioId, String usuarioAuditoria) {
        Conta conta = buscarPorIdEUsuario(id, usuarioId);

        long usoTransacoes = contarEmTabela("TRANSACOES_BANCARIAS", id);
        if (usoTransacoes > 0) {
            throw new RegraNegocioException("msg.conta.em-uso.bloqueada");
        }

        long usoGeral = contarUso(id);
        if (usoGeral > 0) {
            boolean bloqueiaEmUso = parametroGlobalRepository
                .findByCodigo(ParametrosContaCatalogo.CONTA_EXCLUSAO_BLOQUEIA_EM_USO.getCodigo())
                .map(p -> Boolean.parseBoolean(p.getValor()))
                .orElse(true);

            if (bloqueiaEmUso) {
                throw new RegraNegocioException("msg.conta.em-uso.bloqueada");
            } else {
                desvincularUso(id);
            }
        }

        conta.setDataExclusao(Instant.now());
        conta.setExcluidoPor(usuarioAuditoria);
        contaRepository.save(conta);
        invalidarCache(usuarioId);
    }

    @Transactional(readOnly = true)
    public List<ContaOpcaoDTO> listarOpcoesCombobox(Long usuarioId) {
        boolean usarCache = parametroGlobalRepository
            .findByCodigo(ParametrosContaCatalogo.CONTA_COMBOBOX_CACHE.getCodigo())
            .map(p -> Boolean.parseBoolean(p.getValor()))
            .orElse(true);

        if (usarCache && cacheOpcoes.containsKey(usuarioId)) {
            return cacheOpcoes.get(usuarioId);
        }

        List<Conta> ativas = contaRepository.listarAtivasPorUsuario(usuarioId);
        List<ContaOpcaoDTO> dtos = ativas.stream()
            .map(c -> new ContaOpcaoDTO(
                c.getId(),
                c.getDescricao(),
                c.getTipo(),
                c.getMoeda(),
                c.getInstituicao().getNome(),
                c.getSaldo()))
            .toList();

        if (usarCache) {
            cacheOpcoes.put(usuarioId, dtos);
        }
        return dtos;
    }

    public long contarUso(Long contaId) {
        if (contaId == null) {
            return 0L;
        }
        long total = 0L;
        total += contarEmTabela("TRANSACOES_BANCARIAS", contaId);
        total += contarEmTabela("RECEITAS", contaId);
        total += contarEmTabela("DESPESAS", contaId);
        total += contarEmTabela("INVESTIMENTOS", contaId);
        total += contarEmTabela("CARTOES_CREDITO", contaId);
        return total;
    }

    private long contarEmTabela(String tabela, Long contaId) {
        try {
            String sql = "SELECT COUNT(*) FROM " + tabela + " WHERE CTA_ID = ? AND audit_data_exclusao IS NULL";
            Long count = jdbcTemplate.queryForObject(sql, Long.class, contaId);
            return count != null ? count : 0L;
        } catch (Exception e) {
            return 0L;
        }
    }

    private void desvincularUso(Long contaId) {
        desvincularTabela("RECEITAS", contaId);
        desvincularTabela("DESPESAS", contaId);
        desvincularTabela("INVESTIMENTOS", contaId);
        desvincularTabela("CARTOES_CREDITO", contaId);
    }

    private void desvincularTabela(String tabela, Long contaId) {
        try {
            String sql = "UPDATE " + tabela + " SET CTA_ID = NULL WHERE CTA_ID = ?";
            jdbcTemplate.update(sql, contaId);
        } catch (Exception ignored) {
        }
    }

    private void invalidarCache(Long usuarioId) {
        cacheOpcoes.remove(usuarioId);
    }

    private String normalizarTexto(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return valor.trim();
    }
}
