package br.com.diegocordeiro.dscproject.web.sistema.validator.instituicaofinanceira;

import br.com.diegocordeiro.dscproject.dto.instituicaofinanceira.InstituicaoFinanceiraFormDTO;
import br.com.diegocordeiro.dscproject.enums.TipoInstituicaoFinanceira;
import br.com.diegocordeiro.dscproject.repository.instituicaofinanceira.InstituicaoFinanceiraRepository;
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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InstituicaoFinanceiraValidatorTest {

    @Mock
    private InstituicaoFinanceiraRepository instituicaoFinanceiraRepository;

    @Mock
    private MessageSource messageSource;

    private InstituicaoFinanceiraValidator validator;

    @BeforeEach
    void setUp() {
        validator = new InstituicaoFinanceiraValidator(instituicaoFinanceiraRepository, messageSource, Locale.getDefault());
    }

    @Test
    @DisplayName("Validação: nome duplicado adiciona erro")
    void validate_quandoNomeDuplicado_deveAdicionarErro() {
        when(instituicaoFinanceiraRepository.contarPorNome("Banco Nubank", null)).thenReturn(1L);
        when(messageSource.getMessage(eq("msg.instituicao.nome.duplicado"), any(), any()))
                .thenReturn("Já existe uma instituição com este nome.");

        InstituicaoFinanceiraFormDTO dto = new InstituicaoFinanceiraFormDTO();
        dto.setNome("Banco Nubank");
        dto.setTipo(TipoInstituicaoFinanceira.BANCO);

        Errors errors = new BeanPropertyBindingResult(dto, "instituicaoFinanceiraFormDTO");
        validator.validate(dto, errors);

        assertTrue(errors.hasFieldErrors("nome"));
    }

    @Test
    @DisplayName("Validação: código duplicado adiciona erro")
    void validate_quandoCodigoDuplicado_deveAdicionarErro() {
        when(instituicaoFinanceiraRepository.contarPorNome("Novo Banco", null)).thenReturn(0L);
        when(instituicaoFinanceiraRepository.contarPorCodigo("001", null)).thenReturn(1L);
        when(messageSource.getMessage(eq("msg.instituicao.codigo.duplicado"), any(), any()))
                .thenReturn("Já existe uma instituição com este código.");

        InstituicaoFinanceiraFormDTO dto = new InstituicaoFinanceiraFormDTO();
        dto.setNome("Novo Banco");
        dto.setCodigo("001");
        dto.setTipo(TipoInstituicaoFinanceira.BANCO);

        Errors errors = new BeanPropertyBindingResult(dto, "instituicaoFinanceiraFormDTO");
        validator.validate(dto, errors);

        assertTrue(errors.hasFieldErrors("codigo"));
    }

    @Test
    @DisplayName("Validação: dados válidos não adicionam erro")
    void validate_quandoValido_naoDeveAdicionarErro() {
        when(instituicaoFinanceiraRepository.contarPorNome("Banco Inédito", null)).thenReturn(0L);
        when(instituicaoFinanceiraRepository.contarPorCodigo("888", null)).thenReturn(0L);

        InstituicaoFinanceiraFormDTO dto = new InstituicaoFinanceiraFormDTO();
        dto.setNome("Banco Inédito");
        dto.setCodigo("888");
        dto.setTipo(TipoInstituicaoFinanceira.BANCO);

        Errors errors = new BeanPropertyBindingResult(dto, "instituicaoFinanceiraFormDTO");
        validator.validate(dto, errors);

        assertFalse(errors.hasErrors());
    }
}
