package br.com.diegocordeiro.dscproject.web.sistema.validator.instituicaofinanceira;

import br.com.diegocordeiro.dscproject.dto.instituicaoprovedor.InstituicaoProvedorFormDTO;
import br.com.diegocordeiro.dscproject.model.instituicaofinanceira.InstituicaoFinanceira;
import br.com.diegocordeiro.dscproject.model.instituicaofinanceira.OpenFinanceProvedor;
import br.com.diegocordeiro.dscproject.repository.instituicaofinanceira.InstituicaoFinanceiraRepository;
import br.com.diegocordeiro.dscproject.repository.instituicaofinanceira.OpenFinanceInstituicaoProvedorRepository;
import br.com.diegocordeiro.dscproject.repository.instituicaofinanceira.OpenFinanceProvedorRepository;
import org.springframework.context.MessageSource;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.Locale;
import java.util.Optional;

public class InstituicaoProvedorValidator implements Validator {

    private final OpenFinanceInstituicaoProvedorRepository openFinanceInstituicaoProvedorRepository;
    private final InstituicaoFinanceiraRepository instituicaoFinanceiraRepository;
    private final OpenFinanceProvedorRepository openFinanceProvedorRepository;
    private final MessageSource messageSource;
    private final Locale locale;

    public InstituicaoProvedorValidator(OpenFinanceInstituicaoProvedorRepository openFinanceInstituicaoProvedorRepository, InstituicaoFinanceiraRepository instituicaoFinanceiraRepository, OpenFinanceProvedorRepository openFinanceProvedorRepository, MessageSource messageSource, Locale locale) {
        this.openFinanceInstituicaoProvedorRepository = openFinanceInstituicaoProvedorRepository;
        this.instituicaoFinanceiraRepository = instituicaoFinanceiraRepository;
        this.openFinanceProvedorRepository = openFinanceProvedorRepository;
        this.messageSource = messageSource;
        this.locale = locale;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return InstituicaoProvedorFormDTO.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        InstituicaoProvedorFormDTO dto = (InstituicaoProvedorFormDTO) target;

        if (dto.getInstituicaoId() != null && dto.getProvedorId() != null && !errors.hasFieldErrors("instituicaoId")) {
            long count = openFinanceInstituicaoProvedorRepository.contarPorInstituicaoEProvedor(
                dto.getInstituicaoId(), dto.getProvedorId(), dto.getId());
            if (count > 0) {
                errors.rejectValue("instituicaoId", "Duplicate.instituicaoProvedorFormDTO.instituicaoId",
                    messageSource.getMessage("msg.instituicaoprovedor.instituicao.duplicada", null, locale));
            }
        }

        if (dto.getProvedorId() != null && dto.getIdExterno() != null && !dto.getIdExterno().isBlank() && !errors.hasFieldErrors("idExterno")) {
            long count = openFinanceInstituicaoProvedorRepository.contarPorProvedorEIdExterno(
                dto.getProvedorId(), dto.getIdExterno().trim(), dto.getId());
            if (count > 0) {
                errors.rejectValue("idExterno", "Duplicate.instituicaoProvedorFormDTO.idExterno",
                    messageSource.getMessage("msg.instituicaoprovedor.idexterno.duplicado", null, locale));
            }
        }

        if (dto.getInstituicaoId() != null && !errors.hasFieldErrors("instituicaoId")) {
            Optional<InstituicaoFinanceira> infiOpt = instituicaoFinanceiraRepository.findByIdAndDataExclusaoIsNull(dto.getInstituicaoId());
            if (infiOpt.isEmpty() || !infiOpt.get().isAtivo()) {
                errors.rejectValue("instituicaoId", "Invalid.instituicaoProvedorFormDTO.instituicaoId",
                    messageSource.getMessage("msg.instituicaoprovedor.instituicao.invalida", null, locale));
            }
        }

        if (dto.getProvedorId() != null && !errors.hasFieldErrors("provedorId")) {
            Optional<OpenFinanceProvedor> provOpt = openFinanceProvedorRepository.findById(dto.getProvedorId());
            if (provOpt.isEmpty() || !provOpt.get().isAtivo() || provOpt.get().isExcluido()) {
                errors.rejectValue("provedorId", "Invalid.instituicaoProvedorFormDTO.provedorId",
                    messageSource.getMessage("msg.instituicaoprovedor.provedor.invalido", null, locale));
            }
        }
    }
}
