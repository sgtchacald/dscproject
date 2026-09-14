package br.com.diegocordeiro.dscproject.web.sistema.controller.receita;

import br.com.diegocordeiro.dscproject.dto.receita.ReceitaEdicaoDTO;
import br.com.diegocordeiro.dscproject.dto.receita.ReceitaFormDTO;
import br.com.diegocordeiro.dscproject.dto.receita.ReceitaGridDTO;
import br.com.diegocordeiro.dscproject.dto.receita.ReceitaRecebimentoDTO;
import br.com.diegocordeiro.dscproject.model.usuario.Usuario;
import br.com.diegocordeiro.dscproject.repository.categoria.CategoriaRepository;
import br.com.diegocordeiro.dscproject.repository.conta.ContaRepository;
import br.com.diegocordeiro.dscproject.repository.receita.ReceitaRepository;
import br.com.diegocordeiro.dscproject.repository.usuario.UsuarioRepository;
import br.com.diegocordeiro.dscproject.service.receita.ReceitaService;
import br.com.diegocordeiro.dscproject.service.exceptions.RegistroNaoEncontradoException;
import br.com.diegocordeiro.dscproject.util.SecurityUtils;
import br.com.diegocordeiro.dscproject.web.sistema.validator.receita.ReceitaValidator;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Controller
public class ReceitaController {

    private final ReceitaService receitaService;
    private final ReceitaRepository receitaRepository;
    private final ContaRepository contaRepository;
    private final CategoriaRepository categoriaRepository;
    private final UsuarioRepository usuarioRepository;
    private final MessageSource messageSource;
    private final SmartValidator smartValidator;

    public ReceitaController(ReceitaService receitaService,
                              ReceitaRepository receitaRepository,
                              ContaRepository contaRepository,
                              CategoriaRepository categoriaRepository,
                              UsuarioRepository usuarioRepository,
                              MessageSource messageSource,
                              SmartValidator smartValidator) {
        this.receitaService = receitaService;
        this.receitaRepository = receitaRepository;
        this.contaRepository = contaRepository;
        this.categoriaRepository = categoriaRepository;
        this.usuarioRepository = usuarioRepository;
        this.messageSource = messageSource;
        this.smartValidator = smartValidator;
    }

    @InitBinder
    public void binderComum(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    @GetMapping("/receitas/listar")
    public String listar(Model model) {
        return "sistema/modulos/receita/listar";
    }

    @GetMapping("/receitas/listar-dados")
    @ResponseBody
    public List<ReceitaGridDTO> listarDados(Principal principal) {
        Usuario usuario = obterUsuarioAutenticado(principal);
        return receitaService.listarParaGrid(usuario.getId());
    }

    @PostMapping("/receitas/inserir")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> inserir(@ModelAttribute ReceitaFormDTO dto, Principal principal, Locale locale) {
        Usuario usuario = obterUsuarioAutenticado(principal);
        dto.setId(null);

        BindingResult resultado = validar(dto, usuario.getId(), locale);
        if (resultado.hasErrors()) {
            return respostaErros(resultado);
        }

        receitaService.inserir(dto, usuario.getId(), usuario.getLogin());
        return ResponseEntity.ok(Map.of("sucesso", true, "mensagem", mensagem("msg.receita.cadastrada", locale)));
    }

    @GetMapping("/receitas/buscar/{id}")
    @ResponseBody
    public ReceitaEdicaoDTO buscar(@PathVariable Long id, Principal principal) {
        Usuario usuario = obterUsuarioAutenticado(principal);
        return receitaService.buscarParaEdicao(id, usuario.getId());
    }

    @PutMapping("/receitas/editar/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> editar(@PathVariable Long id, @ModelAttribute ReceitaFormDTO dto, Principal principal, Locale locale) {
        Usuario usuario = obterUsuarioAutenticado(principal);
        // resolve a posse antes de qualquer outra validação: id de receita de outro usuário é sempre 404.
        receitaService.buscarPorIdEUsuario(id, usuario.getId());
        dto.setId(id);

        BindingResult resultado = validar(dto, usuario.getId(), locale);
        if (resultado.hasErrors()) {
            return respostaErros(resultado);
        }

        receitaService.editar(id, dto, usuario.getId(), usuario.getLogin());
        return ResponseEntity.ok(Map.of("sucesso", true, "mensagem", mensagem("msg.receita.atualizada", locale)));
    }

    @PutMapping("/receitas/marcar-recebida/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> marcarRecebida(@PathVariable Long id, @ModelAttribute ReceitaRecebimentoDTO dto, Principal principal, Locale locale) {
        Usuario usuario = obterUsuarioAutenticado(principal);

        BindingResult resultado = new BeanPropertyBindingResult(dto, "receitaRecebimentoDTO");
        smartValidator.validate(dto, resultado);
        if (resultado.hasErrors()) {
            return respostaErros(resultado);
        }

        receitaService.marcarRecebida(id, dto.getDataRecebimento(), usuario.getId(), usuario.getLogin());
        return ResponseEntity.ok(Map.of("sucesso", true, "mensagem", mensagem("msg.receita.recebimento.registrado", locale)));
    }

    @DeleteMapping("/receitas/excluir/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> excluir(@PathVariable Long id, Principal principal, Locale locale) {
        Usuario usuario = obterUsuarioAutenticado(principal);
        receitaService.excluir(id, usuario.getId(), usuario.getLogin());
        return ResponseEntity.ok(Map.of("sucesso", true, "mensagem", mensagem("msg.receita.excluida", locale)));
    }

    @PostMapping("/receitas/duplicar")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> duplicar(@RequestParam(value = "ids", required = false) List<Long> ids, @RequestParam(value = "competenciaAlvo", required = false) String competenciaAlvo, Principal principal, Locale locale) {
        if (ids == null || ids.isEmpty()) {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("sucesso", false);
            body.put("errosCampos", Map.of());
            body.put("errosNegocio", Map.of("ids", mensagem("msg.receita.duplicar.vazio", locale)));
            return ResponseEntity.unprocessableEntity().body(body);
        }
        Usuario usuario = obterUsuarioAutenticado(principal);
        receitaService.duplicar(ids, competenciaAlvo, usuario.getId(), usuario.getLogin());
        return ResponseEntity.ok(Map.of("sucesso", true, "mensagem", mensagem("msg.receita.duplicada", locale)));
    }

    private Usuario obterUsuarioAutenticado(Principal principal) {
        String login = principal != null ? principal.getName() : SecurityUtils.loginAtual();
        return usuarioRepository.findByLogin(login)
            .orElseThrow(() -> new RegistroNaoEncontradoException("usuario.nao.encontrado"));
    }

    private BindingResult validar(ReceitaFormDTO dto, Long usuarioId, Locale locale) {
        BindingResult resultado = new BeanPropertyBindingResult(dto, "receitaFormDTO");
        smartValidator.validate(dto, resultado);
        new ReceitaValidator(receitaRepository, contaRepository, categoriaRepository, messageSource, locale, usuarioId)
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
