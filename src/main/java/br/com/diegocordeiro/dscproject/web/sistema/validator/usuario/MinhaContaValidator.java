package br.com.diegocordeiro.dscproject.web.sistema.validator.usuario;

import br.com.diegocordeiro.dscproject.model.conta.Conta;
import br.com.diegocordeiro.dscproject.dto.minhaconta.MinhaContaDTO;
import br.com.diegocordeiro.dscproject.service.usuario.UsuarioService;
import org.springframework.context.MessageSource;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.Locale;

/**
 * Validação de fronteira da tela Configurações da Conta: unicidade de login e
 * e-mail entre usuários não excluídos (ignorando o próprio registro) e conferência
 * da confirmação quando o usuário decide trocar a senha.
 */
public class MinhaContaValidator implements Validator {

    private final UsuarioService service;
    private final MessageSource messageSource;
    private final Locale locale;
    private final Long idAtual;

    public MinhaContaValidator(UsuarioService service, MessageSource messageSource, Locale locale, Long idAtual) {
        this.service = service;
        this.messageSource = messageSource;
        this.locale = locale;
        this.idAtual = idAtual;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return MinhaContaDTO.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        MinhaContaDTO dto = (MinhaContaDTO) target;

        if (dto.senhaInformada() && !dto.senhasConferem()) {
            errors.rejectValue("confirmacaoSenha", "Differ.minhaContaDTO.confirmacaoSenha",
                mensagem("usuario.confirmacaoSenha.diferente"));
        }

        if (hasTexto(dto.getLogin())
                && !errors.hasFieldErrors("login")
                && service.verificarSeExiste(dto.getLogin(), idAtual)) {
            errors.rejectValue("login", "Duplicate.minhaContaDTO.login",
                mensagem("usuario.login.duplicado"));
        }
        if (hasTexto(dto.getEmail())
                && !errors.hasFieldErrors("email")
                && service.verificarSeExiste(dto.getEmail(), idAtual)) {
            errors.rejectValue("email", "Duplicate.minhaContaDTO.email",
                mensagem("usuario.email.duplicado"));
        }
    }

    private boolean hasTexto(String valor) {
        return valor != null && !valor.isBlank();
    }

    private String mensagem(String chave) {
        return messageSource.getMessage(chave, null, locale);
    }
}
