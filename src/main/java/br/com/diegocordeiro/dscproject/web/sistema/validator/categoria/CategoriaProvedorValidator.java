package br.com.diegocordeiro.dscproject.web.sistema.validator.categoria;

import br.com.diegocordeiro.dscproject.dto.categoriaprovedor.CategoriaProvedorFormDTO;
import br.com.diegocordeiro.dscproject.model.categoria.Categoria;
import br.com.diegocordeiro.dscproject.model.instituicaofinanceira.OpfiProvedor;
import br.com.diegocordeiro.dscproject.repository.categoria.CategoriaProvedorRepository;
import br.com.diegocordeiro.dscproject.repository.categoria.CategoriaRepository;
import br.com.diegocordeiro.dscproject.repository.instituicaofinanceira.OpfiProvedorRepository;
import org.springframework.context.MessageSource;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.Locale;
import java.util.Optional;

public class CategoriaProvedorValidator implements Validator {

    private final CategoriaProvedorRepository categoriaProvedorRepository;
    private final CategoriaRepository categoriaRepository;
    private final OpfiProvedorRepository opfiProvedorRepository;
    private final MessageSource messageSource;
    private final Locale locale;

    public CategoriaProvedorValidator(CategoriaProvedorRepository categoriaProvedorRepository, CategoriaRepository categoriaRepository, OpfiProvedorRepository opfiProvedorRepository, MessageSource messageSource, Locale locale) {
        this.categoriaProvedorRepository = categoriaProvedorRepository;
        this.categoriaRepository = categoriaRepository;
        this.opfiProvedorRepository = opfiProvedorRepository;
        this.messageSource = messageSource;
        this.locale = locale;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return CategoriaProvedorFormDTO.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        CategoriaProvedorFormDTO dto = (CategoriaProvedorFormDTO) target;

        if (dto.getProvedorId() != null && dto.getRotuloExterno() != null && !dto.getRotuloExterno().isBlank()
                && !errors.hasFieldErrors("rotuloExterno")) {
            long count = categoriaProvedorRepository.contarPorProvedorERotulo(
                dto.getProvedorId(), dto.getRotuloExterno().trim(), dto.getId());
            if (count > 0) {
                errors.rejectValue("rotuloExterno", "Duplicate.categoriaProvedorFormDTO.rotuloExterno",
                    messageSource.getMessage("categoriaprovedor.rotulo.duplicado", null, locale));
            }
        }

        if (dto.getCategoriaId() != null && !errors.hasFieldErrors("categoriaId")) {
            Optional<Categoria> catOpt = categoriaRepository.findById(dto.getCategoriaId());
            if (catOpt.isEmpty() || !catOpt.get().isAtivo() || catOpt.get().isExcluido()) {
                errors.rejectValue("categoriaId", "Invalid.categoriaProvedorFormDTO.categoriaId",
                    messageSource.getMessage("categoriaprovedor.categoria.invalida", null, locale));
            }
        }

        if (dto.getProvedorId() != null && !errors.hasFieldErrors("provedorId")) {
            Optional<OpfiProvedor> provOpt = opfiProvedorRepository.findById(dto.getProvedorId());
            if (provOpt.isEmpty() || !provOpt.get().isAtivo() || provOpt.get().isExcluido()) {
                errors.rejectValue("provedorId", "Invalid.categoriaProvedorFormDTO.provedorId",
                    messageSource.getMessage("categoriaprovedor.provedor.invalido", null, locale));
            }
        }
    }
}
