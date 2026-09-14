package br.com.diegocordeiro.dscproject.web.sistema.controller.instituicaofinanceira;

import br.com.diegocordeiro.dscproject.dto.instituicaoprovedor.InstituicaoProvedorEdicaoDTO;
import br.com.diegocordeiro.dscproject.dto.instituicaoprovedor.InstituicaoProvedorFormDTO;
import br.com.diegocordeiro.dscproject.dto.instituicaoprovedor.InstituicaoProvedorGridDTO;
import br.com.diegocordeiro.dscproject.dto.instituicaoprovedor.ProvedorOpcaoDTO;
import br.com.diegocordeiro.dscproject.repository.instituicaofinanceira.InstituicaoFinanceiraRepository;
import br.com.diegocordeiro.dscproject.repository.instituicaofinanceira.OpfiInstituicaoProvedorRepository;
import br.com.diegocordeiro.dscproject.repository.instituicaofinanceira.OpfiProvedorRepository;
import br.com.diegocordeiro.dscproject.service.instituicaofinanceira.InstituicaoProvedorService;
import br.com.diegocordeiro.dscproject.web.sistema.validator.instituicaofinanceira.InstituicaoProvedorValidator;
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
public class InstituicaoProvedorController {

    private final InstituicaoProvedorService instituicaoProvedorService;
    private final OpfiInstituicaoProvedorRepository opfiInstituicaoProvedorRepository;
    private final InstituicaoFinanceiraRepository instituicaoFinanceiraRepository;
    private final OpfiProvedorRepository opfiProvedorRepository;
    private final MessageSource messageSource;
    private final SmartValidator smartValidator;

    public InstituicaoProvedorController(InstituicaoProvedorService instituicaoProvedorService,
                                         OpfiInstituicaoProvedorRepository opfiInstituicaoProvedorRepository,
                                         InstituicaoFinanceiraRepository instituicaoFinanceiraRepository,
                                         OpfiProvedorRepository opfiProvedorRepository,
                                         MessageSource messageSource,
                                         SmartValidator smartValidator) {
        this.instituicaoProvedorService = instituicaoProvedorService;
        this.opfiInstituicaoProvedorRepository = opfiInstituicaoProvedorRepository;
        this.instituicaoFinanceiraRepository = instituicaoFinanceiraRepository;
        this.opfiProvedorRepository = opfiProvedorRepository;
        this.messageSource = messageSource;
        this.smartValidator = smartValidator;
    }

    @InitBinder
    public void binderComum(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    @GetMapping("/instituicoes-provedor/listar")
    public String listar(Model model) {
        model.addAttribute("provedores", instituicaoProvedorService.listarProvedoresOpcoes());
        return "sistema/modulos/instituicao-provedor/listar";
    }

    @GetMapping("/instituicoes-provedor/listar-dados")
    @ResponseBody
    public List<InstituicaoProvedorGridDTO> listarDados() {
        return instituicaoProvedorService.listarParaGrid();
    }

    @GetMapping("/instituicoes-provedor/buscar/{id}")
    @ResponseBody
    public InstituicaoProvedorEdicaoDTO buscar(@PathVariable Long id) {
        return instituicaoProvedorService.buscarParaEdicao(id);
    }

    @PostMapping("/instituicoes-provedor/inserir")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> inserir(@ModelAttribute InstituicaoProvedorFormDTO dto, Locale locale) {
        dto.setId(null);
        BindingResult resultado = validar(dto, locale);
        if (resultado.hasErrors()) {
            return respostaErros(resultado);
        }
        instituicaoProvedorService.inserir(dto);
        return ResponseEntity.ok(Map.of("sucesso", true, "mensagem", mensagem("msg.instituicaoprovedor.cadastrado", locale)));
    }

    @PutMapping("/instituicoes-provedor/editar/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> editar(@PathVariable Long id, @ModelAttribute InstituicaoProvedorFormDTO dto, Locale locale) {
        dto.setId(id);
        BindingResult resultado = validar(dto, locale);
        if (resultado.hasErrors()) {
            return respostaErros(resultado);
        }
        instituicaoProvedorService.editar(id, dto);
        return ResponseEntity.ok(Map.of("sucesso", true, "mensagem", mensagem("msg.instituicaoprovedor.atualizado", locale)));
    }

    @DeleteMapping("/instituicoes-provedor/excluir/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> excluir(@PathVariable Long id, Principal principal, Locale locale) {
        String usuario = principal != null ? principal.getName() : "SISTEMA";
        instituicaoProvedorService.excluir(id, usuario);
        return ResponseEntity.ok(Map.of("sucesso", true, "mensagem", mensagem("msg.instituicaoprovedor.excluido", locale)));
    }

    @GetMapping("/instituicoes-provedor/provedores-opcoes")
    @ResponseBody
    public List<ProvedorOpcaoDTO> listarProvedoresOpcoes() {
        return instituicaoProvedorService.listarProvedoresOpcoes();
    }

    private BindingResult validar(InstituicaoProvedorFormDTO dto, Locale locale) {
        BindingResult resultado = new BeanPropertyBindingResult(dto, "instituicaoProvedorFormDTO");
        smartValidator.validate(dto, resultado);
        new InstituicaoProvedorValidator(opfiInstituicaoProvedorRepository, instituicaoFinanceiraRepository, opfiProvedorRepository, messageSource, locale)
            .validate(dto, resultado);
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
