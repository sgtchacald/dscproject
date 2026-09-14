package br.com.diegocordeiro.dscproject.web.sistema.validator.categoria;

import br.com.diegocordeiro.dscproject.dto.categoria.CategoriaFormDTO;
import br.com.diegocordeiro.dscproject.repository.categoria.CategoriaRepository;
import br.com.diegocordeiro.dscproject.service.categoria.CategoriaService;
import org.springframework.context.MessageSource;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.Locale;

public class CategoriaValidator implements Validator {

    private final CategoriaRepository categoriaRepository;
    private final MessageSource messageSource;
    private final Locale locale;

    public CategoriaValidator(CategoriaRepository categoriaRepository, MessageSource messageSource, Locale locale) {
        this.categoriaRepository = categoriaRepository;
        this.messageSource = messageSource;
        this.locale = locale;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return CategoriaFormDTO.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        CategoriaFormDTO dto = (CategoriaFormDTO) target;

        if (dto.getCodigo() != null && !dto.getCodigo().isBlank() && !errors.hasFieldErrors("codigo")) {
            String normalizado = CategoriaService.normalizarCodigo(dto.getCodigo());
            if (normalizado.isBlank()) {
                errors.rejectValue("codigo", "NotBlank.categoriaFormDTO.codigo",
                    messageSource.getMessage("categoria.validacao.codigo.obrigatorio", null, locale));
            } else if (categoriaRepository.contarPorCodigo(normalizado, dto.getId()) > 0) {
                errors.rejectValue("codigo", "Duplicate.categoriaFormDTO.codigo",
                    messageSource.getMessage("categoria.codigo.duplicado", null, locale));
            }
        }
    }
}
