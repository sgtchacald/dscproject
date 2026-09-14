package br.com.diegocordeiro.dscproject.web.sistema.validator.usuario;

import br.com.diegocordeiro.dscproject.dto.minhaconta.MinhaContaDTO;
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
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MinhaContaValidatorTest {

    private static final Long ID_ATUAL = 9L;

    @Mock
    private UsuarioService service;
    @Mock
    private MessageSource messageSource;

    private MinhaContaValidator validator;

    @BeforeEach
    void init() {
        lenient().when(messageSource.getMessage(anyString(), any(), any()))
            .thenAnswer(i -> i.getArgument(0));
        lenient().when(service.verificarSeExiste(any(), any())).thenReturn(false);
        validator = new MinhaContaValidator(service, messageSource, Locale.forLanguageTag("pt-BR"), ID_ATUAL);
    }

    private MinhaContaDTO dto() {
        MinhaContaDTO dto = new MinhaContaDTO();
        dto.setNome("Diego");
        dto.setGenero(Genero.MASCULINO);
        dto.setNascimento(LocalDate.of(1986, 5, 20));
        dto.setEmail("diego@test.com");
        dto.setLogin("diego");
        return dto;
    }

    private Errors errosDe(MinhaContaDTO dto) {
        Errors errors = new BeanPropertyBindingResult(dto, "minhaContaDTO");
        validator.validate(dto, errors);
        return errors;
    }

    @Test
    void senhaVazia_naoExigeConfirmacao() {
        Errors errors = errosDe(dto());

        assertThat(errors.hasErrors()).isFalse();
    }

    @Test
    void senhaPreenchidaSemConfirmacaoIgual_erroDeNegocioDiffer() {
        MinhaContaDTO dto = dto();
        dto.setSenha("senha123");
        dto.setConfirmacaoSenha("outra");

        Errors errors = errosDe(dto);

        assertThat(errors.getFieldError("confirmacaoSenha").getCode())
            .isEqualTo("Differ.minhaContaDTO.confirmacaoSenha");
    }

    @Test
    void emailDeOutroUsuario_erroDuplicateIgnorandoOProprioId() {
        when(service.verificarSeExiste(eq("diego@test.com"), eq(ID_ATUAL))).thenReturn(true);

        Errors errors = errosDe(dto());

        assertThat(errors.getFieldError("email").getCode()).isEqualTo("Duplicate.minhaContaDTO.email");
    }

    @Test
    void loginDeOutroUsuario_erroDuplicate() {
        when(service.verificarSeExiste(eq("diego"), eq(ID_ATUAL))).thenReturn(true);

        Errors errors = errosDe(dto());

        assertThat(errors.getFieldError("login").getCode()).isEqualTo("Duplicate.minhaContaDTO.login");
    }
}
