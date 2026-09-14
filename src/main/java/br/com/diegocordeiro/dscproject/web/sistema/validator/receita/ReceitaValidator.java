package br.com.diegocordeiro.dscproject.web.sistema.validator.receita;

import br.com.diegocordeiro.dscproject.model.conta.Conta;
import br.com.diegocordeiro.dscproject.dto.receita.ReceitaFormDTO;
import br.com.diegocordeiro.dscproject.enums.AplicaA;
import br.com.diegocordeiro.dscproject.enums.OrigemLancamento;
import br.com.diegocordeiro.dscproject.model.categoria.Categoria;
import br.com.diegocordeiro.dscproject.model.receita.Receita;
import br.com.diegocordeiro.dscproject.repository.categoria.CategoriaRepository;
import br.com.diegocordeiro.dscproject.repository.conta.ContaRepository;
import br.com.diegocordeiro.dscproject.repository.receita.ReceitaRepository;
import org.springframework.context.MessageSource;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.Locale;
import java.util.Optional;

/**
 * Validação de fronteira que depende do banco/estado: conta, categoria e a
 * coerência entre "recebido" e a data de recebimento. Na edição de uma
 * receita importada (origem diferente de manual), a conta não é revalidada
 * — o valor enviado é ignorado, não rejeitado.
 */
public class ReceitaValidator implements Validator {

    private final ReceitaRepository receitaRepository;
    private final ContaRepository contaRepository;
    private final CategoriaRepository categoriaRepository;
    private final MessageSource messageSource;
    private final Locale locale;
    private final Long usuarioId;

    public ReceitaValidator(ReceitaRepository receitaRepository,
                             ContaRepository contaRepository,
                             CategoriaRepository categoriaRepository,
                             MessageSource messageSource,
                             Locale locale,
                             Long usuarioId) {
        this.receitaRepository = receitaRepository;
        this.contaRepository = contaRepository;
        this.categoriaRepository = categoriaRepository;
        this.messageSource = messageSource;
        this.locale = locale;
        this.usuarioId = usuarioId;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return ReceitaFormDTO.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        ReceitaFormDTO dto = (ReceitaFormDTO) target;

        if (dto.getContaId() != null && !errors.hasFieldErrors("contaId")) {
            validarConta(dto, errors);
        }

        if (dto.getCategoriaId() != null && !errors.hasFieldErrors("categoriaId")) {
            validarCategoria(dto, errors);
        }

        if (dto.isRecebido() && dto.getDataRecebimento() == null) {
            errors.rejectValue("dataRecebimento", "NotNull.receitaFormDTO.dataRecebimento",
                messageSource.getMessage("msg.receita.recebimento.data-obrigatoria", null, locale));
        }
    }

    private void validarConta(ReceitaFormDTO dto, Errors errors) {
        if (edicaoDeReceitaImportada(dto)) {
            return; // receita importada: a conta original é preservada, não revalidada
        }
        Optional<br.com.diegocordeiro.dscproject.model.conta.Conta> contaOpt =
            contaRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(dto.getContaId(), usuarioId);
        if (contaOpt.isEmpty() || !contaOpt.get().isAtivo()) {
            errors.rejectValue("contaId", "Invalid.receitaFormDTO.contaId",
                messageSource.getMessage("msg.receita.conta.invalida", null, locale));
        }
    }

    private boolean edicaoDeReceitaImportada(ReceitaFormDTO dto) {
        if (dto.getId() == null) {
            return false;
        }
        return receitaRepository.findByIdAndContaUsuarioIdAndDataExclusaoIsNull(dto.getId(), usuarioId)
            .map(Receita::getOrigem)
            .map(origem -> origem != OrigemLancamento.MANUAL)
            .orElse(false);
    }

    private void validarCategoria(ReceitaFormDTO dto, Errors errors) {
        Optional<Categoria> categoriaOpt = categoriaRepository.findByIdAndDataExclusaoIsNull(dto.getCategoriaId());
        boolean valida = categoriaOpt.isPresent()
            && categoriaOpt.get().isAtivo()
            && (categoriaOpt.get().getAplicaA() == AplicaA.RECEITA || categoriaOpt.get().getAplicaA() == AplicaA.AMBOS);
        if (!valida) {
            errors.rejectValue("categoriaId", "Invalid.receitaFormDTO.categoriaId",
                messageSource.getMessage("msg.receita.categoria.invalida", null, locale));
        }
    }
}
