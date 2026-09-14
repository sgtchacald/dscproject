package br.com.diegocordeiro.dscproject.service.usuario;

import br.com.diegocordeiro.dscproject.dto.usuario.UsuarioRedeSocialDTO;
import br.com.diegocordeiro.dscproject.dto.usuario.UsuarioRedeSocialFormDTO;
import br.com.diegocordeiro.dscproject.enums.TipoRedeSocial;
import br.com.diegocordeiro.dscproject.model.usuario.Usuario;
import br.com.diegocordeiro.dscproject.model.usuario.UsuarioRedeSocial;
import br.com.diegocordeiro.dscproject.repository.usuario.UsuarioRedeSocialRepository;
import br.com.diegocordeiro.dscproject.repository.usuario.UsuarioRepository;
import br.com.diegocordeiro.dscproject.service.exceptions.RegistroNaoEncontradoException;
import br.com.diegocordeiro.dscproject.service.exceptions.RegraNegocioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioRedeSocialServiceTest {

    @Mock
    private UsuarioRedeSocialRepository usuarioRedeSocialRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UsuarioRedeSocialService usuarioRedeSocialService;

    private Usuario usuario;
    private UsuarioRedeSocial redeSocial;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNome("Diego Cordeiro");
        usuario.setLogin("admin");

        redeSocial = UsuarioRedeSocial.builder()
                .usuario(usuario)
                .tipo(TipoRedeSocial.LINKEDIN)
                .url("https://linkedin.com/in/diegocordeiro")
                .identificador("diegocordeiro")
                .ativo(true)
                .build();
        redeSocial.setId(10L);
    }

    @Test
    @DisplayName("Deve listar redes sociais ativas e inativas do usuario")
    void deveListarPorUsuario() {
        when(usuarioRedeSocialRepository.findByUsuarioIdAndDataExclusaoIsNullOrderByTipoAsc(1L))
                .thenReturn(List.of(redeSocial));

        List<UsuarioRedeSocialDTO> resultado = usuarioRedeSocialService.listarPorUsuario(1L);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getTipo()).isEqualTo(TipoRedeSocial.LINKEDIN);
        assertThat(resultado.get(0).getTipoDescricao()).isEqualTo("LinkedIn");
    }

    @Test
    @DisplayName("Deve listar apenas redes sociais ativas do usuario")
    void deveListarAtivasPorUsuario() {
        when(usuarioRedeSocialRepository.buscarAtivasPorUsuario(1L))
                .thenReturn(List.of(redeSocial));

        List<UsuarioRedeSocialDTO> resultado = usuarioRedeSocialService.listarAtivasPorUsuario(1L);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).isAtivo()).isTrue();
    }

    @Test
    @DisplayName("Deve buscar rede social por ID e Usuario")
    void deveBuscarPorIdEUsuario() {
        when(usuarioRedeSocialRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(10L, 1L))
                .thenReturn(Optional.of(redeSocial));

        UsuarioRedeSocialDTO resultado = usuarioRedeSocialService.buscarPorIdEUsuario(10L, 1L);

        assertThat(resultado.getId()).isEqualTo(10L);
        assertThat(resultado.getUrl()).isEqualTo("https://linkedin.com/in/diegocordeiro");
    }

    @Test
    @DisplayName("Deve lançar exceção quando rede social não for encontrada ou pertencer a outro usuário")
    void deveLancarExcecaoQuandoRedeNaoEncontrada() {
        when(usuarioRedeSocialRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(99L, 1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioRedeSocialService.buscarPorIdEUsuario(99L, 1L))
                .isInstanceOf(RegistroNaoEncontradoException.class);
    }

    @Test
    @DisplayName("Deve inserir rede social com sucesso")
    void deveInserirRedeSocial() {
        UsuarioRedeSocialFormDTO form = UsuarioRedeSocialFormDTO.builder()
                .tipo(TipoRedeSocial.GITHUB)
                .url("https://github.com/sgtchacald")
                .identificador("sgtchacald")
                .ativo(true)
                .build();

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRedeSocialRepository.existsByUsuarioIdAndTipoAndDataExclusaoIsNull(1L, TipoRedeSocial.GITHUB))
                .thenReturn(false);
        when(usuarioRedeSocialRepository.save(any(UsuarioRedeSocial.class))).thenAnswer(inv -> {
            UsuarioRedeSocial salvo = inv.getArgument(0);
            salvo.setId(20L);
            return salvo;
        });

        UsuarioRedeSocialDTO resultado = usuarioRedeSocialService.inserir(form, 1L, "admin");

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(20L);
        assertThat(resultado.getTipo()).isEqualTo(TipoRedeSocial.GITHUB);
        assertThat(resultado.getUrl()).isEqualTo("https://github.com/sgtchacald");

        ArgumentCaptor<UsuarioRedeSocial> captor = ArgumentCaptor.forClass(UsuarioRedeSocial.class);
        verify(usuarioRedeSocialRepository).save(captor.capture());
        assertThat(captor.getValue().getCriadoPor()).isEqualTo("admin");
    }

    @Test
    @DisplayName("Deve rejeitar inserção com tipo nulo")
    void deveRejeitarTipoNuloAoInserir() {
        UsuarioRedeSocialFormDTO form = UsuarioRedeSocialFormDTO.builder()
                .tipo(null)
                .url("https://github.com/sgtchacald")
                .build();

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        assertThatThrownBy(() -> usuarioRedeSocialService.inserir(form, 1L, "admin"))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("usuario.rede-social.tipo.obrigatorio");
    }

    @Test
    @DisplayName("Deve rejeitar inserção com tipo já existente para o usuário (RN21)")
    void deveRejeitarTipoDuplicadoAoInserir() {
        UsuarioRedeSocialFormDTO form = UsuarioRedeSocialFormDTO.builder()
                .tipo(TipoRedeSocial.LINKEDIN)
                .url("https://linkedin.com/in/outro")
                .build();

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRedeSocialRepository.existsByUsuarioIdAndTipoAndDataExclusaoIsNull(1L, TipoRedeSocial.LINKEDIN))
                .thenReturn(true);

        assertThatThrownBy(() -> usuarioRedeSocialService.inserir(form, 1L, "admin"))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("usuario.rede-social.tipo.duplicado");
    }

    @Test
    @DisplayName("Deve rejeitar inserção com URL em branco")
    void deveRejeitarUrlEmBrancoAoInserir() {
        UsuarioRedeSocialFormDTO form = UsuarioRedeSocialFormDTO.builder()
                .tipo(TipoRedeSocial.GITHUB)
                .url("   ")
                .build();

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        assertThatThrownBy(() -> usuarioRedeSocialService.inserir(form, 1L, "admin"))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("usuario.rede-social.url.obrigatoria");
    }

    @Test
    @DisplayName("Deve rejeitar inserção com URL inválida sem esquema http/https (RN22)")
    void deveRejeitarUrlInvalidaSemHttp() {
        UsuarioRedeSocialFormDTO form = UsuarioRedeSocialFormDTO.builder()
                .tipo(TipoRedeSocial.GITHUB)
                .url("ftp://github.com/sgtchacald")
                .build();

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        assertThatThrownBy(() -> usuarioRedeSocialService.inserir(form, 1L, "admin"))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("usuario.rede-social.url.invalida");
    }

    @Test
    @DisplayName("Deve editar rede social com sucesso")
    void deveEditarRedeSocial() {
        UsuarioRedeSocialFormDTO form = UsuarioRedeSocialFormDTO.builder()
                .tipo(TipoRedeSocial.LINKEDIN)
                .url("https://linkedin.com/in/diegocordeiro-novo")
                .identificador("diegocordeiro-novo")
                .ativo(false)
                .build();

        when(usuarioRedeSocialRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(10L, 1L))
                .thenReturn(Optional.of(redeSocial));
        when(usuarioRedeSocialRepository.existsByUsuarioIdAndTipoAndIdNotAndDataExclusaoIsNull(1L, TipoRedeSocial.LINKEDIN, 10L))
                .thenReturn(false);
        when(usuarioRedeSocialRepository.save(any(UsuarioRedeSocial.class))).thenAnswer(inv -> inv.getArgument(0));

        UsuarioRedeSocialDTO resultado = usuarioRedeSocialService.editar(10L, form, 1L, "admin");

        assertThat(resultado.getUrl()).isEqualTo("https://linkedin.com/in/diegocordeiro-novo");
        assertThat(resultado.isAtivo()).isFalse();

        ArgumentCaptor<UsuarioRedeSocial> captor = ArgumentCaptor.forClass(UsuarioRedeSocial.class);
        verify(usuarioRedeSocialRepository).save(captor.capture());
        assertThat(captor.getValue().getAlteradoPor()).isEqualTo("admin");
    }

    @Test
    @DisplayName("Deve realizar exclusão lógica (soft delete) da rede social")
    void deveExcluirRedeSocial() {
        when(usuarioRedeSocialRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(10L, 1L))
                .thenReturn(Optional.of(redeSocial));
        when(usuarioRedeSocialRepository.save(any(UsuarioRedeSocial.class))).thenAnswer(inv -> inv.getArgument(0));

        usuarioRedeSocialService.excluir(10L, 1L, "admin");

        ArgumentCaptor<UsuarioRedeSocial> captor = ArgumentCaptor.forClass(UsuarioRedeSocial.class);
        verify(usuarioRedeSocialRepository).save(captor.capture());
        assertThat(captor.getValue().getDataExclusao()).isNotNull();
        assertThat(captor.getValue().getExcluidoPor()).isEqualTo("admin");
    }
}
