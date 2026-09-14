package br.com.diegocordeiro.dscproject.service.perfil;

import br.com.diegocordeiro.dscproject.dto.perfil.PerfilFormDTO;
import br.com.diegocordeiro.dscproject.model.perfil.Perfil;
import br.com.diegocordeiro.dscproject.model.perfil.PerfilPermissao;
import br.com.diegocordeiro.dscproject.model.perfil.Permissao;
import br.com.diegocordeiro.dscproject.model.usuario.Usuario;
import br.com.diegocordeiro.dscproject.repository.perfil.PerfilPermissaoRepository;
import br.com.diegocordeiro.dscproject.repository.perfil.PerfilRepository;
import br.com.diegocordeiro.dscproject.repository.perfil.PermissaoRepository;
import br.com.diegocordeiro.dscproject.repository.usuario.UsuarioRepository;
import br.com.diegocordeiro.dscproject.service.exceptions.RegraNegocioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PerfilServiceTest {

    @Mock private PerfilRepository perfilRepository;
    @Mock private PerfilPermissaoRepository perfilPermissaoRepository;
    @Mock private PermissaoRepository permissaoRepository;
    @Mock private UsuarioRepository usuarioRepository;

    @InjectMocks
    private PerfilService perfilService;

    @BeforeEach
    void stubComum() {
        when(perfilRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(perfilPermissaoRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(perfilPermissaoRepository.findByPerfilAndDataExclusaoIsNull(any())).thenReturn(new ArrayList<>());
        when(perfilPermissaoRepository.findByPerfilAndPermissao(any(), any())).thenReturn(Optional.empty());
        when(perfilPermissaoRepository.buscarCodigosPermissaoAtivos(any())).thenReturn(new ArrayList<>());
        when(permissaoRepository.findByCodigo(anyString()))
            .thenAnswer(i -> Optional.of(permissao(i.getArgument(0), false)));
        when(perfilRepository.contarPerfisComUsuarioAtivoConcedendo(anyString())).thenReturn(1L);
    }

    private static Permissao permissao(String codigo, boolean orfa) {
        Permissao p = new Permissao(codigo, codigo, null, "Perfis e Permissões");
        p.setOrfa(orfa);
        return p;
    }

    private static Perfil perfil(Long id, String codigo, boolean sistema) {
        Perfil p = new Perfil(codigo, codigo, null, sistema);
        p.setId(id);
        return p;
    }

    private static PerfilFormDTO form(String codigo, String nome, String... permissoes) {
        PerfilFormDTO dto = new PerfilFormDTO();
        dto.setCodigo(codigo);
        dto.setNome(nome);
        dto.setPermissoes(new ArrayList<>(List.of(permissoes)));
        return dto;
    }

    // ---------- BDD 16.1 — criar perfil com permissões ----------

    @Test
    void inserir_criaPerfilNaoSistemaComExatamenteAsPermissoesMarcadas() {
        Perfil salvo = perfilService.inserir(form("RELATORIOS", "Relatórios", "USUARIOS_LISTAR", "DESPESAS_LISTAR"));

        assertThat(salvo.getCodigo()).isEqualTo("RELATORIOS");
        assertThat(salvo.isSistema()).isFalse();
        verify(perfilPermissaoRepository, org.mockito.Mockito.times(2)).save(any(PerfilPermissao.class));
    }

    @Test
    void inserir_codigoEmMinusculas_ficaEmMaiusculas() {
        Perfil salvo = perfilService.inserir(form("relatorios", "Relatórios"));

        assertThat(salvo.getCodigo()).isEqualTo("RELATORIOS");
    }

    // ---------- RN04 / BDD 16.9 — permissão órfã não vincula ----------

    @Test
    void inserir_comPermissaoOrfa_lancaRegraNegocio() {
        when(permissaoRepository.findByCodigo("RECURSO_ANTIGO")).thenReturn(Optional.of(permissao("RECURSO_ANTIGO", true)));

        assertThatThrownBy(() -> perfilService.inserir(form("X", "X", "RECURSO_ANTIGO")))
            .isInstanceOf(RegraNegocioException.class)
            .hasMessage("perfil.permissao.orfa");
    }

    @Test
    void editar_removeVinculoDePermissaoQueSeTornouOrfa_fazSoftDelete() {
        Perfil perfil = perfil(5L, "RELATORIOS", false);
        when(perfilRepository.findById(5L)).thenReturn(Optional.of(perfil));

        PerfilPermissao vinculoOrfao = new PerfilPermissao(perfil, permissao("RECURSO_ANTIGO", true));
        PerfilPermissao vinculoManter = new PerfilPermissao(perfil, permissao("PERFIS_VINCULAR_PERMISSAO", false));
        when(perfilPermissaoRepository.buscarCodigosPermissaoAtivos(5L))
            .thenReturn(new ArrayList<>(List.of("RECURSO_ANTIGO", "PERFIS_VINCULAR_PERMISSAO")));
        when(perfilPermissaoRepository.findByPerfilAndDataExclusaoIsNull(perfil))
            .thenReturn(new ArrayList<>(List.of(vinculoOrfao, vinculoManter)));

        perfilService.editar(5L, form("RELATORIOS", "Relatórios", "PERFIS_VINCULAR_PERMISSAO"), null);

        assertThat(vinculoOrfao.getDataExclusao()).isNotNull();     // órfã pode ser removida
        assertThat(vinculoManter.getDataExclusao()).isNull();
    }

    // ---------- RN02 / BDD 16.3 — código de perfil de sistema não muda ----------

    @Test
    void editar_perfilDeSistema_naoAlteraOCodigoAindaQueOFormularioEnvie() {
        Perfil admin = perfil(1L, "ADMIN", true);
        when(perfilRepository.findById(1L)).thenReturn(Optional.of(admin));

        perfilService.editar(1L, form("OUTRO", "Administrador", "PERFIS_VINCULAR_PERMISSAO"), null);

        assertThat(admin.getCodigo()).isEqualTo("ADMIN");
    }

    @Test
    void editar_perfilNaoSistema_alteraOCodigo() {
        Perfil relatorios = perfil(9L, "RELATORIOS", false);
        when(perfilRepository.findById(9L)).thenReturn(Optional.of(relatorios));

        perfilService.editar(9L, form("RELAT", "Relatórios"), null);

        assertThat(relatorios.getCodigo()).isEqualTo("RELAT");
    }

    // ---------- BDD 16.1 — substituição do conjunto de vínculos ----------

    @Test
    void editar_substituiConjuntoDeVinculos_softDeleteDosQueSairamECriaOsQueEntraram() {
        Perfil perfil = perfil(5L, "RELATORIOS", false);
        when(perfilRepository.findById(5L)).thenReturn(Optional.of(perfil));

        PerfilPermissao vinculoA = new PerfilPermissao(perfil, permissao("PERM_A", false));
        PerfilPermissao vinculoB = new PerfilPermissao(perfil, permissao("PERM_B", false));
        when(perfilPermissaoRepository.buscarCodigosPermissaoAtivos(5L)).thenReturn(new ArrayList<>(List.of("PERM_A", "PERM_B")));
        when(perfilPermissaoRepository.findByPerfilAndDataExclusaoIsNull(perfil))
            .thenReturn(new ArrayList<>(List.of(vinculoA, vinculoB)));

        perfilService.editar(5L, form("RELATORIOS", "Relatórios", "PERM_B", "PERM_C"), null);

        assertThat(vinculoA.getDataExclusao()).isNotNull();     // saiu
        assertThat(vinculoB.getDataExclusao()).isNull();        // permaneceu
        verify(perfilPermissaoRepository).save(any(PerfilPermissao.class));  // PERM_C entrou
    }

    // ---------- RN05 / BDD 16.4 — anti-lockout do próprio perfil ----------

    @Test
    void editar_removePerfisManterDoProprioPerfil_lancaRegraNegocioENaoGrava() {
        Perfil admin = perfil(1L, "ADMIN", true);
        when(perfilRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(perfilPermissaoRepository.buscarCodigosPermissaoAtivos(1L))
            .thenReturn(new ArrayList<>(List.of("PERFIS_VINCULAR_PERMISSAO", "USUARIOS_EDITAR")));
        Usuario logado = new Usuario();
        logado.setPerfil(admin);
        when(usuarioRepository.findByLoginOrEmail("diego", "diego")).thenReturn(logado);

        assertThatThrownBy(() -> perfilService.editar(1L, form("ADMIN", "Administrador", "USUARIOS_EDITAR"), "diego"))
            .isInstanceOf(RegraNegocioException.class)
            .hasMessage("perfil.antilockout.proprio");

        verify(perfilPermissaoRepository, never()).flush();
    }

    // ---------- RN06 / BDD 16.5 — anti-lockout global ----------

    @Test
    void editar_deixariaSistemaSemGestorDeUsuarios_lancaRegraNegocio() {
        Perfil admin = perfil(1L, "ADMIN", true);
        when(perfilRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(perfilRepository.contarPerfisComUsuarioAtivoConcedendo("USUARIOS_EDITAR")).thenReturn(0L);

        assertThatThrownBy(() -> perfilService.editar(1L, form("ADMIN", "Administrador", "PERFIS_VINCULAR_PERMISSAO"), null))
            .isInstanceOf(RegraNegocioException.class)
            .hasMessage("perfil.antilockout.global");
    }

    // ---------- RN07 / BDD 16.6 e 16.7 — exclusão ----------

    @Test
    void excluir_perfilDeSistema_recusa() {
        when(perfilRepository.findById(2L)).thenReturn(Optional.of(perfil(2L, "USER", true)));

        assertThatThrownBy(() -> perfilService.excluir(2L))
            .isInstanceOf(RegraNegocioException.class)
            .hasMessage("perfil.exclusao.sistema");
    }

    @Test
    void excluir_perfilComUsuariosVinculados_recusa() {
        Perfil relatorios = perfil(9L, "RELATORIOS", false);
        when(perfilRepository.findById(9L)).thenReturn(Optional.of(relatorios));
        when(usuarioRepository.existsByPerfil(relatorios)).thenReturn(true);

        assertThatThrownBy(() -> perfilService.excluir(9L))
            .isInstanceOf(RegraNegocioException.class)
            .hasMessage("perfil.exclusao.com.usuarios");
    }

    @Test
    void excluir_perfilSemUsuarios_fazSoftDeleteDoPerfilEDosVinculos() {
        Perfil relatorios = perfil(9L, "RELATORIOS", false);
        when(perfilRepository.findById(9L)).thenReturn(Optional.of(relatorios));
        when(usuarioRepository.existsByPerfil(relatorios)).thenReturn(false);
        PerfilPermissao vinculo = new PerfilPermissao(relatorios, permissao("PERM_A", false));
        when(perfilPermissaoRepository.findByPerfilAndDataExclusaoIsNull(relatorios))
            .thenReturn(new ArrayList<>(List.of(vinculo)));

        perfilService.excluir(9L);

        assertThat(relatorios.getDataExclusao()).isNotNull();
        assertThat(relatorios.getExcluidoPor()).isNotBlank();
        assertThat(vinculo.getDataExclusao()).isNotNull();
    }
}
