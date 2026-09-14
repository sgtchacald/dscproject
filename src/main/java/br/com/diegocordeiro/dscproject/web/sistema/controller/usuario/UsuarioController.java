package br.com.diegocordeiro.dscproject.web.sistema.controller.usuario;

import br.com.diegocordeiro.dscproject.model.conta.Conta;
import br.com.diegocordeiro.dscproject.dto.minhaconta.MinhaContaDTO;
import br.com.diegocordeiro.dscproject.dto.recuperacaosenha.RecuperarSenhaConfirmacaoDTO;
import br.com.diegocordeiro.dscproject.dto.recuperacaosenha.RecuperarSenhaSolicitacaoDTO;
import br.com.diegocordeiro.dscproject.dto.usuario.AlterarSenhaDTO;
import br.com.diegocordeiro.dscproject.dto.usuario.RevisaoUsuarioDTO;
import br.com.diegocordeiro.dscproject.dto.usuario.UsuarioDTO;
import br.com.diegocordeiro.dscproject.dto.usuario.UsuarioEdicaoDTO;
import br.com.diegocordeiro.dscproject.dto.usuario.UsuarioListaDTO;
import br.com.diegocordeiro.dscproject.dto.usuario.UsuarioRedeSocialDTO;
import br.com.diegocordeiro.dscproject.dto.usuario.UsuarioRedeSocialFormDTO;
import br.com.diegocordeiro.dscproject.enums.Genero;
import br.com.diegocordeiro.dscproject.enums.TipoRedeSocial;
import br.com.diegocordeiro.dscproject.model.usuario.Usuario;
import br.com.diegocordeiro.dscproject.service.usuario.RecuperacaoSenhaService;
import br.com.diegocordeiro.dscproject.service.usuario.UsuarioRedeSocialService;
import br.com.diegocordeiro.dscproject.service.usuario.UsuarioService;
import br.com.diegocordeiro.dscproject.util.SecurityUtils;
import br.com.diegocordeiro.dscproject.web.sistema.validator.usuario.MinhaContaValidator;
import br.com.diegocordeiro.dscproject.web.sistema.validator.usuario.UsuarioValidator;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.beans.PropertyEditorSupport;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Toda a fronteira web do agregado Usuário: CRUD administrativo, auto-cadastro,
 * recuperação de senha e a tela self-service Configurações da Conta.
 */
@Controller
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final RecuperacaoSenhaService recuperacaoSenhaService;
    private final UsuarioRedeSocialService usuarioRedeSocialService;
    private final MessageSource messageSource;
    private final SmartValidator smartValidator;

    public UsuarioController(UsuarioService usuarioService, RecuperacaoSenhaService recuperacaoSenhaService, UsuarioRedeSocialService usuarioRedeSocialService, MessageSource messageSource, SmartValidator smartValidator) {
        this.usuarioService = usuarioService;
        this.recuperacaoSenhaService = recuperacaoSenhaService;
        this.usuarioRedeSocialService = usuarioRedeSocialService;
        this.messageSource = messageSource;
        this.smartValidator = smartValidator;
    }

    @InitBinder
    public void binderComum(WebDataBinder binder) {
        // campo de texto vazio no formulário chega como null — senha em branco não vira erro de tamanho
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    @InitBinder({"usuarioDTO", "minhaContaDTO"})
    public void binderGenero(WebDataBinder binder) {
        binder.registerCustomEditor(Genero.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                setValue((text == null || text.isBlank()) ? null : Genero.toEnum(text));
            }
        });
    }

    // ---------- Área administrativa ----------

    @GetMapping("/usuarios/listar")
    public String listar(Model model) {
        model.addAttribute("perfis", usuarioService.listarPerfis());
        model.addAttribute("generos", Genero.values());
        return "sistema/modulos/usuario/listar";
    }

    @GetMapping("/usuarios/listar-dados")
    @ResponseBody
    public List<UsuarioListaDTO> listarDados() {
        return usuarioService.listarParaGrid();
    }

    @GetMapping("/usuarios/buscar/{id}")
    @ResponseBody
    public UsuarioEdicaoDTO buscar(@PathVariable Long id) {
        return usuarioService.buscarParaEdicao(id);
    }

    @PostMapping("/usuarios/inserir")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> inserir(@ModelAttribute UsuarioDTO dto, Locale locale) {
        dto.setId(null);
        BindingResult resultado = validarUsuario(dto, locale);
        if (resultado.hasErrors()) {
            return respostaErros(resultado);
        }
        usuarioService.inserir(dto);
        return ResponseEntity.ok(Map.of("sucesso", true));
    }

    @PutMapping("/usuarios/editar/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> editar(@PathVariable Long id, @ModelAttribute UsuarioDTO dto, Locale locale) {
        dto.setId(id);
        BindingResult resultado = validarUsuario(dto, locale);
        if (resultado.hasErrors()) {
            return respostaErros(resultado);
        }
        usuarioService.editar(id, dto);
        return ResponseEntity.ok(Map.of("sucesso", true));
    }

    @PutMapping("/usuarios/{id}/senha")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> alterarSenha(@PathVariable Long id, @ModelAttribute AlterarSenhaDTO dto, Locale locale) {
        BindingResult resultado = new BeanPropertyBindingResult(dto, "alterarSenhaDTO");
        smartValidator.validate(dto, resultado);
        if (!dto.senhasConferem() && !resultado.hasFieldErrors("senha")) {
            resultado.rejectValue("confirmacaoSenha", "Differ.alterarSenhaDTO.confirmacaoSenha",
                mensagem("usuario.confirmacaoSenha.diferente", locale));
        }
        if (resultado.hasErrors()) {
            return respostaErros(resultado);
        }
        usuarioService.alterarSenha(id, dto.getSenha());
        return ResponseEntity.ok(Map.of("sucesso", true, "mensagem", mensagem("msg.usuario.senha.alterada", locale)));
    }

    @DeleteMapping("/usuarios/excluir/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> excluir(@PathVariable Long id, Authentication authentication) {
        usuarioService.excluir(id, authentication != null ? authentication.getName() : null);
        return ResponseEntity.ok(Map.of("sucesso", true));
    }

    @GetMapping("/usuarios/historico/{id}")
    @ResponseBody
    public Page<RevisaoUsuarioDTO> historico(@PathVariable Long id, @RequestParam(defaultValue = "0") int pagina, @RequestParam(defaultValue = "20") int tamanho) {
        return usuarioService.buscarHistorico(id, PageRequest.of(pagina, tamanho));
    }

    // ---------- Configurações da Conta (self-service) ----------

    @GetMapping("/minha-conta")
    public String minhaConta(Model model) {
        model.addAttribute("minhaConta", new MinhaContaDTO(usuarioService.buscarPorLogin(SecurityUtils.loginAtual())));
        model.addAttribute("generos", Genero.values());
        model.addAttribute("tiposRedesSociais", TipoRedeSocial.values());
        return "sistema/minha-conta";
    }

    @PutMapping("/minha-conta")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> salvarMinhaConta(@ModelAttribute MinhaContaDTO dto, Locale locale) {
        String login = SecurityUtils.loginAtual();
        Long idAtual = usuarioService.buscarPorLogin(login).getId();

        BindingResult resultado = new BeanPropertyBindingResult(dto, "minhaContaDTO");
        smartValidator.validate(dto, resultado);
        new MinhaContaValidator(usuarioService, messageSource, locale, idAtual).validate(dto, resultado);
        if (resultado.hasErrors()) {
            return respostaErros(resultado);
        }
        usuarioService.atualizarPropriaConta(login, dto);
        return ResponseEntity.ok(Map.of("sucesso", true, "mensagem", mensagem("msg.minha-conta.atualizada", locale)));
    }

    // ---------- Redes Sociais do Usuário (self-service: EDP15 a EDP19) ----------

    @GetMapping("/minha-conta/redes-sociais")
    @ResponseBody
    public List<UsuarioRedeSocialDTO> listarRedesSociais() {
        Usuario usuario = usuarioService.buscarPorLogin(SecurityUtils.loginAtual());
        return usuarioRedeSocialService.listarPorUsuario(usuario.getId());
    }

    @GetMapping("/minha-conta/redes-sociais/ativas")
    @ResponseBody
    public List<UsuarioRedeSocialDTO> listarRedesSociaisAtivas() {
        Usuario usuario = usuarioService.buscarPorLogin(SecurityUtils.loginAtual());
        return usuarioRedeSocialService.listarAtivasPorUsuario(usuario.getId());
    }

    @GetMapping("/minha-conta/redes-sociais/{id}")
    @ResponseBody
    public UsuarioRedeSocialDTO buscarRedeSocial(@PathVariable Long id) {
        Usuario usuario = usuarioService.buscarPorLogin(SecurityUtils.loginAtual());
        return usuarioRedeSocialService.buscarPorIdEUsuario(id, usuario.getId());
    }

    @PostMapping("/minha-conta/redes-sociais")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> inserirRedeSocial(@ModelAttribute UsuarioRedeSocialFormDTO form, Locale locale) {
        String login = SecurityUtils.loginAtual();
        Usuario usuario = usuarioService.buscarPorLogin(login);
        UsuarioRedeSocialDTO criada = usuarioRedeSocialService.inserir(form, usuario.getId(), login);
        return ResponseEntity.ok(Map.of(
                "sucesso", true,
                "mensagem", mensagem("msg.usuario.rede-social.cadastrada", locale),
                "redeSocial", criada
        ));
    }

    @PutMapping("/minha-conta/redes-sociais/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> editarRedeSocial(@PathVariable Long id, @ModelAttribute UsuarioRedeSocialFormDTO form, Locale locale) {
        String login = SecurityUtils.loginAtual();
        Usuario usuario = usuarioService.buscarPorLogin(login);
        UsuarioRedeSocialDTO atualizada = usuarioRedeSocialService.editar(id, form, usuario.getId(), login);
        return ResponseEntity.ok(Map.of(
                "sucesso", true,
                "mensagem", mensagem("msg.usuario.rede-social.atualizada", locale),
                "redeSocial", atualizada
        ));
    }

    @DeleteMapping("/minha-conta/redes-sociais/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> excluirRedeSocial(@PathVariable Long id, Locale locale) {
        String login = SecurityUtils.loginAtual();
        Usuario usuario = usuarioService.buscarPorLogin(login);
        usuarioRedeSocialService.excluir(id, usuario.getId(), login);
        return ResponseEntity.ok(Map.of(
                "sucesso", true,
                "mensagem", mensagem("msg.usuario.rede-social.excluida", locale)
        ));
    }

    // ---------- Públicos: auto-cadastro ----------

    @GetMapping("/usuarios/existe")
    @ResponseBody
    public boolean existe(@RequestParam String valor, @RequestParam(required = false) Long idAtual) {
        return usuarioService.verificarSeExiste(valor, idAtual);
    }

    @GetMapping("/usuarios/cadastrar-site")
    public String paginaAutoCadastro(Model model) {
        model.addAttribute("generos", Genero.values());
        return "sistema/publico/auto-cadastro";
    }

    @PostMapping("/usuarios/cadastrar-site")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> cadastrarSite(@ModelAttribute UsuarioDTO dto, Locale locale) {
        dto.setId(null);
        dto.setPerfilCodigo(null);   // no auto-cadastro o perfil vindo do formulário é ignorado
        BindingResult resultado = validarUsuario(dto, locale);
        if (resultado.hasErrors()) {
            return respostaErros(resultado);
        }
        usuarioService.autoCadastrar(dto);
        return ResponseEntity.ok(Map.of("sucesso", true));
    }

    // ---------- Públicos: recuperação de senha ----------

    @GetMapping("/usuarios/recuperar-senha")
    public String paginaRecuperarSenha(@RequestParam(required = false) String token, Model model) {
        boolean etapaDois = token != null && !token.isBlank();
        model.addAttribute("token", token);
        model.addAttribute("etapa", etapaDois ? 2 : 1);
        return "sistema/publico/recuperar-senha";
    }

    /** Resposta idêntica exista ou não a conta, para não revelar cadastro. */
    @PostMapping("/usuarios/recuperar-senha/solicitar")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> solicitarRecuperacao(@ModelAttribute RecuperarSenhaSolicitacaoDTO dto, Locale locale) {
        BindingResult resultado = new BeanPropertyBindingResult(dto, "recuperarSenhaSolicitacaoDTO");
        smartValidator.validate(dto, resultado);
        if (resultado.hasErrors()) {
            return respostaErros(resultado);
        }
        recuperacaoSenhaService.solicitar(dto.getEmail());
        return ResponseEntity.ok(Map.of(
            "sucesso", true,
            "mensagem", mensagem("msg.recuperacao.solicitacao.enviada", locale)));
    }

    /** Troca a senha; token inválido/expirado/excedido cai no advice (422). */
    @PostMapping("/usuarios/recuperar-senha/confirmar")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> confirmarRecuperacao(@ModelAttribute RecuperarSenhaConfirmacaoDTO dto, Locale locale) {
        BindingResult resultado = new BeanPropertyBindingResult(dto, "recuperarSenhaConfirmacaoDTO");
        smartValidator.validate(dto, resultado);
        if (!dto.senhasConferem() && !resultado.hasFieldErrors("senha")) {
            resultado.rejectValue("confirmacaoSenha", "Differ.recuperarSenhaConfirmacaoDTO.confirmacaoSenha",
                mensagem("usuario.confirmacaoSenha.diferente", locale));
        }
        if (resultado.hasErrors()) {
            return respostaErros(resultado);
        }
        recuperacaoSenhaService.confirmar(dto.getToken(), dto.getSenha());
        return ResponseEntity.ok(Map.of(
            "sucesso", true,
            "mensagem", mensagem("msg.recuperacao.senha.redefinida", locale)));
    }

    // ---------- infra ----------

    private BindingResult validarUsuario(UsuarioDTO dto, Locale locale) {
        BindingResult resultado = new BeanPropertyBindingResult(dto, "usuarioDTO");
        smartValidator.validate(dto, resultado);
        new UsuarioValidator(usuarioService, messageSource, locale).validate(dto, resultado);
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
