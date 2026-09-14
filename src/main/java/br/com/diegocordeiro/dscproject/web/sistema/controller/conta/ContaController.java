package br.com.diegocordeiro.dscproject.web.sistema.controller.conta;

import br.com.diegocordeiro.dscproject.dto.conta.ContaAjusteSaldoDTO;
import br.com.diegocordeiro.dscproject.dto.conta.ContaEdicaoDTO;
import br.com.diegocordeiro.dscproject.dto.conta.ContaFormDTO;
import br.com.diegocordeiro.dscproject.dto.conta.ContaGridDTO;
import br.com.diegocordeiro.dscproject.dto.conta.ContaOpcaoDTO;
import br.com.diegocordeiro.dscproject.enums.TipoConta;
import br.com.diegocordeiro.dscproject.model.usuario.Usuario;
import br.com.diegocordeiro.dscproject.repository.conta.ContaRepository;
import br.com.diegocordeiro.dscproject.repository.instituicaofinanceira.InstituicaoFinanceiraRepository;
import br.com.diegocordeiro.dscproject.repository.usuario.UsuarioRepository;
import br.com.diegocordeiro.dscproject.service.conta.ContaService;
import br.com.diegocordeiro.dscproject.service.exceptions.RegistroNaoEncontradoException;
import br.com.diegocordeiro.dscproject.util.SecurityUtils;
import br.com.diegocordeiro.dscproject.web.sistema.validator.conta.ContaValidator;
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
public class ContaController {

    private final ContaService contaService;
    private final ContaRepository contaRepository;
    private final InstituicaoFinanceiraRepository instituicaoFinanceiraRepository;
    private final UsuarioRepository usuarioRepository;
    private final MessageSource messageSource;
    private final SmartValidator smartValidator;

    public ContaController(ContaService contaService,
                           ContaRepository contaRepository,
                           InstituicaoFinanceiraRepository instituicaoFinanceiraRepository,
                           UsuarioRepository usuarioRepository,
                           MessageSource messageSource,
                           SmartValidator smartValidator) {
        this.contaService = contaService;
        this.contaRepository = contaRepository;
        this.instituicaoFinanceiraRepository = instituicaoFinanceiraRepository;
        this.usuarioRepository = usuarioRepository;
        this.messageSource = messageSource;
        this.smartValidator = smartValidator;
    }

    @InitBinder
    public void binderComum(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    @GetMapping("/contas/listar")
    public String listar(Model model) {
        model.addAttribute("tipos", TipoConta.values());
        model.addAttribute("moedas", List.of("BRL", "USD", "EUR", "GBP"));
        return "sistema/modulos/conta/listar";
    }

    @GetMapping("/contas/listar-dados")
    @ResponseBody
    public List<ContaGridDTO> listarDados(Principal principal) {
        Usuario usuario = obterUsuarioAutenticado(principal);
        return contaService.listarParaGrid(usuario.getId());
    }

    @GetMapping("/contas/buscar/{id}")
    @ResponseBody
    public ContaEdicaoDTO buscar(@PathVariable Long id, Principal principal) {
        Usuario usuario = obterUsuarioAutenticado(principal);
        return contaService.buscarParaEdicao(id, usuario.getId());
    }

    @PostMapping("/contas/inserir")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> inserir(@ModelAttribute ContaFormDTO dto, Principal principal, Locale locale) {
        Usuario usuario = obterUsuarioAutenticado(principal);
        dto.setId(null);

        BindingResult resultado = validar(dto, usuario.getId(), locale);
        if (resultado.hasErrors()) {
            return respostaErros(resultado);
        }

        contaService.inserir(dto, usuario.getId(), usuario.getLogin());
        return ResponseEntity.ok(Map.of("sucesso", true, "mensagem", mensagem("msg.conta.cadastrada", locale)));
    }

    @PutMapping("/contas/editar/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> editar(@PathVariable Long id, @ModelAttribute ContaFormDTO dto, Principal principal, Locale locale) {
        Usuario usuario = obterUsuarioAutenticado(principal);
        dto.setId(id);

        BindingResult resultado = validar(dto, usuario.getId(), locale);
        if (resultado.hasErrors()) {
            return respostaErros(resultado);
        }

        contaService.editar(id, dto, usuario.getId(), usuario.getLogin());
        return ResponseEntity.ok(Map.of("sucesso", true, "mensagem", mensagem("msg.conta.atualizada", locale)));
    }

    @PutMapping("/contas/ajustar-saldo/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> ajustarSaldo(@PathVariable Long id, @ModelAttribute ContaAjusteSaldoDTO dto, Principal principal, Locale locale) {
        Usuario usuario = obterUsuarioAutenticado(principal);

        BindingResult resultado = new BeanPropertyBindingResult(dto, "contaAjusteSaldoDTO");
        smartValidator.validate(dto, resultado);
        if (resultado.hasErrors()) {
            return respostaErros(resultado);
        }

        contaService.ajustarSaldo(id, dto, usuario.getId(), usuario.getLogin());
        return ResponseEntity.ok(Map.of("sucesso", true, "mensagem", mensagem("msg.conta.saldo.ajustado", locale)));
    }

    @PutMapping("/contas/desativar/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> desativar(@PathVariable Long id, Principal principal, Locale locale) {
        Usuario usuario = obterUsuarioAutenticado(principal);
        contaService.desativar(id, usuario.getId());
        return ResponseEntity.ok(Map.of("sucesso", true, "mensagem", mensagem("msg.conta.atualizada", locale)));
    }

    @DeleteMapping("/contas/excluir/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> excluir(@PathVariable Long id, Principal principal, Locale locale) {
        Usuario usuario = obterUsuarioAutenticado(principal);
        contaService.excluir(id, usuario.getId(), usuario.getLogin());
        return ResponseEntity.ok(Map.of("sucesso", true, "mensagem", mensagem("msg.conta.excluida", locale)));
    }

    @GetMapping("/contas/opcoes")
    @ResponseBody
    public List<ContaOpcaoDTO> listarOpcoes(Principal principal) {
        Usuario usuario = obterUsuarioAutenticado(principal);
        return contaService.listarOpcoesCombobox(usuario.getId());
    }

    private Usuario obterUsuarioAutenticado(Principal principal) {
        String login = principal != null ? principal.getName() : SecurityUtils.loginAtual();
        return usuarioRepository.findByLogin(login)
            .orElseThrow(() -> new RegistroNaoEncontradoException("msg.usuario.nao-encontrado"));
    }

    private BindingResult validar(ContaFormDTO dto, Long usuarioId, Locale locale) {
        BindingResult resultado = new BeanPropertyBindingResult(dto, "contaFormDTO");
        smartValidator.validate(dto, resultado);
        new ContaValidator(contaRepository, instituicaoFinanceiraRepository, messageSource, locale, usuarioId)
            .validate(dto, resultado);
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
