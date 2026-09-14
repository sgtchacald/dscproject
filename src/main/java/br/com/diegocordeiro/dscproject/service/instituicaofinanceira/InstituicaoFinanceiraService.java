package br.com.diegocordeiro.dscproject.service.instituicaofinanceira;

import br.com.diegocordeiro.dscproject.dto.instituicaofinanceira.InstituicaoFinanceiraEdicaoDTO;
import br.com.diegocordeiro.dscproject.dto.instituicaofinanceira.InstituicaoFinanceiraFormDTO;
import br.com.diegocordeiro.dscproject.dto.instituicaofinanceira.InstituicaoFinanceiraGridDTO;
import br.com.diegocordeiro.dscproject.dto.instituicaofinanceira.InstituicaoFinanceiraOpcaoDTO;
import br.com.diegocordeiro.dscproject.enums.TipoInstituicaoFinanceira;
import br.com.diegocordeiro.dscproject.model.instituicaofinanceira.InstituicaoFinanceira;
import br.com.diegocordeiro.dscproject.model.instituicaofinanceira.OpfiInstituicaoProvedor;
import br.com.diegocordeiro.dscproject.config.parametro.ParametrosInstituicaoCatalogo;
import br.com.diegocordeiro.dscproject.repository.instituicaofinanceira.InstituicaoFinanceiraRepository;
import br.com.diegocordeiro.dscproject.repository.instituicaofinanceira.OpfiInstituicaoProvedorRepository;
import br.com.diegocordeiro.dscproject.repository.parametro.ParametroGlobalRepository;
import br.com.diegocordeiro.dscproject.service.exceptions.RegistroNaoEncontradoException;
import br.com.diegocordeiro.dscproject.service.exceptions.RegraNegocioException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class InstituicaoFinanceiraService {

    private final InstituicaoFinanceiraRepository instituicaoFinanceiraRepository;
    private final OpfiInstituicaoProvedorRepository opfiInstituicaoProvedorRepository;
    private final ParametroGlobalRepository parametroGlobalRepository;
    private final JdbcTemplate jdbcTemplate;

    private final Map<String, List<InstituicaoFinanceiraOpcaoDTO>> cacheOpcoes = new ConcurrentHashMap<>();

    public InstituicaoFinanceiraService(InstituicaoFinanceiraRepository instituicaoFinanceiraRepository, OpfiInstituicaoProvedorRepository opfiInstituicaoProvedorRepository, ParametroGlobalRepository parametroGlobalRepository, JdbcTemplate jdbcTemplate) {
        this.instituicaoFinanceiraRepository = instituicaoFinanceiraRepository;
        this.opfiInstituicaoProvedorRepository = opfiInstituicaoProvedorRepository;
        this.parametroGlobalRepository = parametroGlobalRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional(readOnly = true)
    public List<InstituicaoFinanceiraGridDTO> listarParaGrid() {
        List<InstituicaoFinanceira> lista = instituicaoFinanceiraRepository.findAllByOrderByNomeAsc();
        return lista.stream()
            .map(i -> InstituicaoFinanceiraGridDTO.builder()
                .id(i.getId())
                .nome(i.getNome())
                .codigo(i.getCodigo())
                .tipo(i.getTipo())
                .qtdUso(contarUso(i.getId()))
                .qtdProvedores(opfiInstituicaoProvedorRepository.countByInstituicaoIdAndDataExclusaoIsNull(i.getId()))
                .sistema(i.isSistema())
                .ativo(i.isAtivo())
                .excluido(i.isExcluido())
                .build())
            .toList();
    }

    @Transactional(readOnly = true)
    public InstituicaoFinanceira buscarPorId(Long id) {
        return instituicaoFinanceiraRepository.findById(id)
            .orElseThrow(() -> new RegistroNaoEncontradoException("msg.instituicao.nao-encontrada"));
    }

    @Transactional(readOnly = true)
    public InstituicaoFinanceiraEdicaoDTO buscarParaEdicao(Long id) {
        InstituicaoFinanceira i = buscarPorId(id);
        return InstituicaoFinanceiraEdicaoDTO.builder()
            .id(i.getId())
            .nome(i.getNome())
            .codigo(i.getCodigo())
            .tipo(i.getTipo())
            .ativo(i.isAtivo())
            .sistema(i.isSistema())
            .qtdUso(contarUso(i.getId()))
            .build();
    }

    @Transactional
    public InstituicaoFinanceira inserir(InstituicaoFinanceiraFormDTO dto) {
        String nome = normalizarNome(dto.getNome());
        String codigo = normalizarCodigo(dto.getCodigo());

        if (instituicaoFinanceiraRepository.contarPorNome(nome, null) > 0) {
            throw new RegraNegocioException("nome", "msg.instituicao.nome.duplicado");
        }

        if (codigo != null && instituicaoFinanceiraRepository.contarPorCodigo(codigo, null) > 0) {
            throw new RegraNegocioException("codigo", "msg.instituicao.codigo.duplicado");
        }

        InstituicaoFinanceira entity = new InstituicaoFinanceira();
        entity.setNome(nome);
        entity.setCodigo(codigo);
        entity.setTipo(dto.getTipo());
        entity.setAtivo(true);
        entity.setSistema(false);

        InstituicaoFinanceira salva = instituicaoFinanceiraRepository.save(entity);
        invalidarCache();
        return salva;
    }

    @Transactional
    public InstituicaoFinanceira editar(Long id, InstituicaoFinanceiraFormDTO dto) {
        InstituicaoFinanceira entity = buscarPorId(id);
        String nome = normalizarNome(dto.getNome());
        String codigo = normalizarCodigo(dto.getCodigo());

        if (entity.isSistema()) {
            if (!Objects.equals(entity.getNome(), nome) || !Objects.equals(entity.getCodigo(), codigo)) {
                throw new RegraNegocioException("msg.instituicao.sistema.imutavel");
            }
        } else {
            if (instituicaoFinanceiraRepository.contarPorNome(nome, id) > 0) {
                throw new RegraNegocioException("nome", "msg.instituicao.nome.duplicado");
            }
            if (codigo != null && instituicaoFinanceiraRepository.contarPorCodigo(codigo, id) > 0) {
                throw new RegraNegocioException("codigo", "msg.instituicao.codigo.duplicado");
            }
            entity.setNome(nome);
            entity.setCodigo(codigo);
        }

        entity.setTipo(dto.getTipo());
        entity.setAtivo(dto.isAtivo());

        InstituicaoFinanceira salva = instituicaoFinanceiraRepository.save(entity);
        invalidarCache();
        return salva;
    }

    @Transactional
    public void desativar(Long id) {
        InstituicaoFinanceira entity = buscarPorId(id);
        entity.setAtivo(false);
        instituicaoFinanceiraRepository.save(entity);
        invalidarCache();
    }

    @Transactional
    public void excluir(Long id, String usuarioLogado) {
        InstituicaoFinanceira entity = buscarPorId(id);

        if (entity.isSistema()) {
            throw new RegraNegocioException("msg.instituicao.sistema.imutavel");
        }

        long usoContas = contarEmTabela("CONTAS", id);
        long usoGeral = contarUso(id);

        if (usoContas > 0) {
            throw new RegraNegocioException("msg.instituicao.em-uso.bloqueada");
        }

        if (usoGeral > 0) {
            boolean bloqueiaEmUso = parametroGlobalRepository
                .findByCodigo(ParametrosInstituicaoCatalogo.INSTITUICAO_EXCLUSAO_BLOQUEIA_EM_USO.getCodigo())
                .map(p -> Boolean.parseBoolean(p.getValor()))
                .orElse(true);

            if (bloqueiaEmUso) {
                throw new RegraNegocioException("msg.instituicao.em-uso.bloqueada");
            } else {
                desvincularUso(id);
            }
        }

        // RN12: cascatear exclusão lógica dos vínculos de provedor
        List<OpfiInstituicaoProvedor> vinculos = opfiInstituicaoProvedorRepository.findByInstituicaoIdAndDataExclusaoIsNull(id);
        for (OpfiInstituicaoProvedor v : vinculos) {
            v.setDataExclusao(Instant.now());
            v.setExcluidoPor(usuarioLogado);
            opfiInstituicaoProvedorRepository.save(v);
        }

        entity.setDataExclusao(Instant.now());
        entity.setExcluidoPor(usuarioLogado);
        instituicaoFinanceiraRepository.save(entity);
        invalidarCache();
    }

    @Transactional(readOnly = true)
    public List<InstituicaoFinanceiraOpcaoDTO> listarOpcoesCombobox(TipoInstituicaoFinanceira tipo) {
        boolean usarCache = parametroGlobalRepository
            .findByCodigo(ParametrosInstituicaoCatalogo.INSTITUICAO_COMBOBOX_CACHE.getCodigo())
            .map(p -> Boolean.parseBoolean(p.getValor()))
            .orElse(true);

        String cacheKey = tipo != null ? tipo.name() : "TODAS";
        if (usarCache && cacheOpcoes.containsKey(cacheKey)) {
            return cacheOpcoes.get(cacheKey);
        }

        List<InstituicaoFinanceira> ativas = instituicaoFinanceiraRepository.listarAtivasPorTipo(tipo);
        List<InstituicaoFinanceiraOpcaoDTO> dtos = ativas.stream()
            .map(i -> new InstituicaoFinanceiraOpcaoDTO(i.getId(), i.getNome(), i.getCodigo(), i.getTipo()))
            .toList();

        if (usarCache) {
            cacheOpcoes.put(cacheKey, dtos);
        }
        return dtos;
    }

    public long contarUso(Long instituicaoId) {
        if (instituicaoId == null) {
            return 0L;
        }
        long total = 0L;
        total += contarEmTabela("CONTAS", instituicaoId);
        total += contarEmTabela("INVESTIMENTOS", instituicaoId);
        total += contarEmTabela("OPFI_CONEXOES", instituicaoId);
        return total;
    }

    private long contarEmTabela(String tabela, Long id) {
        try {
            String sql = "SELECT COUNT(*) FROM " + tabela + " WHERE INFI_ID = ? AND audit_data_exclusao IS NULL";
            Long count = jdbcTemplate.queryForObject(sql, Long.class, id);
            return count != null ? count : 0L;
        } catch (Exception e) {
            return 0L;
        }
    }

    private void desvincularUso(Long id) {
        desvincularTabela("INVESTIMENTOS", id);
        desvincularTabela("OPFI_CONEXOES", id);
    }

    private void desvincularTabela(String tabela, Long id) {
        try {
            String sql = "UPDATE " + tabela + " SET INFI_ID = NULL WHERE INFI_ID = ?";
            jdbcTemplate.update(sql, id);
        } catch (Exception ignored) {
        }
    }

    private void invalidarCache() {
        cacheOpcoes.clear();
    }

    private String normalizarNome(String nome) {
        return (nome == null) ? "" : nome.trim();
    }

    private String normalizarCodigo(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            return null;
        }
        String digitos = codigo.replaceAll("\\D", "").trim();
        return digitos.isEmpty() ? null : digitos;
    }
}
