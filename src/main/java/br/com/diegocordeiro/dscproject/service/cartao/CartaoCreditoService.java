package br.com.diegocordeiro.dscproject.service.cartao;

import br.com.diegocordeiro.dscproject.dto.cartaocredito.CartaoCreditoEdicaoDTO;
import br.com.diegocordeiro.dscproject.dto.cartaocredito.CartaoCreditoFormDTO;
import br.com.diegocordeiro.dscproject.dto.cartaocredito.CartaoCreditoGridDTO;
import br.com.diegocordeiro.dscproject.dto.cartaocredito.CartaoCreditoOpcaoDTO;
import br.com.diegocordeiro.dscproject.enums.BandeiraCartao;
import br.com.diegocordeiro.dscproject.model.cartao.CartaoCredito;
import br.com.diegocordeiro.dscproject.model.conta.Conta;
import br.com.diegocordeiro.dscproject.model.usuario.Usuario;
import br.com.diegocordeiro.dscproject.config.parametro.ParametrosCartaoCatalogo;
import br.com.diegocordeiro.dscproject.repository.cartao.CartaoCreditoRepository;
import br.com.diegocordeiro.dscproject.repository.conta.ContaRepository;
import br.com.diegocordeiro.dscproject.repository.parametro.ParametroGlobalRepository;
import br.com.diegocordeiro.dscproject.repository.usuario.UsuarioRepository;
import br.com.diegocordeiro.dscproject.service.exceptions.RegistroNaoEncontradoException;
import br.com.diegocordeiro.dscproject.service.exceptions.RegraNegocioException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

@Service
public class CartaoCreditoService {

    private static final Pattern PADRAO_FINAL_CARTAO = Pattern.compile("\\d{4}");

    private final CartaoCreditoRepository cartaoCreditoRepository;
    private final ContaRepository contaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ParametroGlobalRepository parametroGlobalRepository;
    private final JdbcTemplate jdbcTemplate;

    private final Map<Long, List<CartaoCreditoOpcaoDTO>> cacheOpcoes = new ConcurrentHashMap<>();

    public CartaoCreditoService(CartaoCreditoRepository cartaoCreditoRepository, ContaRepository contaRepository, UsuarioRepository usuarioRepository, ParametroGlobalRepository parametroGlobalRepository, JdbcTemplate jdbcTemplate) {
        this.cartaoCreditoRepository = cartaoCreditoRepository;
        this.contaRepository = contaRepository;
        this.usuarioRepository = usuarioRepository;
        this.parametroGlobalRepository = parametroGlobalRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional(readOnly = true)
    public List<CartaoCreditoGridDTO> listarParaGrid(Long usuarioId) {
        List<CartaoCredito> cartoes = cartaoCreditoRepository.listarPorUsuario(usuarioId);
        return cartoes.stream()
            .map(c -> CartaoCreditoGridDTO.builder()
                .id(c.getId())
                .descricao(c.getDescricao())
                .bandeira(c.getBandeira())
                .finalCartao(c.getFinalCartao())
                .limite(c.getLimite())
                .diaFechamento(c.getDiaFechamento())
                .diaVencimento(c.getDiaVencimento())
                .contaId(c.getConta() != null ? c.getConta().getId() : null)
                .contaDescricao(c.getConta() != null ? c.getConta().getDescricao() : null)
                .qtdVinculos(contarVinculos(c.getId()))
                .ativo(c.isAtivo())
                .excluido(c.isExcluido())
                .build())
            .toList();
    }

    @Transactional(readOnly = true)
    public CartaoCredito buscarPorIdEUsuario(Long id, Long usuarioId) {
        return cartaoCreditoRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(id, usuarioId)
            .orElseThrow(() -> new RegistroNaoEncontradoException("msg.cartao.nao-encontrado"));
    }

    @Transactional(readOnly = true)
    public CartaoCreditoEdicaoDTO buscarParaEdicao(Long id, Long usuarioId) {
        CartaoCredito c = buscarPorIdEUsuario(id, usuarioId);
        return CartaoCreditoEdicaoDTO.builder()
            .id(c.getId())
            .descricao(c.getDescricao())
            .bandeira(c.getBandeira())
            .finalCartao(c.getFinalCartao())
            .limite(c.getLimite())
            .diaFechamento(c.getDiaFechamento())
            .diaVencimento(c.getDiaVencimento())
            .contaId(c.getConta() != null ? c.getConta().getId() : null)
            .ativo(c.isAtivo())
            .qtdVinculos(contarVinculos(c.getId()))
            .build();
    }

    @Transactional
    public CartaoCredito inserir(CartaoCreditoFormDTO dto, Long usuarioId, String usuarioAuditoria) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
            .orElseThrow(() -> new RegistroNaoEncontradoException("msg.usuario.nao-encontrado"));

        validarCampos(dto);
        Conta conta = resolverContaDebito(dto.getContaId(), usuarioId);

        CartaoCredito cartao = new CartaoCredito();
        cartao.setDescricao(dto.getDescricao().trim());
        cartao.setBandeira(normalizarTexto(dto.getBandeira()));
        cartao.setFinalCartao(normalizarTexto(dto.getFinalCartao()));
        cartao.setLimite(dto.getLimite());
        cartao.setDiaFechamento(dto.getDiaFechamento());
        cartao.setDiaVencimento(dto.getDiaVencimento());
        cartao.setConta(conta);
        cartao.setAtivo(true);
        cartao.setUsuario(usuario);

        CartaoCredito salvo = cartaoCreditoRepository.save(cartao);
        invalidarCache(usuarioId);
        return salvo;
    }

    @Transactional
    public CartaoCredito editar(Long id, CartaoCreditoFormDTO dto, Long usuarioId, String usuarioAuditoria) {
        CartaoCredito cartao = buscarPorIdEUsuario(id, usuarioId);

        validarCampos(dto);
        Conta conta = resolverContaDebito(dto.getContaId(), usuarioId);

        cartao.setDescricao(dto.getDescricao().trim());
        cartao.setBandeira(normalizarTexto(dto.getBandeira()));
        cartao.setFinalCartao(normalizarTexto(dto.getFinalCartao()));
        cartao.setLimite(dto.getLimite());
        cartao.setDiaFechamento(dto.getDiaFechamento());
        cartao.setDiaVencimento(dto.getDiaVencimento());
        cartao.setConta(conta);
        cartao.setAtivo(dto.isAtivo());

        CartaoCredito salvo = cartaoCreditoRepository.save(cartao);
        invalidarCache(usuarioId);
        return salvo;
    }

    @Transactional
    public void excluir(Long id, Long usuarioId, String usuarioAuditoria) {
        CartaoCredito cartao = buscarPorIdEUsuario(id, usuarioId);

        long usoFaturas = contarEmTabela("FATURAS_CARTAO", id);
        if (usoFaturas > 0) {
            throw new RegraNegocioException("msg.cartao.em-uso.bloqueada");
        }

        long usoDespesas = contarEmTabela("DESPESAS", id);
        if (usoDespesas > 0) {
            boolean bloqueiaEmUso = parametroGlobalRepository
                .findByCodigo(ParametrosCartaoCatalogo.CARTAO_EXCLUSAO_BLOQUEIA_EM_USO.getCodigo())
                .map(p -> Boolean.parseBoolean(p.getValor()))
                .orElse(true);

            if (bloqueiaEmUso) {
                throw new RegraNegocioException("msg.cartao.em-uso.bloqueada");
            } else {
                desvincularTabela("DESPESAS", id);
            }
        }

        cartao.setDataExclusao(Instant.now());
        cartao.setExcluidoPor(usuarioAuditoria);
        cartaoCreditoRepository.save(cartao);
        invalidarCache(usuarioId);
    }

    @Transactional(readOnly = true)
    public List<CartaoCreditoOpcaoDTO> listarOpcoesCombobox(Long usuarioId) {
        boolean usarCache = parametroGlobalRepository
            .findByCodigo(ParametrosCartaoCatalogo.CARTAO_COMBOBOX_CACHE.getCodigo())
            .map(p -> Boolean.parseBoolean(p.getValor()))
            .orElse(true);

        if (usarCache && cacheOpcoes.containsKey(usuarioId)) {
            return cacheOpcoes.get(usuarioId);
        }

        List<CartaoCredito> ativos = cartaoCreditoRepository.listarAtivasPorUsuario(usuarioId);
        List<CartaoCreditoOpcaoDTO> dtos = ativos.stream()
            .map(c -> new CartaoCreditoOpcaoDTO(c.getId(), c.getDescricao(), c.getBandeira(), c.getFinalCartao(), c.getDiaFechamento(), c.getDiaVencimento()))
            .toList();

        if (usarCache) {
            cacheOpcoes.put(usuarioId, dtos);
        }
        return dtos;
    }

    private void invalidarCache(Long usuarioId) {
        cacheOpcoes.remove(usuarioId);
    }

    private void desvincularTabela(String tabela, Long cartaoId) {
        try {
            String sql = "UPDATE " + tabela + " SET CACR_ID = NULL WHERE CACR_ID = ?";
            jdbcTemplate.update(sql, cartaoId);
        } catch (Exception ignored) {
        }
    }

    /** Repete no serviço as validações de campo já feitas na tela/Validator (RNF03). */
    private void validarCampos(CartaoCreditoFormDTO dto) {
        if (dto.getDescricao() == null || dto.getDescricao().isBlank()) {
            throw new RegraNegocioException("descricao", "cartao.validacao.descricao.obrigatoria");
        }
        if (dto.getBandeira() != null && !dto.getBandeira().isBlank() && BandeiraCartao.porCodigo(dto.getBandeira()) == null) {
            throw new RegraNegocioException("bandeira", "cartao.validacao.bandeira.invalida");
        }
        if (dto.getFinalCartao() != null && !dto.getFinalCartao().isBlank() && !PADRAO_FINAL_CARTAO.matcher(dto.getFinalCartao()).matches()) {
            throw new RegraNegocioException("finalCartao", "cartao.validacao.finalCartao.invalido");
        }
        if (dto.getDiaFechamento() != null && (dto.getDiaFechamento() < 1 || dto.getDiaFechamento() > 31)) {
            throw new RegraNegocioException("diaFechamento", "cartao.validacao.dia.invalido");
        }
        if (dto.getDiaVencimento() != null && (dto.getDiaVencimento() < 1 || dto.getDiaVencimento() > 31)) {
            throw new RegraNegocioException("diaVencimento", "cartao.validacao.dia.invalido");
        }
    }

    private Conta resolverContaDebito(Long contaId, Long usuarioId) {
        if (contaId == null) {
            return null;
        }
        Conta conta = contaRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(contaId, usuarioId)
            .orElseThrow(() -> new RegraNegocioException("contaId", "msg.cartao.conta.invalida"));
        if (!conta.isAtivo()) {
            throw new RegraNegocioException("contaId", "msg.cartao.conta.invalida");
        }
        return conta;
    }

    private String normalizarTexto(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return valor.trim();
    }

    /** Soma faturas e despesas não excluídas vinculadas ao cartão. */
    private long contarVinculos(Long cartaoId) {
        if (cartaoId == null) {
            return 0L;
        }
        return contarEmTabela("FATURAS_CARTAO", cartaoId) + contarEmTabela("DESPESAS", cartaoId);
    }

    private long contarEmTabela(String tabela, Long cartaoId) {
        try {
            String sql = "SELECT COUNT(*) FROM " + tabela + " WHERE CACR_ID = ? AND audit_data_exclusao IS NULL";
            Long count = jdbcTemplate.queryForObject(sql, Long.class, cartaoId);
            return count != null ? count : 0L;
        } catch (Exception e) {
            return 0L;
        }
    }
}
