package br.com.diegocordeiro.dscproject.web.sistema.validator.perfil;

import br.com.diegocordeiro.dscproject.dto.perfil.PerfilFormDTO;
import br.com.diegocordeiro.dscproject.service.perfil.PerfilService;
import org.springframework.context.MessageSource;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.Locale;

/**
 * Validação de fronteira que depende do banco: unicidade do código de perfil
 * entre perfis não excluídos. O prefixo {@code Duplicate.} marca erro de negócio.
 * As travas anti-lockout ficam no {@code PerfilService} (RNF02).
 */
public class PerfilValidator implements Validator {

    private final PerfilService service;
    private final MessageSource messageSource;
    private final Locale locale;

    public PerfilValidator(PerfilService service, MessageSource messageSource, Locale locale) {
        this.service = service;
        this.messageSource = messageSource;
        this.locale = locale;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return PerfilFormDTO.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        PerfilFormDTO dto = (PerfilFormDTO) target;

        if (dto.getCodigo() != null && !dto.getCodigo().isBlank()
                && !errors.hasFieldErrors("codigo")
                && service.verificarCodigoDuplicado(dto.getCodigo(), dto.getId())) {
            errors.rejectValue("codigo", "Duplicate.perfilFormDTO.codigo",
                messageSource.getMessage("perfil.codigo.duplicado", null, locale));
        }
    }
}
