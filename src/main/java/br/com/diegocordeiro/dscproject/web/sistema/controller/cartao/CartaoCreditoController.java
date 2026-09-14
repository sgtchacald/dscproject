package br.com.diegocordeiro.dscproject.web.sistema.controller.cartao;

import br.com.diegocordeiro.dscproject.dto.cartaocredito.CartaoCreditoEdicaoDTO;
import br.com.diegocordeiro.dscproject.dto.cartaocredito.CartaoCreditoFormDTO;
import br.com.diegocordeiro.dscproject.dto.cartaocredito.CartaoCreditoGridDTO;
import br.com.diegocordeiro.dscproject.dto.cartaocredito.CartaoCreditoOpcaoDTO;
import br.com.diegocordeiro.dscproject.enums.BandeiraCartao;
import br.com.diegocordeiro.dscproject.model.usuario.Usuario;
import br.com.diegocordeiro.dscproject.repository.cartao.CartaoCreditoRepository;
import br.com.diegocordeiro.dscproject.repository.conta.ContaRepository;
import br.com.diegocordeiro.dscproject.repository.usuario.UsuarioRepository;
import br.com.diegocordeiro.dscproject.service.cartao.CartaoCreditoService;
import br.com.diegocordeiro.dscproject.service.exceptions.RegistroNaoEncontradoException;
import br.com.diegocordeiro.dscproject.util.SecurityUtils;
import br.com.diegocordeiro.dscproject.web.sistema.validator.cartao.CartaoCreditoValidator;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
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

import java.security.Principal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Controller
public class CartaoCreditoController {

    private final CartaoCreditoService cartaoCreditoService;
    private final CartaoCreditoRepository cartaoCreditoRepository;
    private final ContaRepository contaRepository;
    private final UsuarioRepository usuarioRepository;
    private final MessageSource messageSource;
    private final SmartValidator smartValidator;

    public CartaoCreditoController(CartaoCreditoService cartaoCreditoService,
                                   CartaoCreditoRepository cartaoCreditoRepository,
                                   ContaRepository contaRepository,
                                   UsuarioRepository usuarioRepository,
                                   MessageSource messageSource,
                                   SmartValidator smartValidator) {
        this.cartaoCreditoService = cartaoCreditoService;
        this.cartaoCreditoRepository = cartaoCreditoRepository;
        this.contaRepository = contaRepository;
        this.usuarioRepository = usuarioRepository;
        this.messageSource = messageSource;
        this.smartValidator = smartValidator;
    }

    @InitBinder
    public void binderComum(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    @GetMapping("/cartoes/listar")
    public String listar(Model model) {
        model.addAttribute("bandeiras", BandeiraCartao.values());
        return "sistema/modulos/cartao/listar";
    }

    @GetMapping("/cartoes/listar-dados")
    @ResponseBody
    public List<CartaoCreditoGridDTO> listarDados(Principal principal) {
        Usuario usuario = obterUsuarioAutenticado(principal);
        return cartaoCreditoService.listarParaGrid(usuario.getId());
    }

    @GetMapping("/cartoes/buscar/{id}")
    @ResponseBody
    public CartaoCreditoEdicaoDTO buscar(@PathVariable Long id, Principal principal) {
        Usuario usuario = obterUsuarioAutenticado(principal);
        return cartaoCreditoService.buscarParaEdicao(id, usuario.getId());
    }

    @PostMapping("/cartoes/inserir")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> inserir(@ModelAttribute CartaoCreditoFormDTO dto, Principal principal, Locale locale) {
        Usuario usuario = obterUsuarioAutenticado(principal);
        dto.setId(null);

        BindingResult resultado = validar(dto, usuario.getId(), locale);
        if (resultado.hasErrors()) {
            return respostaErros(resultado);
        }

        cartaoCreditoService.inserir(dto, usuario.getId(), usuario.getLogin());
        return ResponseEntity.ok(Map.of("sucesso", true, "mensagem", mensagem("msg.cartao.cadastrado", locale)));
    }

    @PutMapping("/cartoes/editar/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> editar(@PathVariable Long id, @ModelAttribute CartaoCreditoFormDTO dto, Principal principal, Locale locale) {
        Usuario usuario = obterUsuarioAutenticado(principal);
        dto.setId(id);

        BindingResult resultado = validar(dto, usuario.getId(), locale);
        if (resultado.hasErrors()) {
            return respostaErros(resultado);
        }

        cartaoCreditoService.editar(id, dto, usuario.getId(), usuario.getLogin());
        return ResponseEntity.ok(Map.of("sucesso", true, "mensagem", mensagem("msg.cartao.atualizado", locale)));
    }

    @DeleteMapping("/cartoes/excluir/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> excluir(@PathVariable Long id, Principal principal, Locale locale) {
        Usuario usuario = obterUsuarioAutenticado(principal);
        cartaoCreditoService.excluir(id, usuario.getId(), usuario.getLogin());
        return ResponseEntity.ok(Map.of("sucesso", true, "mensagem", mensagem("msg.cartao.excluido", locale)));
    }

    @GetMapping("/cartoes/opcoes")
    @ResponseBody
    public List<CartaoCreditoOpcaoDTO> listarOpcoes(Principal principal) {
        Usuario usuario = obterUsuarioAutenticado(principal);
        return cartaoCreditoService.listarOpcoesCombobox(usuario.getId());
    }

    private Usuario obterUsuarioAutenticado(Principal principal) {
        String login = principal != null ? principal.getName() : SecurityUtils.loginAtual();
        return usuarioRepository.findByLogin(login)
            .orElseThrow(() -> new RegistroNaoEncontradoException("msg.usuario.nao-encontrado"));
    }

    private BindingResult validar(CartaoCreditoFormDTO dto, Long usuarioId, Locale locale) {
        BindingResult resultado = new BeanPropertyBindingResult(dto, "cartaoCreditoFormDTO");
        smartValidator.validate(dto, resultado);
        new CartaoCreditoValidator(contaRepository, messageSource, locale, usuarioId).validate(dto, resultado);
        return resultado;
    }

    private ResponseEntity<Map<String, Object>> respostaErros(BindingResult bindingResult) {
        Map<String, String> errosCampos = new LinkedHashMap<>();
        Map<String, String> errosNegocio = new LinkedHashMap<>();
        bindingResult.getFieldErrors().forEach(fe -> {
            String code = fe.getCode();
            if (code != null && (code.startsWith("Differ.") || code.startsWith("Duplicate.") || code.startsWith("Immutable."))) {
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
