package br.com.diegocordeiro.dscproject.web.sistema.controller.fatura;

import br.com.diegocordeiro.dscproject.dto.cartaocredito.CartaoCreditoOpcaoDTO;
import br.com.diegocordeiro.dscproject.dto.conta.ContaOpcaoDTO;
import br.com.diegocordeiro.dscproject.dto.fatura.FaturaCartaoGridDTO;
import br.com.diegocordeiro.dscproject.dto.fatura.FaturaCompraItemDTO;
import br.com.diegocordeiro.dscproject.dto.fatura.FaturaEncargosDTO;
import br.com.diegocordeiro.dscproject.dto.fatura.FaturaFechamentoDTO;
import br.com.diegocordeiro.dscproject.dto.fatura.FaturaPagamentoDTO;
import br.com.diegocordeiro.dscproject.model.usuario.Usuario;
import br.com.diegocordeiro.dscproject.repository.usuario.UsuarioRepository;
import br.com.diegocordeiro.dscproject.service.cartao.CartaoCreditoService;
import br.com.diegocordeiro.dscproject.service.conta.ContaService;
import br.com.diegocordeiro.dscproject.service.exceptions.RegistroNaoEncontradoException;
import br.com.diegocordeiro.dscproject.service.fatura.FaturaCartaoService;
import br.com.diegocordeiro.dscproject.util.SecurityUtils;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.SmartValidator;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Controller
public class FaturaCartaoController {

    private final FaturaCartaoService faturaCartaoService;
    private final CartaoCreditoService cartaoCreditoService;
    private final ContaService contaService;
    private final UsuarioRepository usuarioRepository;
    private final MessageSource messageSource;
    private final SmartValidator smartValidator;

    public FaturaCartaoController(
            FaturaCartaoService faturaCartaoService,
            CartaoCreditoService cartaoCreditoService,
            ContaService contaService,
            UsuarioRepository usuarioRepository,
            MessageSource messageSource,
            SmartValidator smartValidator) {
        this.faturaCartaoService = faturaCartaoService;
        this.cartaoCreditoService = cartaoCreditoService;
        this.contaService = contaService;
        this.usuarioRepository = usuarioRepository;
        this.messageSource = messageSource;
        this.smartValidator = smartValidator;
    }

    @InitBinder
    public void binderComum(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    @GetMapping("/faturas-cartao/listar")
    public String listar(Model model, Principal principal) {
        Usuario usuario = obterUsuarioAutenticado(principal);
        List<CartaoCreditoOpcaoDTO> cartoes = cartaoCreditoService.listarOpcoesCombobox(usuario.getId());
        List<ContaOpcaoDTO> contas = contaService.listarOpcoesCombobox(usuario.getId());

        model.addAttribute("cartoes", cartoes);
        model.addAttribute("contas", contas);
        return "sistema/modulos/fatura-cartao/listar";
    }

    @GetMapping("/faturas-cartao/dados")
    @ResponseBody
    public ResponseEntity<List<FaturaCartaoGridDTO>> listarDados(@RequestParam Long cartaoId, Principal principal) {
        Usuario usuario = obterUsuarioAutenticado(principal);
        return ResponseEntity.ok(faturaCartaoService.listarFaturas(cartaoId, usuario.getId()));
    }

    @GetMapping("/faturas-cartao/{id}/compras")
    @ResponseBody
    public ResponseEntity<List<FaturaCompraItemDTO>> listarCompras(@PathVariable Long id, Principal principal) {
        Usuario usuario = obterUsuarioAutenticado(principal);
        return ResponseEntity.ok(faturaCartaoService.listarComprasDaFatura(id, usuario.getId()));
    }

    @PostMapping("/faturas-cartao/{id}/fechar")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> fechar(@PathVariable Long id, @ModelAttribute FaturaFechamentoDTO dto, Principal principal, Locale locale) {
        Usuario usuario = obterUsuarioAutenticado(principal);
        BindingResult resultado = new BeanPropertyBindingResult(dto, "faturaFechamentoDTO");
        smartValidator.validate(dto, resultado);
        if (resultado.hasErrors()) {
            return respostaErros(resultado);
        }

        faturaCartaoService.fecharFatura(id, dto, usuario.getId(), usuario.getLogin());
        return ResponseEntity.ok(Map.of("sucesso", true, "mensagem", mensagem("msg.fatura.fechada.sucesso", locale)));
    }

    @PostMapping("/faturas-cartao/{id}/reabrir")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> reabrir(@PathVariable Long id, Principal principal, Locale locale) {
        Usuario usuario = obterUsuarioAutenticado(principal);
        faturaCartaoService.reabrirFatura(id, usuario.getId(), usuario.getLogin());
        return ResponseEntity.ok(Map.of("sucesso", true, "mensagem", mensagem("msg.fatura.reaberta.sucesso", locale)));
    }

    @PostMapping("/faturas-cartao/{id}/pagar")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> pagar(@PathVariable Long id, @ModelAttribute FaturaPagamentoDTO dto, Principal principal, Locale locale) {
        Usuario usuario = obterUsuarioAutenticado(principal);
        BindingResult resultado = new BeanPropertyBindingResult(dto, "faturaPagamentoDTO");
        smartValidator.validate(dto, resultado);
        if (resultado.hasErrors()) {
            return respostaErros(resultado);
        }

        faturaCartaoService.registrarPagamento(id, dto, usuario.getId(), usuario.getLogin());
        return ResponseEntity.ok(Map.of("sucesso", true, "mensagem", mensagem("msg.fatura.paga.sucesso", locale)));
    }

    @PutMapping("/faturas-cartao/{id}/encargos")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> editarEncargos(@PathVariable Long id, @ModelAttribute FaturaEncargosDTO dto, Principal principal, Locale locale) {
        Usuario usuario = obterUsuarioAutenticado(principal);
        faturaCartaoService.editarEncargos(id, dto, usuario.getId(), usuario.getLogin());
        return ResponseEntity.ok(Map.of("sucesso", true, "mensagem", mensagem("msg.fatura.fechada.sucesso", locale)));
    }

    private ResponseEntity<Map<String, Object>> respostaErros(BindingResult resultado) {
        Map<String, String> errosCampos = new LinkedHashMap<>();
        resultado.getFieldErrors().forEach(fe -> errosCampos.putIfAbsent(fe.getField(), fe.getDefaultMessage()));
        return ResponseEntity.badRequest().body(Map.of(
            "sucesso", false,
            "errosCampos", errosCampos
        ));
    }

    private Usuario obterUsuarioAutenticado(Principal principal) {
        String login = principal != null ? principal.getName() : SecurityUtils.loginAtual();
        return usuarioRepository.findByLogin(login)
            .orElseThrow(() -> new RegistroNaoEncontradoException("msg.usuario.nao-encontrado"));
    }

    private String mensagem(String chave, Locale locale) {
        return messageSource.getMessage(chave, null, locale);
    }
}
