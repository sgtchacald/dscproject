package br.com.diegocordeiro.dscproject.web.sistema.validator.transacao;

import br.com.diegocordeiro.dscproject.dto.transacao.TransacaoBancariaFormDTO;
import br.com.diegocordeiro.dscproject.enums.OrigemLancamento;
import br.com.diegocordeiro.dscproject.model.categoria.Categoria;
import br.com.diegocordeiro.dscproject.model.conta.Conta;
import br.com.diegocordeiro.dscproject.model.transacao.TransacaoBancaria;
import br.com.diegocordeiro.dscproject.repository.categoria.CategoriaRepository;
import br.com.diegocordeiro.dscproject.repository.conta.ContaRepository;
import br.com.diegocordeiro.dscproject.repository.transacao.TransacaoBancariaRepository;
import org.springframework.context.MessageSource;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.Locale;
import java.util.Optional;

public class TransacaoBancariaValidator implements Validator {

    private final TransacaoBancariaRepository transacaoBancariaRepository;
    private final ContaRepository contaRepository;
    private final CategoriaRepository categoriaRepository;
    private final MessageSource messageSource;
    private final Locale locale;
    private final Long usuarioId;

    public TransacaoBancariaValidator(TransacaoBancariaRepository transacaoBancariaRepository,
                                     ContaRepository contaRepository,
                                     CategoriaRepository categoriaRepository,
                                     MessageSource messageSource,
                                     Locale locale,
                                     Long usuarioId) {
        this.transacaoBancariaRepository = transacaoBancariaRepository;
        this.contaRepository = contaRepository;
        this.categoriaRepository = categoriaRepository;
        this.messageSource = messageSource;
        this.locale = locale;
        this.usuarioId = usuarioId;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return TransacaoBancariaFormDTO.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        TransacaoBancariaFormDTO dto = (TransacaoBancariaFormDTO) target;

        if (dto.getContaId() != null && !errors.hasFieldErrors("contaId")) {
            validarConta(dto, errors);
        }

        if (dto.getCategoriaId() != null && !errors.hasFieldErrors("categoriaId")) {
            validarCategoria(dto, errors);
        }

        if (dto.getId() != null) {
            validarTravasEdicao(dto, errors);
        }
    }

    private void validarConta(TransacaoBancariaFormDTO dto, Errors errors) {
        if (edicaoDeTransacaoImportada(dto)) {
            return;
        }
        Optional<Conta> contaOpt = contaRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(dto.getContaId(), usuarioId);
        if (contaOpt.isEmpty() || !contaOpt.get().isAtivo()) {
            errors.rejectValue("contaId", "Invalid.transacaoBancariaFormDTO.contaId",
                messageSource.getMessage("msg.transacao.conta.invalida", null, locale));
        }
    }

    private boolean edicaoDeTransacaoImportada(TransacaoBancariaFormDTO dto) {
        if (dto.getId() == null) {
            return false;
        }
        return transacaoBancariaRepository.findByIdAndContaUsuarioIdAndDataExclusaoIsNull(dto.getId(), usuarioId)
            .map(TransacaoBancaria::getOrigem)
            .map(origem -> origem != OrigemLancamento.MANUAL)
            .orElse(false);
    }

    private void validarCategoria(TransacaoBancariaFormDTO dto, Errors errors) {
        Optional<Categoria> categoriaOpt = categoriaRepository.findByIdAndDataExclusaoIsNull(dto.getCategoriaId());
        if (categoriaOpt.isEmpty() || !categoriaOpt.get().isAtivo()) {
            errors.rejectValue("categoriaId", "Invalid.transacaoBancariaFormDTO.categoriaId",
                messageSource.getMessage("msg.transacao.categoria.invalida", null, locale));
        }
    }

    private void validarTravasEdicao(TransacaoBancariaFormDTO dto, Errors errors) {
        transacaoBancariaRepository.findByIdAndContaUsuarioIdAndDataExclusaoIsNull(dto.getId(), usuarioId)
            .ifPresent(transacao -> {
                if (transacao.isPagamentoFatura()) {
                    if (dto.getValor() != null && transacao.getValor().compareTo(dto.getValor()) != 0) {
                        errors.rejectValue("valor", "Invalid.transacaoBancariaFormDTO.valor",
                            messageSource.getMessage("msg.transacao.pagamento-fatura.imutavel", null, locale));
                    }
                    if (dto.getContaId() != null && !transacao.getConta().getId().equals(dto.getContaId())) {
                        errors.rejectValue("contaId", "Invalid.transacaoBancariaFormDTO.contaId",
                            messageSource.getMessage("msg.transacao.pagamento-fatura.imutavel", null, locale));
                    }
                }
            });
    }
}
