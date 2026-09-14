package br.com.diegocordeiro.dscproject.web.sistema.validator.conta;

import br.com.diegocordeiro.dscproject.dto.conta.ContaFormDTO;
import br.com.diegocordeiro.dscproject.enums.TipoConta;
import br.com.diegocordeiro.dscproject.model.conta.Conta;
import br.com.diegocordeiro.dscproject.model.instituicaofinanceira.InstituicaoFinanceira;
import br.com.diegocordeiro.dscproject.repository.conta.ContaRepository;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContaValidatorTest {

    @Mock
    private ContaRepository contaRepository;

    @Mock
    private InstituicaoFinanceiraRepository instituicaoFinanceiraRepository;

    @Mock
    private MessageSource messageSource;

    private ContaValidator validator;

    @BeforeEach
    void setUp() {
        validator = new ContaValidator(contaRepository, instituicaoFinanceiraRepository, messageSource, Locale.getDefault(), 1L);
    }

    @Test
    @DisplayName("Validação: descrição duplicada para o mesmo usuário adiciona erro")
    void validate_quandoDescricaoDuplicada_deveAdicionarErro() {
        when(contaRepository.contarPorUsuarioEDescricao(1L, "Conta Corrente", null)).thenReturn(1L);
        when(messageSource.getMessage(eq("msg.conta.descricao.duplicada"), any(), any()))
                .thenReturn("Já existe uma conta com esta descrição.");

        ContaFormDTO dto = new ContaFormDTO();
        dto.setDescricao("Conta Corrente");
        dto.setInstituicaoId(10L);
        dto.setTipo(TipoConta.CORRENTE);

        Errors errors = new BeanPropertyBindingResult(dto, "contaFormDTO");
        validator.validate(dto, errors);

        assertTrue(errors.hasFieldErrors("descricao"));
    }

    @Test
    @DisplayName("Validação: instituição inativa na criação adiciona erro")
    void validate_quandoInstituicaoInativa_deveAdicionarErro() {
        when(contaRepository.contarPorUsuarioEDescricao(1L, "Conta Nova", null)).thenReturn(0L);

        InstituicaoFinanceira inst = new InstituicaoFinanceira();
        inst.setId(10L);
        inst.setAtivo(false);

        when(instituicaoFinanceiraRepository.findByIdAndDataExclusaoIsNull(10L)).thenReturn(Optional.of(inst));
        when(messageSource.getMessage(eq("msg.conta.instituicao.invalida"), any(), any()))
                .thenReturn("A instituição financeira selecionada está inativa.");

        ContaFormDTO dto = new ContaFormDTO();
        dto.setDescricao("Conta Nova");
        dto.setInstituicaoId(10L);
        dto.setTipo(TipoConta.CORRENTE);

        Errors errors = new BeanPropertyBindingResult(dto, "contaFormDTO");
        validator.validate(dto, errors);

        assertTrue(errors.hasFieldErrors("instituicaoId"));
    }

    @Test
    @DisplayName("Validação: tentativa de alterar instituição na edição adiciona erro")
    void validate_quandoAlteraInstituicaoNaEdicao_deveAdicionarErro() {
        InstituicaoFinanceira instOriginal = new InstituicaoFinanceira();
        instOriginal.setId(10L);

        Conta conta = new Conta();
        conta.setId(5L);
        conta.setInstituicao(instOriginal);

        when(contaRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(5L, 1L)).thenReturn(Optional.of(conta));
        when(messageSource.getMessage(eq("msg.conta.instituicao.imutavel"), any(), any()))
                .thenReturn("A instituição financeira não pode ser alterada após a criação da conta.");

        ContaFormDTO dto = new ContaFormDTO();
        dto.setId(5L);
        dto.setDescricao("Conta Editada");
        dto.setInstituicaoId(20L); // diferente da original 10L
        dto.setTipo(TipoConta.CORRENTE);

        Errors errors = new BeanPropertyBindingResult(dto, "contaFormDTO");
        validator.validate(dto, errors);

        assertTrue(errors.hasFieldErrors("instituicaoId"));
    }

    @Test
    @DisplayName("Validação: dados válidos não adicionam erros")
    void validate_quandoDadosValidos_naoDeveAdicionarErros() {
        when(contaRepository.contarPorUsuarioEDescricao(1L, "Conta Investimentos", null)).thenReturn(0L);

        InstituicaoFinanceira inst = new InstituicaoFinanceira();
        inst.setId(15L);
        inst.setAtivo(true);

        when(instituicaoFinanceiraRepository.findByIdAndDataExclusaoIsNull(15L)).thenReturn(Optional.of(inst));

        ContaFormDTO dto = new ContaFormDTO();
        dto.setDescricao("Conta Investimentos");
        dto.setInstituicaoId(15L);
        dto.setTipo(TipoConta.INVESTIMENTO);

        Errors errors = new BeanPropertyBindingResult(dto, "contaFormDTO");
        validator.validate(dto, errors);

        assertFalse(errors.hasErrors());
    }
}
