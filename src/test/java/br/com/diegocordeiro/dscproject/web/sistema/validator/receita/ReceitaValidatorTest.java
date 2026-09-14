package br.com.diegocordeiro.dscproject.web.sistema.validator.receita;

import br.com.diegocordeiro.dscproject.model.usuario.Usuario;
import br.com.diegocordeiro.dscproject.dto.receita.ReceitaFormDTO;
import br.com.diegocordeiro.dscproject.enums.AplicaA;
import br.com.diegocordeiro.dscproject.enums.OrigemLancamento;
import br.com.diegocordeiro.dscproject.model.categoria.Categoria;
import br.com.diegocordeiro.dscproject.model.conta.Conta;
import br.com.diegocordeiro.dscproject.model.receita.Receita;
import br.com.diegocordeiro.dscproject.repository.categoria.CategoriaRepository;
import br.com.diegocordeiro.dscproject.repository.conta.ContaRepository;
import br.com.diegocordeiro.dscproject.repository.receita.ReceitaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReceitaValidatorTest {

    @Mock private ContaRepository contaRepository;
    @Mock private CategoriaRepository categoriaRepository;
    @Mock private ReceitaRepository receitaRepository;
    @Mock private MessageSource messageSource;

    private ReceitaValidator validator(Long usuarioId) {
        return new ReceitaValidator(receitaRepository, contaRepository, categoriaRepository, messageSource, Locale.of("pt", "BR"), usuarioId);
    }

    @BeforeEach
    void mensagensPadrao() {
        lenient().when(messageSource.getMessage(any(), any(), any(Locale.class))).thenReturn("mensagem");
    }

    private ReceitaFormDTO dtoValido() {
        ReceitaFormDTO dto = new ReceitaFormDTO();
        dto.setNome("Salário");
        dto.setValor(new BigDecimal("5000.00"));
        dto.setDataLancamento(LocalDate.of(2026, 9, 5));
        dto.setCompetencia("2026-09");
        dto.setContaId(10L);
        dto.setRecebido(false);
        return dto;
    }

    private Conta contaAtiva(Long id, Long usuarioId) {
        Conta c = new Conta();
        c.setId(id);
        c.setAtivo(true);
        var usuario = new br.com.diegocordeiro.dscproject.model.usuario.Usuario();
        usuario.setId(usuarioId);
        c.setUsuario(usuario);
        return c;
    }

    @Test
    @DisplayName("RN03/C3 - Conta inexistente ou de outro usuário é rejeitada")
    void validate_contaInvalida_deveRejeitarContaId() {
        when(contaRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(10L, 1L)).thenReturn(Optional.empty());

        ReceitaFormDTO dto = dtoValido();
        Errors errors = new BeanPropertyBindingResult(dto, "receitaFormDTO");
        validator(1L).validate(dto, errors);

        assertTrue(errors.hasFieldErrors("contaId"));
    }

    @Test
    @DisplayName("RN03/C3 - Conta inativa é rejeitada")
    void validate_contaInativa_deveRejeitarContaId() {
        Conta contaInativa = contaAtiva(10L, 1L);
        contaInativa.setAtivo(false);
        when(contaRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(10L, 1L)).thenReturn(Optional.of(contaInativa));

        ReceitaFormDTO dto = dtoValido();
        Errors errors = new BeanPropertyBindingResult(dto, "receitaFormDTO");
        validator(1L).validate(dto, errors);

        assertTrue(errors.hasFieldErrors("contaId"));
    }

    @Test
    @DisplayName("RN03/C3 - Conta ativa do próprio usuário é aceita")
    void validate_contaValida_naoDeveRejeitar() {
        when(contaRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(10L, 1L)).thenReturn(Optional.of(contaAtiva(10L, 1L)));

        ReceitaFormDTO dto = dtoValido();
        Errors errors = new BeanPropertyBindingResult(dto, "receitaFormDTO");
        validator(1L).validate(dto, errors);

        assertFalse(errors.hasFieldErrors("contaId"));
    }

    @Test
    @DisplayName("RN08 - Editar receita não-MANUAL não valida/rejeita a conta, ainda que o valor enviado seja inválido")
    void validate_editarReceitaOpenFinance_naoValidaConta() {
        Receita existente = new Receita();
        existente.setId(99L);
        existente.setOrigem(OrigemLancamento.OPEN_FINANCE);
        when(receitaRepository.findByIdAndContaUsuarioIdAndDataExclusaoIsNull(99L, 1L)).thenReturn(Optional.of(existente));

        ReceitaFormDTO dto = dtoValido();
        dto.setId(99L);
        dto.setContaId(999L); // conta que nem existe - deve ser ignorada, não rejeitada
        Errors errors = new BeanPropertyBindingResult(dto, "receitaFormDTO");
        validator(1L).validate(dto, errors);

        assertFalse(errors.hasFieldErrors("contaId"));
    }

    @Test
    @DisplayName("C4 - Categoria inativa ou que não aplica a RECEITA é rejeitada")
    void validate_categoriaInvalida_deveRejeitarCategoriaId() {
        when(contaRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(10L, 1L)).thenReturn(Optional.of(contaAtiva(10L, 1L)));

        Categoria categoriaDespesa = new Categoria();
        categoriaDespesa.setId(30L);
        categoriaDespesa.setAtivo(true);
        categoriaDespesa.setAplicaA(AplicaA.DESPESA);
        when(categoriaRepository.findByIdAndDataExclusaoIsNull(30L)).thenReturn(Optional.of(categoriaDespesa));

        ReceitaFormDTO dto = dtoValido();
        dto.setCategoriaId(30L);
        Errors errors = new BeanPropertyBindingResult(dto, "receitaFormDTO");
        validator(1L).validate(dto, errors);

        assertTrue(errors.hasFieldErrors("categoriaId"));
    }

    @Test
    @DisplayName("C4 - Categoria AMBOS ativa é aceita")
    void validate_categoriaAmbosAtiva_naoDeveRejeitar() {
        when(contaRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(10L, 1L)).thenReturn(Optional.of(contaAtiva(10L, 1L)));

        Categoria categoria = new Categoria();
        categoria.setId(30L);
        categoria.setAtivo(true);
        categoria.setAplicaA(AplicaA.AMBOS);
        when(categoriaRepository.findByIdAndDataExclusaoIsNull(30L)).thenReturn(Optional.of(categoria));

        ReceitaFormDTO dto = dtoValido();
        dto.setCategoriaId(30L);
        Errors errors = new BeanPropertyBindingResult(dto, "receitaFormDTO");
        validator(1L).validate(dto, errors);

        assertFalse(errors.hasFieldErrors("categoriaId"));
    }

    @Test
    @DisplayName("RN06 / MSG09 - Recebido=true sem data de recebimento é rejeitado")
    void validate_recebidoSemData_deveRejeitarDataRecebimento() {
        when(contaRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(10L, 1L)).thenReturn(Optional.of(contaAtiva(10L, 1L)));

        ReceitaFormDTO dto = dtoValido();
        dto.setRecebido(true);
        dto.setDataRecebimento(null);
        Errors errors = new BeanPropertyBindingResult(dto, "receitaFormDTO");
        validator(1L).validate(dto, errors);

        assertTrue(errors.hasFieldErrors("dataRecebimento"));
    }

    @Test
    @DisplayName("RN06 - Recebido=true com data preenchida não é rejeitado")
    void validate_recebidoComData_naoDeveRejeitar() {
        when(contaRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(10L, 1L)).thenReturn(Optional.of(contaAtiva(10L, 1L)));

        ReceitaFormDTO dto = dtoValido();
        dto.setRecebido(true);
        dto.setDataRecebimento(LocalDate.of(2026, 9, 6));
        Errors errors = new BeanPropertyBindingResult(dto, "receitaFormDTO");
        validator(1L).validate(dto, errors);

        assertFalse(errors.hasFieldErrors("dataRecebimento"));
    }
}
