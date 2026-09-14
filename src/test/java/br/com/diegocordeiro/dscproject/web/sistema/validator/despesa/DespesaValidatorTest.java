package br.com.diegocordeiro.dscproject.web.sistema.validator.despesa;

import br.com.diegocordeiro.dscproject.dto.despesa.DespesaFormDTO;
import br.com.diegocordeiro.dscproject.dto.despesa.DespesaRateioDTO;
import br.com.diegocordeiro.dscproject.enums.AplicaA;
import br.com.diegocordeiro.dscproject.enums.MeioPagamento;
import br.com.diegocordeiro.dscproject.enums.OrigemLancamento;
import br.com.diegocordeiro.dscproject.enums.StatusPagamento;
import br.com.diegocordeiro.dscproject.enums.TipoConta;
import br.com.diegocordeiro.dscproject.enums.StatusContato;
import br.com.diegocordeiro.dscproject.enums.TipoContato;
import br.com.diegocordeiro.dscproject.model.cartao.CartaoCredito;
import br.com.diegocordeiro.dscproject.model.categoria.Categoria;
import br.com.diegocordeiro.dscproject.model.conta.Conta;
import br.com.diegocordeiro.dscproject.model.contato.Contato;
import br.com.diegocordeiro.dscproject.model.despesa.Despesa;
import br.com.diegocordeiro.dscproject.model.usuario.Usuario;
import br.com.diegocordeiro.dscproject.repository.cartao.CartaoCreditoRepository;
import br.com.diegocordeiro.dscproject.repository.categoria.CategoriaRepository;
import br.com.diegocordeiro.dscproject.repository.conta.ContaRepository;
import br.com.diegocordeiro.dscproject.repository.contato.ContatoRepository;
import br.com.diegocordeiro.dscproject.repository.despesa.DespesaRepository;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DespesaValidatorTest {

    @Mock private DespesaRepository despesaRepository;
    @Mock private ContaRepository contaRepository;
    @Mock private CartaoCreditoRepository cartaoCreditoRepository;
    @Mock private CategoriaRepository categoriaRepository;
    @Mock private ContatoRepository contatoRepository;
    @Mock private MessageSource messageSource;

    private DespesaValidator validator(Long usuarioId, boolean podeRatear) {
        return new DespesaValidator(
                despesaRepository,
                contaRepository,
                cartaoCreditoRepository,
                categoriaRepository,
                contatoRepository,
                messageSource,
                Locale.of("pt", "BR"),
                usuarioId,
                podeRatear);
    }

    @BeforeEach
    void mensagensPadrao() {
        lenient().when(messageSource.getMessage(any(), any(), any(Locale.class))).thenReturn("mensagem");
    }

    private DespesaFormDTO dtoValido() {
        DespesaFormDTO dto = new DespesaFormDTO();
        dto.setNome("Supermercado");
        dto.setValor(new BigDecimal("250.00"));
        dto.setDataLancamento(LocalDate.of(2026, 9, 5));
        dto.setCompetencia("2026-09");
        dto.setFormaPagamento("CONTA");
        dto.setContaId(10L);
        dto.setMeioPagamento(MeioPagamento.DEBITO);
        dto.setStatusPagamento(StatusPagamento.NAO);
        dto.setParcelada(false);
        return dto;
    }

    private Conta contaAtiva(Long id, Long usuarioId, TipoConta tipo) {
        Conta c = new Conta();
        c.setId(id);
        c.setDescricao("Conta Corrente");
        c.setTipo(tipo);
        c.setAtivo(true);
        Usuario u = new Usuario();
        u.setId(usuarioId);
        c.setUsuario(u);
        return c;
    }

    private CartaoCredito cartaoAtivo(Long id, Long usuarioId) {
        CartaoCredito cc = new CartaoCredito();
        cc.setId(id);
        cc.setDescricao("Nubank Platinum");
        cc.setAtivo(true);
        Usuario u = new Usuario();
        u.setId(usuarioId);
        cc.setUsuario(u);
        return cc;
    }

    private Contato contatoAtivo(Long id, Long usuarioDonoId) {
        Contato c = new Contato();
        c.setId(id);
        c.setNome("Contato Teste");
        c.setTipo(TipoContato.EXTERNO);
        c.setStatus(StatusContato.ATIVO);
        Usuario dono = new Usuario();
        dono.setId(usuarioDonoId);
        c.setUsuarioDono(dono);
        return c;
    }

    @Test
    @DisplayName("RN03/C3 - Conta inexistente ou de outro usuário é rejeitada")
    void validate_contaInvalida_deveRejeitarContaId() {
        when(contaRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(10L, 1L)).thenReturn(Optional.empty());

        DespesaFormDTO dto = dtoValido();
        Errors errors = new BeanPropertyBindingResult(dto, "despesaFormDTO");
        validator(1L, false).validate(dto, errors);

        assertTrue(errors.hasFieldErrors("contaId"));
    }

    @Test
    @DisplayName("RN03/C3 - Conta inativa é rejeitada")
    void validate_contaInativa_deveRejeitarContaId() {
        Conta c = contaAtiva(10L, 1L, TipoConta.CORRENTE);
        c.setAtivo(false);
        when(contaRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(10L, 1L)).thenReturn(Optional.of(c));

        DespesaFormDTO dto = dtoValido();
        Errors errors = new BeanPropertyBindingResult(dto, "despesaFormDTO");
        validator(1L, false).validate(dto, errors);

        assertTrue(errors.hasFieldErrors("contaId"));
    }

    @Test
    @DisplayName("RN03/C3 - Conta ativa do próprio usuário é aceita")
    void validate_contaValida_naoDeveRejeitar() {
        when(contaRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(10L, 1L)).thenReturn(Optional.of(contaAtiva(10L, 1L, TipoConta.CORRENTE)));

        DespesaFormDTO dto = dtoValido();
        Errors errors = new BeanPropertyBindingResult(dto, "despesaFormDTO");
        validator(1L, false).validate(dto, errors);

        assertFalse(errors.hasFieldErrors("contaId"));
    }

    @Test
    @DisplayName("RN03/C4 - Cartão inexistente ou de outro usuário é rejeitado")
    void validate_cartaoInvalido_deveRejeitarCartaoId() {
        when(cartaoCreditoRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(20L, 1L)).thenReturn(Optional.empty());

        DespesaFormDTO dto = dtoValido();
        dto.setFormaPagamento("CARTAO");
        dto.setContaId(null);
        dto.setCartaoId(20L);
        Errors errors = new BeanPropertyBindingResult(dto, "despesaFormDTO");
        validator(1L, false).validate(dto, errors);

        assertTrue(errors.hasFieldErrors("cartaoId"));
    }

    @Test
    @DisplayName("RN03/C4 - Cartão inativo é rejeitado")
    void validate_cartaoInativo_deveRejeitarCartaoId() {
        CartaoCredito cc = cartaoAtivo(20L, 1L);
        cc.setAtivo(false);
        when(cartaoCreditoRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(20L, 1L)).thenReturn(Optional.of(cc));

        DespesaFormDTO dto = dtoValido();
        dto.setFormaPagamento("CARTAO");
        dto.setContaId(null);
        dto.setCartaoId(20L);
        Errors errors = new BeanPropertyBindingResult(dto, "despesaFormDTO");
        validator(1L, false).validate(dto, errors);

        assertTrue(errors.hasFieldErrors("cartaoId"));
    }

    @Test
    @DisplayName("RN03/C4 - Cartão ativo do usuário é aceito")
    void validate_cartaoValido_naoDeveRejeitar() {
        when(cartaoCreditoRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(20L, 1L)).thenReturn(Optional.of(cartaoAtivo(20L, 1L)));

        DespesaFormDTO dto = dtoValido();
        dto.setFormaPagamento("CARTAO");
        dto.setContaId(null);
        dto.setCartaoId(20L);
        Errors errors = new BeanPropertyBindingResult(dto, "despesaFormDTO");
        validator(1L, false).validate(dto, errors);

        assertFalse(errors.hasFieldErrors("cartaoId"));
    }

    @Test
    @DisplayName("RN03 - Dinheiro exige conta do tipo CARTEIRA")
    void validate_dinheiroSemCarteira_deveRejeitar() {
        Conta c = contaAtiva(10L, 1L, TipoConta.CORRENTE);
        when(contaRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(10L, 1L)).thenReturn(Optional.of(c));

        DespesaFormDTO dto = dtoValido();
        dto.setFormaPagamento("DINHEIRO");
        Errors errors = new BeanPropertyBindingResult(dto, "despesaFormDTO");
        validator(1L, false).validate(dto, errors);

        assertTrue(errors.hasFieldErrors("contaId"));
    }

    @Test
    @DisplayName("RN03 - Dinheiro com conta do tipo CARTEIRA é aceito")
    void validate_dinheiroComCarteira_deveAceitar() {
        Conta c = contaAtiva(10L, 1L, TipoConta.CARTEIRA);
        when(contaRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(10L, 1L)).thenReturn(Optional.of(c));

        DespesaFormDTO dto = dtoValido();
        dto.setFormaPagamento("DINHEIRO");
        Errors errors = new BeanPropertyBindingResult(dto, "despesaFormDTO");
        validator(1L, false).validate(dto, errors);

        assertFalse(errors.hasFieldErrors("contaId"));
    }

    @Test
    @DisplayName("RN12 - Parcelada exige qtdParcelas entre 2 e 72")
    void validate_parceladaSemQtdOuInvalida_deveRejeitar() {
        when(contaRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(10L, 1L)).thenReturn(Optional.of(contaAtiva(10L, 1L, TipoConta.CORRENTE)));

        DespesaFormDTO dto = dtoValido();
        dto.setParcelada(true);
        dto.setQtdParcelas(1);

        Errors errors = new BeanPropertyBindingResult(dto, "despesaFormDTO");
        validator(1L, false).validate(dto, errors);
        assertTrue(errors.hasFieldErrors("qtdParcelas"));

        dto.setQtdParcelas(73);
        errors = new BeanPropertyBindingResult(dto, "despesaFormDTO");
        validator(1L, false).validate(dto, errors);
        assertTrue(errors.hasFieldErrors("qtdParcelas"));

        dto.setQtdParcelas(12);
        errors = new BeanPropertyBindingResult(dto, "despesaFormDTO");
        validator(1L, false).validate(dto, errors);
        assertFalse(errors.hasFieldErrors("qtdParcelas"));
    }

    @Test
    @DisplayName("RN06 - StatusPagamento=SIM exige dataPagamento")
    void validate_statusSimSemData_deveRejeitarDataPagamento() {
        when(contaRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(10L, 1L)).thenReturn(Optional.of(contaAtiva(10L, 1L, TipoConta.CORRENTE)));

        DespesaFormDTO dto = dtoValido();
        dto.setStatusPagamento(StatusPagamento.SIM);
        dto.setDataPagamento(null);

        Errors errors = new BeanPropertyBindingResult(dto, "despesaFormDTO");
        validator(1L, false).validate(dto, errors);
        assertTrue(errors.hasFieldErrors("dataPagamento"));
    }

    @Test
    @DisplayName("RN15 - Rateio enviado por usuário sem permissão é rejeitado")
    void validate_rateioSemPermissao_deveRejeitar() {
        when(contaRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(10L, 1L)).thenReturn(Optional.of(contaAtiva(10L, 1L, TipoConta.CORRENTE)));

        DespesaFormDTO dto = dtoValido();
        dto.setRateio(List.of(new DespesaRateioDTO(2L, "Amigo", new BigDecimal("50.00"), StatusPagamento.NAO, null)));

        Errors errors = new BeanPropertyBindingResult(dto, "despesaFormDTO");
        validator(1L, false).validate(dto, errors);
        assertTrue(errors.hasFieldErrors("rateio"));
    }

    @Test
    @DisplayName("RN17 - Rateio com soma das fatias maior que o valor da despesa é rejeitado")
    void validate_rateioSomaExcede_deveRejeitar() {
        when(contaRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(10L, 1L)).thenReturn(Optional.of(contaAtiva(10L, 1L, TipoConta.CORRENTE)));
        when(contatoRepository.findByIdAndUsuarioDonoIdAndDataExclusaoIsNull(2L, 1L)).thenReturn(Optional.of(contatoAtivo(2L, 1L)));

        DespesaFormDTO dto = dtoValido();
        dto.setValor(new BigDecimal("100.00"));
        dto.setRateio(List.of(new DespesaRateioDTO(2L, "Amigo", new BigDecimal("150.00"), StatusPagamento.NAO, null)));

        Errors errors = new BeanPropertyBindingResult(dto, "despesaFormDTO");
        validator(1L, true).validate(dto, errors);
        assertTrue(errors.hasFieldErrors("rateio"));
    }

    @Test
    @DisplayName("RN16/RN19 - Rateio com contato inexistente ou de outro usuário é rejeitado")
    void validate_rateioComContatoDeOutroUsuario_deveRejeitar() {
        when(contaRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(10L, 1L)).thenReturn(Optional.of(contaAtiva(10L, 1L, TipoConta.CORRENTE)));
        when(contatoRepository.findByIdAndUsuarioDonoIdAndDataExclusaoIsNull(999L, 1L)).thenReturn(Optional.empty());

        DespesaFormDTO dto = dtoValido();
        dto.setValor(new BigDecimal("100.00"));
        dto.setRateio(List.of(new DespesaRateioDTO(999L, "Desconhecido", new BigDecimal("50.00"), StatusPagamento.NAO, null)));

        Errors errors = new BeanPropertyBindingResult(dto, "despesaFormDTO");
        validator(1L, true).validate(dto, errors);
        assertTrue(errors.hasFieldErrors("rateio[0].contatoId"));
    }

    @Test
    @DisplayName("RN20 - Rateio com contato duplicado é rejeitado")
    void validate_rateioComContatoDuplicado_deveRejeitar() {
        when(contaRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(10L, 1L)).thenReturn(Optional.of(contaAtiva(10L, 1L, TipoConta.CORRENTE)));
        when(contatoRepository.findByIdAndUsuarioDonoIdAndDataExclusaoIsNull(2L, 1L)).thenReturn(Optional.of(contatoAtivo(2L, 1L)));

        DespesaFormDTO dto = dtoValido();
        dto.setValor(new BigDecimal("100.00"));
        dto.setRateio(List.of(
                new DespesaRateioDTO(2L, "Amigo", new BigDecimal("30.00"), StatusPagamento.NAO, null),
                new DespesaRateioDTO(2L, "Amigo", new BigDecimal("30.00"), StatusPagamento.NAO, null)
        ));

        Errors errors = new BeanPropertyBindingResult(dto, "despesaFormDTO");
        validator(1L, true).validate(dto, errors);
        assertTrue(errors.hasFieldErrors("rateio[1].contatoId"));
    }

    @Test
    @DisplayName("RN08 - Despesa importada não valida conta nem cartão na edição")
    void validate_despesaImportada_naoValidaConta() {
        Despesa d = new Despesa();
        d.setId(99L);
        d.setOrigem(OrigemLancamento.OPEN_FINANCE);
        when(despesaRepository.buscarPorIdEUsuario(99L, 1L)).thenReturn(Optional.of(d));

        DespesaFormDTO dto = dtoValido();
        dto.setId(99L);
        dto.setContaId(999L);

        Errors errors = new BeanPropertyBindingResult(dto, "despesaFormDTO");
        validator(1L, false).validate(dto, errors);
        assertFalse(errors.hasFieldErrors("contaId"));
    }

    @Test
    @DisplayName("RN25 - Despesa recorrente válida deve passar na validação")
    void validate_recorrenteValida_devePassar() {
        when(contaRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(10L, 1L)).thenReturn(Optional.of(contaAtiva(10L, 1L, TipoConta.CORRENTE)));

        DespesaFormDTO dto = dtoValido();
        dto.setRecorrente(true);
        dto.setQtdMesesRecorrencia(12);

        Errors errors = new BeanPropertyBindingResult(dto, "despesaFormDTO");
        validator(1L, false).validate(dto, errors);
        assertFalse(errors.hasFieldErrors("recorrente"));
        assertFalse(errors.hasFieldErrors("qtdMesesRecorrencia"));
    }

    @Test
    @DisplayName("RN25 - Despesa não pode ser parcelada e recorrente ao mesmo tempo")
    void validate_recorrenteEParceladaJuntas_deveRejeitar() {
        when(contaRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(10L, 1L)).thenReturn(Optional.of(contaAtiva(10L, 1L, TipoConta.CORRENTE)));

        DespesaFormDTO dto = dtoValido();
        dto.setParcelada(true);
        dto.setQtdParcelas(3);
        dto.setRecorrente(true);
        dto.setQtdMesesRecorrencia(12);

        Errors errors = new BeanPropertyBindingResult(dto, "despesaFormDTO");
        validator(1L, false).validate(dto, errors);
        assertTrue(errors.hasFieldErrors("recorrente"));
    }

    @Test
    @DisplayName("RN25 - Despesa recorrente com meses fora do intervalo (2 a 36) deve rejeitar")
    void validate_recorrenteMesesInvalido_deveRejeitar() {
        when(contaRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(10L, 1L)).thenReturn(Optional.of(contaAtiva(10L, 1L, TipoConta.CORRENTE)));

        DespesaFormDTO dto = dtoValido();
        dto.setRecorrente(true);
        dto.setQtdMesesRecorrencia(1);

        Errors errors = new BeanPropertyBindingResult(dto, "despesaFormDTO");
        validator(1L, false).validate(dto, errors);
        assertTrue(errors.hasFieldErrors("qtdMesesRecorrencia"));
    }
}
