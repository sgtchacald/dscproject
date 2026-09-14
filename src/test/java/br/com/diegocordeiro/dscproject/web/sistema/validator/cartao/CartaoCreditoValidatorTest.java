package br.com.diegocordeiro.dscproject.web.sistema.validator.cartao;

import br.com.diegocordeiro.dscproject.dto.cartaocredito.CartaoCreditoFormDTO;
import br.com.diegocordeiro.dscproject.model.conta.Conta;
import br.com.diegocordeiro.dscproject.repository.conta.ContaRepository;
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
class CartaoCreditoValidatorTest {

    @Mock
    private ContaRepository contaRepository;

    @Mock
    private MessageSource messageSource;

    private CartaoCreditoValidator validator;

    @BeforeEach
    void setUp() {
        validator = new CartaoCreditoValidator(contaRepository, messageSource, Locale.getDefault(), 1L);
    }

    private CartaoCreditoFormDTO dtoValido() {
        CartaoCreditoFormDTO dto = new CartaoCreditoFormDTO();
        dto.setDescricao("Nubank Ultravioleta");
        dto.setBandeira("MASTERCARD");
        dto.setFinalCartao("1234");
        dto.setDiaFechamento(3);
        dto.setDiaVencimento(10);
        return dto;
    }

    @Test
    @DisplayName("RN06 - Bandeira fora do domínio (Visa/Mastercard/Elo/Amex) adiciona erro de campo")
    void validate_bandeiraForaDoDominio_deveAdicionarErro() {
        when(messageSource.getMessage(eq("cartao.validacao.bandeira.invalida"), any(), any()))
                .thenReturn("O campo Bandeira é obrigatório.");

        CartaoCreditoFormDTO dto = dtoValido();
        dto.setBandeira("DINERS");

        Errors errors = new BeanPropertyBindingResult(dto, "cartaoCreditoFormDTO");
        validator.validate(dto, errors);

        assertTrue(errors.hasFieldErrors("bandeira"));
    }

    @Test
    @DisplayName("BDD 16.7 - Conta de débito de outro usuário é rejeitada")
    void validate_contaDeOutroUsuario_deveAdicionarErro() {
        when(contaRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(9L, 1L)).thenReturn(Optional.empty());
        when(messageSource.getMessage(eq("msg.cartao.conta.invalida"), any(), any()))
                .thenReturn("A conta de débito informada não é válida.");

        CartaoCreditoFormDTO dto = dtoValido();
        dto.setContaId(9L);

        Errors errors = new BeanPropertyBindingResult(dto, "cartaoCreditoFormDTO");
        validator.validate(dto, errors);

        assertTrue(errors.hasFieldErrors("contaId"));
    }

    @Test
    @DisplayName("RN07 - Conta de débito inativa é rejeitada")
    void validate_contaInativa_deveAdicionarErro() {
        Conta contaInativa = new Conta();
        contaInativa.setId(9L);
        contaInativa.setAtivo(false);

        when(contaRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(9L, 1L)).thenReturn(Optional.of(contaInativa));
        when(messageSource.getMessage(eq("msg.cartao.conta.invalida"), any(), any()))
                .thenReturn("A conta de débito informada não é válida.");

        CartaoCreditoFormDTO dto = dtoValido();
        dto.setContaId(9L);

        Errors errors = new BeanPropertyBindingResult(dto, "cartaoCreditoFormDTO");
        validator.validate(dto, errors);

        assertTrue(errors.hasFieldErrors("contaId"));
    }

    @Test
    @DisplayName("BDD 16.8 - Conta de débito é opcional: sem contaId não adiciona erro")
    void validate_semContaDebito_naoDeveAdicionarErro() {
        CartaoCreditoFormDTO dto = dtoValido();
        dto.setContaId(null);

        Errors errors = new BeanPropertyBindingResult(dto, "cartaoCreditoFormDTO");
        validator.validate(dto, errors);

        assertFalse(errors.hasFieldErrors("contaId"));
    }

    @Test
    @DisplayName("Dados válidos não adicionam erros")
    void validate_quandoDadosValidos_naoDeveAdicionarErros() {
        Conta contaAtiva = new Conta();
        contaAtiva.setId(9L);
        contaAtiva.setAtivo(true);

        when(contaRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(9L, 1L)).thenReturn(Optional.of(contaAtiva));

        CartaoCreditoFormDTO dto = dtoValido();
        dto.setContaId(9L);

        Errors errors = new BeanPropertyBindingResult(dto, "cartaoCreditoFormDTO");
        validator.validate(dto, errors);

        assertFalse(errors.hasErrors());
    }
}
