package br.com.diegocordeiro.dscproject.web.sistema.controller;

import br.com.diegocordeiro.dscproject.dto.usuario.UsuarioRedeSocialDTO;
import br.com.diegocordeiro.dscproject.service.usuario.UsuarioRedeSocialService;
import br.com.diegocordeiro.dscproject.util.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@ControllerAdvice
public class GlobalModelAdvice {

    private final String appVersao;
    private final UsuarioRedeSocialService usuarioRedeSocialService;

    public GlobalModelAdvice(@Value("${app.version:dev}") String appVersao, @Autowired(required = false) UsuarioRedeSocialService usuarioRedeSocialService) {
        this.appVersao = appVersao;
        this.usuarioRedeSocialService = usuarioRedeSocialService;
    }

    @ModelAttribute("appVersao")
    public String appVersao() {
        return "v" + appVersao;
    }

    /** URI da requisição atual — a sidebar usa para marcar o item de menu ativo (Thymeleaf 3.1 não expõe mais {@code #request}). */
    @ModelAttribute("uriAtual")
    public String uriAtual(HttpServletRequest request) {
        return request.getRequestURI();
    }

    /** Redes sociais ativas do usuário autenticado para alimentar footer/header do layout admin. */
    @ModelAttribute("redesSociaisUsuario")
    public List<UsuarioRedeSocialDTO> redesSociaisUsuario() {
        if (usuarioRedeSocialService == null) {
            return List.of();
        }
        String login = SecurityUtils.loginAtual();
        return usuarioRedeSocialService.listarAtivasPorLogin(login);
    }
}
