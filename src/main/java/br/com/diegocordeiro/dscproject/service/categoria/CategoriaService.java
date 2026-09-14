package br.com.diegocordeiro.dscproject.service.categoria;

import br.com.diegocordeiro.dscproject.dto.categoria.CategoriaFiltroDTO;
import br.com.diegocordeiro.dscproject.dto.categoria.CategoriaFormDTO;
import br.com.diegocordeiro.dscproject.dto.categoria.CategoriaGridDTO;
import br.com.diegocordeiro.dscproject.dto.categoria.CategoriaOpcaoDTO;
import br.com.diegocordeiro.dscproject.enums.AplicaA;
import br.com.diegocordeiro.dscproject.model.categoria.Categoria;
import br.com.diegocordeiro.dscproject.config.parametro.ParametrosCategoriaCatalogo;
import br.com.diegocordeiro.dscproject.repository.categoria.CategoriaRepository;
import br.com.diegocordeiro.dscproject.repository.parametro.ParametroGlobalRepository;
import br.com.diegocordeiro.dscproject.service.exceptions.RegistroNaoEncontradoException;
import br.com.diegocordeiro.dscproject.service.exceptions.RegraNegocioException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final ParametroGlobalRepository parametroGlobalRepository;
    private final JdbcTemplate jdbcTemplate;

    private final Map<AplicaA, List<CategoriaOpcaoDTO>> cacheOpcoes = new ConcurrentHashMap<>();

    public CategoriaService(CategoriaRepository categoriaRepository, ParametroGlobalRepository parametroGlobalRepository, JdbcTemplate jdbcTemplate) {
        this.categoriaRepository = categoriaRepository;
        this.parametroGlobalRepository = parametroGlobalRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    public static String normalizarCodigo(String codigo) {
        if (codigo == null) {
            return "";
        }
        String semAcento = Normalizer.normalize(codigo.trim(), Normalizer.Form.NFD)
            .replaceAll("\\p{M}", "");
        String comUnderscore = semAcento.replaceAll("[\\s-]+", "_");
        String limpo = comUnderscore.replaceAll("[^a-zA-Z0-9_]", "");
        return limpo.toUpperCase();
    }

    @Transactional(readOnly = true)
    public List<CategoriaGridDTO> listar(CategoriaFiltroDTO filtro) {
        String busca = (filtro.getBusca() != null && !filtro.getBusca().isBlank()) ? filtro.getBusca().trim() : null;
        Boolean sistema = filtro.getSistemaBoolean();
        String situacao = filtro.getSituacaoNormalizada();

        List<Categoria> categorias = categoriaRepository.listarComFiltros(busca, filtro.getAplicaA(), sistema, situacao);

        return categorias.stream()
            .map(c -> CategoriaGridDTO.builder()
                .id(c.getId())
                .codigo(c.getCodigo())
                .nome(c.getNome())
                .aplicaA(c.getAplicaA())
                .cor(c.getCor())
                .icone(c.getIcone())
                .sistema(c.isSistema())
                .ativo(c.isAtivo())
                .excluido(c.isExcluido())
                .qtdUso(contarUso(c.getId()))
                .build())
            .toList();
    }

    @Transactional(readOnly = true)
    public Categoria buscarPorId(Long id) {
        return categoriaRepository.findById(id)
            .orElseThrow(() -> new RegistroNaoEncontradoException("categoria.nao-encontrada"));
    }

    @Transactional
    public Categoria inserir(CategoriaFormDTO dto) {
        String codigoNormalizado = normalizarCodigo(dto.getCodigo());
        if (codigoNormalizado.isBlank()) {
            throw new RegraNegocioException("codigo", "categoria.validacao.codigo.obrigatorio");
        }

        if (categoriaRepository.contarPorCodigo(codigoNormalizado, null) > 0) {
            throw new RegraNegocioException("codigo", "categoria.codigo.duplicado");
        }

        Categoria categoria = new Categoria();
        categoria.setCodigo(codigoNormalizado);
        categoria.setNome(dto.getNome().trim());
        categoria.setAplicaA(dto.getAplicaA());
        categoria.setCor(tratarTextoOpcional(dto.getCor()));
        categoria.setIcone(tratarTextoOpcional(dto.getIcone()));
        categoria.setAtivo(true);
        categoria.setSistema(false);

        Categoria salva = categoriaRepository.save(categoria);
        invalidarCache();
        return salva;
    }

    @Transactional
    public Categoria editar(Long id, CategoriaFormDTO dto) {
        Categoria categoria = buscarPorId(id);

        if (categoria.isSistema()) {
            String codigoNormalizado = normalizarCodigo(dto.getCodigo());
            if (!codigoNormalizado.equalsIgnoreCase(categoria.getCodigo())) {
                throw new RegraNegocioException("codigo", "categoria.sistema.codigo-imutavel");
            }
        } else {
            String codigoNormalizado = normalizarCodigo(dto.getCodigo());
            if (codigoNormalizado.isBlank()) {
                throw new RegraNegocioException("codigo", "categoria.validacao.codigo.obrigatorio");
            }
            if (categoriaRepository.contarPorCodigo(codigoNormalizado, id) > 0) {
                throw new RegraNegocioException("codigo", "categoria.codigo.duplicado");
            }
            categoria.setCodigo(codigoNormalizado);
        }

        categoria.setNome(dto.getNome().trim());
        categoria.setAplicaA(dto.getAplicaA());
        categoria.setCor(tratarTextoOpcional(dto.getCor()));
        categoria.setIcone(tratarTextoOpcional(dto.getIcone()));
        categoria.setAtivo(dto.isAtivo());

        Categoria salva = categoriaRepository.save(categoria);
        invalidarCache();
        return salva;
    }

    @Transactional
    public void desativar(Long id) {
        Categoria categoria = buscarPorId(id);
        categoria.setAtivo(false);
        categoriaRepository.save(categoria);
        invalidarCache();
    }

    @Transactional
    public void excluir(Long id, String usuarioLogado) {
        Categoria categoria = buscarPorId(id);

        if (categoria.isSistema()) {
            throw new RegraNegocioException("categoria.sistema.nao-excluivel");
        }

        long uso = contarUso(id);
        if (uso > 0) {
            boolean bloqueiaEmUso = parametroGlobalRepository
                .findByCodigo(ParametrosCategoriaCatalogo.CATEGORIA_EXCLUSAO_BLOQUEIA_EM_USO.getCodigo())
                .map(p -> Boolean.parseBoolean(p.getValor()))
                .orElse(true);

            if (bloqueiaEmUso) {
                throw new RegraNegocioException("categoria.em-uso.bloqueada");
            } else {
                desvincularUso(id);
            }
        }

        categoria.setDataExclusao(Instant.now());
        categoria.setExcluidoPor(usuarioLogado);
        categoriaRepository.save(categoria);
        invalidarCache();
    }

    @Transactional(readOnly = true)
    public List<CategoriaOpcaoDTO> listarOpcoesCombobox(AplicaA aplicaA) {
        boolean usarCache = parametroGlobalRepository
            .findByCodigo(ParametrosCategoriaCatalogo.CATEGORIA_COMBOBOX_CACHE.getCodigo())
            .map(p -> Boolean.parseBoolean(p.getValor()))
            .orElse(true);

        if (usarCache && cacheOpcoes.containsKey(aplicaA)) {
            return cacheOpcoes.get(aplicaA);
        }

        List<Categoria> ativas = categoriaRepository.listarAtivasPorAplicaA(aplicaA);
        List<CategoriaOpcaoDTO> dtos = ativas.stream()
            .map(c -> new CategoriaOpcaoDTO(c.getId(), c.getCodigo(), c.getNome(), c.getCor(), c.getIcone()))
            .toList();

        if (usarCache) {
            cacheOpcoes.put(aplicaA, dtos);
        }
        return dtos;
    }

    public long contarUso(Long categoriaId) {
        if (categoriaId == null) {
            return 0L;
        }
        long total = 0L;
        total += contarEmTabela("RECEITAS", categoriaId);
        total += contarEmTabela("DESPESAS", categoriaId);
        total += contarEmTabela("TRANSACOES_BANCARIAS", categoriaId);
        return total;
    }

    private long contarEmTabela(String tabela, Long categoriaId) {
        try {
            String sql = "SELECT COUNT(*) FROM " + tabela + " WHERE CATE_ID = ? AND audit_data_exclusao IS NULL";
            Long count = jdbcTemplate.queryForObject(sql, Long.class, categoriaId);
            return count != null ? count : 0L;
        } catch (Exception e) {
            return 0L;
        }
    }

    private void desvincularUso(Long categoriaId) {
        desvincularTabela("RECEITAS", categoriaId);
        desvincularTabela("DESPESAS", categoriaId);
        desvincularTabela("TRANSACOES_BANCARIAS", categoriaId);
    }

    private void desvincularTabela(String tabela, Long categoriaId) {
        try {
            String sql = "UPDATE " + tabela + " SET CATE_ID = NULL WHERE CATE_ID = ?";
            jdbcTemplate.update(sql, categoriaId);
        } catch (Exception ignored) {
        }
    }

    private void invalidarCache() {
        cacheOpcoes.clear();
    }

    private String tratarTextoOpcional(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        return texto.trim();
    }
}
