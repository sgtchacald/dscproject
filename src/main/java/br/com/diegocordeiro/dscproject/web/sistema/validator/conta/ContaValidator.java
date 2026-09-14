package br.com.diegocordeiro.dscproject.web.sistema.validator.conta;

import br.com.diegocordeiro.dscproject.dto.conta.ContaFormDTO;
import br.com.diegocordeiro.dscproject.model.instituicaofinanceira.InstituicaoFinanceira;
import br.com.diegocordeiro.dscproject.repository.conta.ContaRepository;
import br.com.diegocordeiro.dscproject.repository.instituicaofinanceira.InstituicaoFinanceiraRepository;
import org.springframework.context.MessageSource;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.Locale;
import java.util.Optional;

public class ContaValidator implements Validator {

    private final ContaRepository contaRepository;
    private final InstituicaoFinanceiraRepository instituicaoFinanceiraRepository;
    private final MessageSource messageSource;
    private final Locale locale;
    private final Long usuarioId;

    public ContaValidator(ContaRepository contaRepository, InstituicaoFinanceiraRepository instituicaoFinanceiraRepository, MessageSource messageSource, Locale locale, Long usuarioId) {
        this.contaRepository = contaRepository;
        this.instituicaoFinanceiraRepository = instituicaoFinanceiraRepository;
        this.messageSource = messageSource;
        this.locale = locale;
        this.usuarioId = usuarioId;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return ContaFormDTO.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        ContaFormDTO dto = (ContaFormDTO) target;

        if (dto.getDescricao() != null && !dto.getDescricao().isBlank() && !errors.hasFieldErrors("descricao")) {
            String descricao = dto.getDescricao().trim();
            if (contaRepository.contarPorUsuarioEDescricao(usuarioId, descricao, dto.getId()) > 0) {
                errors.rejectValue("descricao", "Duplicate.contaFormDTO.descricao",
                    messageSource.getMessage("msg.conta.descricao.duplicada", null, locale));
            }
        }

        if (dto.getId() == null) {
            // Modo Criação
            if (dto.getInstituicaoId() == null) {
                errors.rejectValue("instituicaoId", "NotNull.contaFormDTO.instituicaoId",
                    messageSource.getMessage("conta.validacao.instituicao.obrigatoria", null, locale));
            } else {
                Optional<InstituicaoFinanceira> instOpt = instituicaoFinanceiraRepository.findByIdAndDataExclusaoIsNull(dto.getInstituicaoId());
                if (instOpt.isEmpty() || !instOpt.get().isAtivo()) {
                    errors.rejectValue("instituicaoId", "Invalid.contaFormDTO.instituicaoId",
                        messageSource.getMessage("msg.conta.instituicao.invalida", null, locale));
                }
            }
        } else {
            // Modo Edição: instituição imutável
            contaRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(dto.getId(), usuarioId).ifPresent(c -> {
                if (dto.getInstituicaoId() != null && !dto.getInstituicaoId().equals(c.getInstituicao().getId())) {
                    errors.rejectValue("instituicaoId", "Immutable.contaFormDTO.instituicaoId",
                        messageSource.getMessage("msg.conta.instituicao.imutavel", null, locale));
                }
            });
        }
    }
}
