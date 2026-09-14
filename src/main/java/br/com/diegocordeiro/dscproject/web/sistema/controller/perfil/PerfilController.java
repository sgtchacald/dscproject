package br.com.diegocordeiro.dscproject.web.sistema.controller.perfil;

import br.com.diegocordeiro.dscproject.dto.perfil.PerfilEdicaoDTO;
import br.com.diegocordeiro.dscproject.dto.perfil.PerfilFormDTO;
import br.com.diegocordeiro.dscproject.dto.perfil.PerfilResumoDTO;
import br.com.diegocordeiro.dscproject.model.usuario.Usuario;
import br.com.diegocordeiro.dscproject.service.perfil.PerfilService;
import br.com.diegocordeiro.dscproject.web.sistema.validator.perfil.PerfilValidator;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.SmartValidator;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Fronteira web da tela Perfis e Permissões: CRUD de perfil e o vínculo perfil x permissão. */
@Controller
public class PerfilController {

    private final PerfilService perfilService;
    private final MessageSource messageSource;
    private final SmartValidator smartValidator;

    public PerfilController(PerfilService perfilService, MessageSource messageSource, SmartValidator smartValidator) {
        this.perfilService = perfilService;
        this.messageSource = messageSource;
        this.smartValidator = smartValidator;
    }

    @InitBinder
    public void binderComum(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    @GetMapping("/perfis/listar")
    public String listar(Authentication authentication, Model model) {
        model.addAttribute("perfilLogado", perfilDoUsuarioLogado(authentication));
        return "sistema/modulos/perfil/listar";
    }

    private String perfilDoUsuarioLogado(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof Usuario usuario
                && usuario.getPerfil() != null) {
            return usuario.getPerfil().getCodigo();
        }
        return null;
    }

    @GetMapping("/perfis/listar-dados")
    @ResponseBody
    public List<PerfilResumoDTO> listarDados() {
        return perfilService.listar();
    }

    @GetMapping("/perfis/buscar/{id}")
    @ResponseBody
    public PerfilEdicaoDTO buscar(@PathVariable Long id) {
        return perfilService.buscarParaEdicao(id);
    }

    @PostMapping("/perfis/inserir")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> inserir(@ModelAttribute PerfilFormDTO dto, Locale locale) {
        dto.setId(null);
        BindingResult resultado = validar(dto, locale);
        if (resultado.hasErrors()) {
            return respostaErros(resultado);
        }
        perfilService.inserir(dto);
        return ResponseEntity.ok(Map.of("sucesso", true, "mensagem", mensagem("msg.perfil.cadastrado", locale)));
    }

    @PutMapping("/perfis/editar/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> editar(@PathVariable Long id, @ModelAttribute PerfilFormDTO dto, Authentication authentication, Locale locale) {
        dto.setId(id);
        BindingResult resultado = validar(dto, locale);
        if (resultado.hasErrors()) {
            return respostaErros(resultado);
        }
        perfilService.editar(id, dto, authentication != null ? authentication.getName() : null);
        return ResponseEntity.ok(Map.of("sucesso", true, "mensagem", mensagem("msg.perfil.atualizado", locale)));
    }

    @DeleteMapping("/perfis/excluir/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> excluir(@PathVariable Long id, Locale locale) {
        perfilService.excluir(id);
        return ResponseEntity.ok(Map.of("sucesso", true, "mensagem", mensagem("msg.perfil.excluido", locale)));
    }

    private BindingResult validar(PerfilFormDTO dto, Locale locale) {
        BindingResult resultado = new BeanPropertyBindingResult(dto, "perfilFormDTO");
        smartValidator.validate(dto, resultado);
        new PerfilValidator(perfilService, messageSource, locale).validate(dto, resultado);
        return resultado;
    }

    private ResponseEntity<Map<String, Object>> respostaErros(BindingResult bindingResult) {
        Map<String, String> errosCampos = new LinkedHashMap<>();
        Map<String, String> errosNegocio = new LinkedHashMap<>();
        bindingResult.getFieldErrors().forEach(fe -> {
            String code = fe.getCode();
            if (code != null && (code.startsWith("Differ.") || code.startsWith("Duplicate."))) {
                errosNegocio.putIfAbsent(fe.getField(), fe.getDefaultMessage());
            } else {
                errosCampos.putIfAbsent(fe.getField(), fe.getDefaultMessage());
            }
        });
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("sucesso", false);
        body.put("errosCampos", errosCampos);
        body.put("errosNegocio", errosNegocio);
        return ResponseEntity.unprocessableEntity().body(body);
    }

    private String mensagem(String chave, Locale locale) {
        return messageSource.getMessage(chave, null, locale);
    }
}
