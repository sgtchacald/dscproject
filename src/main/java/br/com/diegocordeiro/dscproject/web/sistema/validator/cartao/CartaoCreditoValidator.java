package br.com.diegocordeiro.dscproject.web.sistema.validator.cartao;

import br.com.diegocordeiro.dscproject.dto.cartaocredito.CartaoCreditoFormDTO;
import br.com.diegocordeiro.dscproject.enums.BandeiraCartao;
import br.com.diegocordeiro.dscproject.model.conta.Conta;
import br.com.diegocordeiro.dscproject.repository.conta.ContaRepository;
import org.springframework.context.MessageSource;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.Locale;
import java.util.Optional;

public class CartaoCreditoValidator implements Validator {

    private final ContaRepository contaRepository;
    private final MessageSource messageSource;
    private final Locale locale;
    private final Long usuarioId;

    public CartaoCreditoValidator(ContaRepository contaRepository, MessageSource messageSource, Locale locale, Long usuarioId) {
        this.contaRepository = contaRepository;
        this.messageSource = messageSource;
        this.locale = locale;
        this.usuarioId = usuarioId;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return CartaoCreditoFormDTO.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        CartaoCreditoFormDTO dto = (CartaoCreditoFormDTO) target;

        if (dto.getBandeira() != null && !dto.getBandeira().isBlank() && !errors.hasFieldErrors("bandeira")) {
            if (BandeiraCartao.porCodigo(dto.getBandeira()) == null) {
                errors.rejectValue("bandeira", "NotValid.cartaoCreditoFormDTO.bandeira",
                    messageSource.getMessage("cartao.validacao.bandeira.invalida", null, locale));
            }
        }

        if (dto.getContaId() != null && !errors.hasFieldErrors("contaId")) {
            Optional<Conta> contaOpt = contaRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(dto.getContaId(), usuarioId);
            if (contaOpt.isEmpty() || !contaOpt.get().isAtivo()) {
                errors.rejectValue("contaId", "Invalid.cartaoCreditoFormDTO.contaId",
                    messageSource.getMessage("msg.cartao.conta.invalida", null, locale));
            }
        }
    }
}
