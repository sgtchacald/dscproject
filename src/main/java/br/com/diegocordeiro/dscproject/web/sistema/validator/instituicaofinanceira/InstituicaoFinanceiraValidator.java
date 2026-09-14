package br.com.diegocordeiro.dscproject.web.sistema.validator.instituicaofinanceira;

import br.com.diegocordeiro.dscproject.dto.instituicaofinanceira.InstituicaoFinanceiraFormDTO;
import br.com.diegocordeiro.dscproject.repository.instituicaofinanceira.InstituicaoFinanceiraRepository;
import org.springframework.context.MessageSource;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.Locale;

public class InstituicaoFinanceiraValidator implements Validator {

    private final InstituicaoFinanceiraRepository instituicaoFinanceiraRepository;
    private final MessageSource messageSource;
    private final Locale locale;

    public InstituicaoFinanceiraValidator(InstituicaoFinanceiraRepository instituicaoFinanceiraRepository, MessageSource messageSource, Locale locale) {
        this.instituicaoFinanceiraRepository = instituicaoFinanceiraRepository;
        this.messageSource = messageSource;
        this.locale = locale;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return InstituicaoFinanceiraFormDTO.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        InstituicaoFinanceiraFormDTO dto = (InstituicaoFinanceiraFormDTO) target;

        if (dto.getNome() != null && !dto.getNome().isBlank() && !errors.hasFieldErrors("nome")) {
            String nome = dto.getNome().trim();
            if (instituicaoFinanceiraRepository.contarPorNome(nome, dto.getId()) > 0) {
                errors.rejectValue("nome", "Duplicate.instituicaoFinanceiraFormDTO.nome",
                    messageSource.getMessage("msg.instituicao.nome.duplicado", null, locale));
            }
        }

        if (dto.getCodigo() != null && !dto.getCodigo().isBlank() && !errors.hasFieldErrors("codigo")) {
            String codigo = dto.getCodigo().replaceAll("\\D", "").trim();
            if (!codigo.isEmpty() && instituicaoFinanceiraRepository.contarPorCodigo(codigo, dto.getId()) > 0) {
                errors.rejectValue("codigo", "Duplicate.instituicaoFinanceiraFormDTO.codigo",
                    messageSource.getMessage("msg.instituicao.codigo.duplicado", null, locale));
            }
        }
    }
}
