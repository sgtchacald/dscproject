package br.com.diegocordeiro.dscproject.web.sistema.validator.categoria;

import br.com.diegocordeiro.dscproject.dto.categoria.CategoriaFormDTO;
import br.com.diegocordeiro.dscproject.repository.categoria.CategoriaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoriaValidatorTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private MessageSource messageSource;

    private CategoriaValidator validator;

    @BeforeEach
    void setUp() {
        validator = new CategoriaValidator(categoriaRepository, messageSource, Locale.getDefault());
    }

    @Test
    @DisplayName("Validação de unicidade de código")
    void validate_quandoCodigoDuplicado_deveAdicionarErro() {
        when(categoriaRepository.contarPorCodigo("ALIMENTACAO", null)).thenReturn(1L);
        when(messageSource.getMessage(eq("categoria.codigo.duplicado"), any(), any()))
            .thenReturn("Já existe uma categoria com este código.");

        CategoriaFormDTO dto = new CategoriaFormDTO();
        dto.setCodigo("Alimentação");

        Errors errors = new BeanPropertyBindingResult(dto, "categoriaForm");
        validator.validate(dto, errors);

        assertTrue(errors.hasFieldErrors("codigo"));
    }

    @Test
    @DisplayName("Validação com código único aceita")
    void validate_quandoCodigoUnico_naoDeveAdicionarErro() {
        when(categoriaRepository.contarPorCodigo("PETS", null)).thenReturn(0L);

        CategoriaFormDTO dto = new CategoriaFormDTO();
        dto.setCodigo("Pets");

        Errors errors = new BeanPropertyBindingResult(dto, "categoriaForm");
        validator.validate(dto, errors);

        assertFalse(errors.hasFieldErrors("codigo"));
    }
}
