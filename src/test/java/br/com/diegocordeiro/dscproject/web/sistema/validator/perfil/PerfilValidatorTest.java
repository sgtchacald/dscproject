package br.com.diegocordeiro.dscproject.web.sistema.validator.perfil;

import br.com.diegocordeiro.dscproject.dto.perfil.PerfilFormDTO;
import br.com.diegocordeiro.dscproject.service.perfil.PerfilService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PerfilValidatorTest {

    @Mock
    private PerfilService service;
    @Mock
    private MessageSource messageSource;

    private PerfilValidator validator;

    @BeforeEach
    void init() {
        lenient().when(messageSource.getMessage(anyString(), any(), any()))
            .thenAnswer(i -> i.getArgument(0));
        lenient().when(service.verificarCodigoDuplicado(any(), any())).thenReturn(false);
        validator = new PerfilValidator(service, messageSource, Locale.forLanguageTag("pt-BR"));
    }

    private PerfilFormDTO dto() {
        PerfilFormDTO dto = new PerfilFormDTO();
        dto.setCodigo("RELATORIOS");
        dto.setNome("Relatórios");
        return dto;
    }

    private Errors errosDe(PerfilFormDTO dto) {
        Errors errors = new BeanPropertyBindingResult(dto, "perfilFormDTO");
        validator.validate(dto, errors);
        return errors;
    }

    // ---------- RN03 / BDD 16.2 ----------

    @Test
    void codigoDuplicado_erroDeNegocioDuplicate() {
        when(service.verificarCodigoDuplicado(eq("RELATORIOS"), isNull())).thenReturn(true);

        Errors errors = errosDe(dto());

        assertThat(errors.getFieldError("codigo")).isNotNull();
        assertThat(errors.getFieldError("codigo").getCode()).isEqualTo("Duplicate.perfilFormDTO.codigo");
    }

    @Test
    void codigoLivre_semErro() {
        assertThat(errosDe(dto()).hasErrors()).isFalse();
    }

    @Test
    void edicao_codigoDeOutroPerfil_usaIdAtualNaChecagem() {
        PerfilFormDTO dto = dto();
        dto.setId(7L);
        when(service.verificarCodigoDuplicado("RELATORIOS", 7L)).thenReturn(true);

        assertThat(errosDe(dto).getFieldError("codigo").getCode()).isEqualTo("Duplicate.perfilFormDTO.codigo");
    }
}
