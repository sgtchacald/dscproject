package br.com.diegocordeiro.dscproject.web.sistema.validator.instituicaofinanceira;

import br.com.diegocordeiro.dscproject.dto.instituicaoprovedor.InstituicaoProvedorFormDTO;
import br.com.diegocordeiro.dscproject.model.instituicaofinanceira.InstituicaoFinanceira;
import br.com.diegocordeiro.dscproject.model.instituicaofinanceira.OpfiProvedor;
import br.com.diegocordeiro.dscproject.repository.instituicaofinanceira.InstituicaoFinanceiraRepository;
import br.com.diegocordeiro.dscproject.repository.instituicaofinanceira.OpfiInstituicaoProvedorRepository;
import br.com.diegocordeiro.dscproject.repository.instituicaofinanceira.OpfiProvedorRepository;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InstituicaoProvedorValidatorTest {

    @Mock
    private OpfiInstituicaoProvedorRepository opfiInstituicaoProvedorRepository;

    @Mock
    private InstituicaoFinanceiraRepository instituicaoFinanceiraRepository;

    @Mock
    private OpfiProvedorRepository opfiProvedorRepository;

    @Mock
    private MessageSource messageSource;

    private InstituicaoProvedorValidator validator;

    @BeforeEach
    void setUp() {
        validator = new InstituicaoProvedorValidator(
                opfiInstituicaoProvedorRepository,
                instituicaoFinanceiraRepository,
                opfiProvedorRepository,
                messageSource,
                Locale.getDefault()
        );
    }

    @Test
    @DisplayName("Validação: par (instituicao, provedor) duplicado adiciona erro")
    void validate_quandoParDuplicado_deveAdicionarErro() {
        when(opfiInstituicaoProvedorRepository.contarPorInstituicaoEProvedor(10L, 1L, null)).thenReturn(1L);
        when(opfiInstituicaoProvedorRepository.contarPorProvedorEIdExterno(1L, "ext_1", null)).thenReturn(0L);
        OpfiProvedor prov = new OpfiProvedor();
        prov.setAtivo(true);
        when(opfiProvedorRepository.findById(1L)).thenReturn(Optional.of(prov));
        when(messageSource.getMessage(eq("msg.instituicaoprovedor.instituicao.duplicada"), any(), any()))
                .thenReturn("Esta instituição já está vinculada a este provedor.");

        InstituicaoProvedorFormDTO dto = new InstituicaoProvedorFormDTO();
        dto.setProvedorId(1L);
        dto.setInstituicaoId(10L);
        dto.setIdExterno("ext_1");

        Errors errors = new BeanPropertyBindingResult(dto, "instituicaoProvedorFormDTO");
        validator.validate(dto, errors);

        assertTrue(errors.hasFieldErrors("instituicaoId"));
    }

    @Test
    @DisplayName("Validação: ID externo duplicado adiciona erro")
    void validate_quandoIdExternoDuplicado_deveAdicionarErro() {
        when(opfiInstituicaoProvedorRepository.contarPorInstituicaoEProvedor(10L, 1L, null)).thenReturn(0L);
        when(opfiInstituicaoProvedorRepository.contarPorProvedorEIdExterno(1L, "ext_1", null)).thenReturn(1L);
        InstituicaoFinanceira inst = new InstituicaoFinanceira();
        inst.setAtivo(true);
        when(instituicaoFinanceiraRepository.findByIdAndDataExclusaoIsNull(10L)).thenReturn(Optional.of(inst));
        OpfiProvedor prov = new OpfiProvedor();
        prov.setAtivo(true);
        when(opfiProvedorRepository.findById(1L)).thenReturn(Optional.of(prov));
        when(messageSource.getMessage(eq("msg.instituicaoprovedor.idexterno.duplicado"), any(), any()))
                .thenReturn("Este identificador já está em uso para este provedor.");

        InstituicaoProvedorFormDTO dto = new InstituicaoProvedorFormDTO();
        dto.setProvedorId(1L);
        dto.setInstituicaoId(10L);
        dto.setIdExterno("ext_1");

        Errors errors = new BeanPropertyBindingResult(dto, "instituicaoProvedorFormDTO");
        validator.validate(dto, errors);

        assertTrue(errors.hasFieldErrors("idExterno"));
    }

    @Test
    @DisplayName("Validação: dados válidos não adicionam erro")
    void validate_quandoValido_naoDeveAdicionarErro() {
        when(opfiInstituicaoProvedorRepository.contarPorInstituicaoEProvedor(10L, 1L, null)).thenReturn(0L);
        when(opfiInstituicaoProvedorRepository.contarPorProvedorEIdExterno(1L, "ext_1", null)).thenReturn(0L);
        InstituicaoFinanceira inst = new InstituicaoFinanceira();
        inst.setAtivo(true);
        when(instituicaoFinanceiraRepository.findByIdAndDataExclusaoIsNull(10L)).thenReturn(Optional.of(inst));
        OpfiProvedor prov = new OpfiProvedor();
        prov.setAtivo(true);
        when(opfiProvedorRepository.findById(1L)).thenReturn(Optional.of(prov));

        InstituicaoProvedorFormDTO dto = new InstituicaoProvedorFormDTO();
        dto.setProvedorId(1L);
        dto.setInstituicaoId(10L);
        dto.setIdExterno("ext_1");

        Errors errors = new BeanPropertyBindingResult(dto, "instituicaoProvedorFormDTO");
        validator.validate(dto, errors);

        assertFalse(errors.hasErrors());
    }
}
