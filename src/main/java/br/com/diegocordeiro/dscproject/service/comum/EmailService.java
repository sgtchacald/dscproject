package br.com.diegocordeiro.dscproject.service.comum;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Locale;

/**
 * Envio de e-mails do sistema. Usa o {@code JavaMailSender} do
 * {@code spring-boot-starter-mail}; se o e-mail não estiver configurado
 * o envio é apenas registrado em log (não quebra o fluxo).
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final MessageSource messageSource;
    private final String remetente;

    public EmailService(ObjectProvider<JavaMailSender> mailSenderProvider, MessageSource messageSource, @Value("${spring.mail.username:}") String remetente) {
        this.mailSender = mailSenderProvider.getIfAvailable();
        this.messageSource = messageSource;
        this.remetente = remetente;
    }

    /** E-mail com o link de redefinição de senha. */
    public void enviarLinkRecuperacaoSenha(String destinatario, String nome, String link) {
        if (mailSender == null) {
            log.warn("JavaMailSender ausente — e-mail de recuperação de senha não enviado para {}", destinatario);
            return;
        }
        Locale locale = LocaleContextHolder.getLocale();
        SimpleMailMessage mensagem = new SimpleMailMessage();
        if (remetente != null && !remetente.isBlank()) {
            mensagem.setFrom(remetente);
        }
        mensagem.setTo(destinatario);
        mensagem.setSubject(messageSource.getMessage("email.recuperacao.assunto", null, locale));
        mensagem.setText(messageSource.getMessage("email.recuperacao.corpo", new Object[]{nome, link}, locale));
        try {
            mailSender.send(mensagem);
        } catch (MailException e) {
            log.error("Falha ao enviar e-mail de recuperação de senha para {}", destinatario, e);
        }
    }
}
