package br.com.diegocordeiro.dscproject.web.sistema.validator.usuario;

import br.com.diegocordeiro.dscproject.dto.usuario.UsuarioDTO;
import br.com.diegocordeiro.dscproject.enums.Genero;
import br.com.diegocordeiro.dscproject.service.usuario.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

import java.time.LocalDate;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioValidatorTest {

    @Mock
    private UsuarioService service;
    @Mock
    private MessageSource messageSource;

    private UsuarioValidator validator;

    @BeforeEach
    void init() {
        lenient().when(messageSource.getMessage(anyString(), any(), any()))
            .thenAnswer(i -> i.getArgument(0));
        lenient().when(service.verificarSeExiste(any(), any())).thenReturn(false);
        validator = new UsuarioValidator(service, messageSource, Locale.forLanguageTag("pt-BR"));
    }

    private UsuarioDTO dto() {
        UsuarioDTO dto = new UsuarioDTO();
        dto.setNome("Diego");
        dto.setGenero(Genero.MASCULINO);
        dto.setNascimento(LocalDate.of(1986, 5, 20));
        dto.setEmail("diego@test.com");
        dto.setLogin("diego");
        dto.setSenha("senha123");
        dto.setConfirmacaoSenha("senha123");
        dto.setPerfilCodigo("USER");
        return dto;
    }

    private Errors errosDe(UsuarioDTO dto) {
        Errors errors = new BeanPropertyBindingResult(dto, "usuarioDTO");
        validator.validate(dto, errors);
        return errors;
    }

    @Test
    void criacao_semSenha_acusaSenhaObrigatoria() {
        UsuarioDTO dto = dto();
        dto.setSenha(null);
        dto.setConfirmacaoSenha(null);

        Errors errors = errosDe(dto);

        assertThat(errors.getFieldError("senha")).isNotNull();
        assertThat(errors.getFieldError("senha").getCode()).isEqualTo("NotBlank.usuarioDTO.senha");
    }

    @Test
    void senhaDiferenteDaConfirmacao_erroDeNegocioDiffer() {
        UsuarioDTO dto = dto();
        dto.setConfirmacaoSenha("outra");

        Errors errors = errosDe(dto);

        assertThat(errors.getFieldError("confirmacaoSenha").getCode())
            .isEqualTo("Differ.usuarioDTO.confirmacaoSenha");
    }

    @Test
    void loginDuplicado_erroDeNegocioDuplicate() {
        when(service.verificarSeExiste(eq("diego"), isNull())).thenReturn(true);

        Errors errors = errosDe(dto());

        assertThat(errors.getFieldError("login").getCode()).isEqualTo("Duplicate.usuarioDTO.login");
    }

    @Test
    void emailDuplicado_erroDeNegocioDuplicate() {
        when(service.verificarSeExiste(eq("diego@test.com"), isNull())).thenReturn(true);

        Errors errors = errosDe(dto());

        assertThat(errors.getFieldError("email").getCode()).isEqualTo("Duplicate.usuarioDTO.email");
    }

    @Test
    void edicao_semSenha_naoAcusaObrigatoriedade() {
        UsuarioDTO dto = dto();
        dto.setId(9L);
        dto.setSenha(null);
        dto.setConfirmacaoSenha(null);

        Errors errors = errosDe(dto);

        assertThat(errors.hasErrors()).isFalse();
    }

    @Test
    void edicao_loginDeOutroUsuario_erroDuplicateUsandoIdAtual() {
        UsuarioDTO dto = dto();
        dto.setId(9L);
        dto.setSenha(null);
        dto.setConfirmacaoSenha(null);
        when(service.verificarSeExiste("diego", 9L)).thenReturn(true);

        Errors errors = errosDe(dto);

        assertThat(errors.getFieldError("login").getCode()).isEqualTo("Duplicate.usuarioDTO.login");
    }
}
