package br.com.diegocordeiro.dscproject.web.sistema.controller.instituicaofinanceira;

import br.com.diegocordeiro.dscproject.dto.instituicaofinanceira.InstituicaoFinanceiraEdicaoDTO;
import br.com.diegocordeiro.dscproject.dto.instituicaofinanceira.InstituicaoFinanceiraFormDTO;
import br.com.diegocordeiro.dscproject.dto.instituicaofinanceira.InstituicaoFinanceiraGridDTO;
import br.com.diegocordeiro.dscproject.dto.instituicaofinanceira.InstituicaoFinanceiraOpcaoDTO;
import br.com.diegocordeiro.dscproject.enums.TipoInstituicaoFinanceira;
import br.com.diegocordeiro.dscproject.repository.instituicaofinanceira.InstituicaoFinanceiraRepository;
import br.com.diegocordeiro.dscproject.service.instituicaofinanceira.InstituicaoFinanceiraService;
import br.com.diegocordeiro.dscproject.web.sistema.validator.instituicaofinanceira.InstituicaoFinanceiraValidator;
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
public class InstituicaoFinanceiraController {

    private final InstituicaoFinanceiraService instituicaoFinanceiraService;
    private final InstituicaoFinanceiraRepository instituicaoFinanceiraRepository;
    private final MessageSource messageSource;
    private final SmartValidator smartValidator;

    public InstituicaoFinanceiraController(InstituicaoFinanceiraService instituicaoFinanceiraService, InstituicaoFinanceiraRepository instituicaoFinanceiraRepository, MessageSource messageSource, SmartValidator smartValidator) {
        this.instituicaoFinanceiraService = instituicaoFinanceiraService;
        this.instituicaoFinanceiraRepository = instituicaoFinanceiraRepository;
        this.messageSource = messageSource;
        this.smartValidator = smartValidator;
    }

    @InitBinder
    public void binderComum(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    @GetMapping("/instituicoes-financeiras/listar")
    public String listar(Model model) {
        model.addAttribute("tipos", TipoInstituicaoFinanceira.values());
        return "sistema/modulos/instituicao-financeira/listar";
    }

    @GetMapping("/instituicoes-financeiras/listar-dados")
    @ResponseBody
    public List<InstituicaoFinanceiraGridDTO> listarDados() {
        return instituicaoFinanceiraService.listarParaGrid();
    }

    @GetMapping("/instituicoes-financeiras/buscar/{id}")
    @ResponseBody
    public InstituicaoFinanceiraEdicaoDTO buscar(@PathVariable Long id) {
        return instituicaoFinanceiraService.buscarParaEdicao(id);
    }

    @PostMapping("/instituicoes-financeiras/inserir")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> inserir(@ModelAttribute InstituicaoFinanceiraFormDTO dto, Locale locale) {
        dto.setId(null);
        BindingResult resultado = validar(dto, locale);
        if (resultado.hasErrors()) {
            return respostaErros(resultado);
        }
        instituicaoFinanceiraService.inserir(dto);
        return ResponseEntity.ok(Map.of("sucesso", true, "mensagem", mensagem("msg.instituicao.cadastrada", locale)));
    }

    @PutMapping("/instituicoes-financeiras/editar/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> editar(@PathVariable Long id, @ModelAttribute InstituicaoFinanceiraFormDTO dto, Locale locale) {
        dto.setId(id);
        BindingResult resultado = validar(dto, locale);
        if (resultado.hasErrors()) {
            return respostaErros(resultado);
        }
        instituicaoFinanceiraService.editar(id, dto);
        return ResponseEntity.ok(Map.of("sucesso", true, "mensagem", mensagem("msg.instituicao.atualizada", locale)));
    }

    @PutMapping("/instituicoes-financeiras/desativar/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> desativar(@PathVariable Long id, Locale locale) {
        instituicaoFinanceiraService.desativar(id);
        return ResponseEntity.ok(Map.of("sucesso", true, "mensagem", mensagem("msg.instituicao.atualizada", locale)));
    }

    @DeleteMapping("/instituicoes-financeiras/excluir/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> excluir(@PathVariable Long id, Principal principal, Locale locale) {
        String usuario = principal != null ? principal.getName() : "SISTEMA";
        instituicaoFinanceiraService.excluir(id, usuario);
        return ResponseEntity.ok(Map.of("sucesso", true, "mensagem", mensagem("msg.instituicao.excluida", locale)));
    }

    @GetMapping("/instituicoes-financeiras/opcoes")
    @ResponseBody
    public List<InstituicaoFinanceiraOpcaoDTO> listarOpcoes(@RequestParam(value = "tipo", required = false) TipoInstituicaoFinanceira tipo) {
        return instituicaoFinanceiraService.listarOpcoesCombobox(tipo);
    }

    private BindingResult validar(InstituicaoFinanceiraFormDTO dto, Locale locale) {
        BindingResult resultado = new BeanPropertyBindingResult(dto, "instituicaoFinanceiraFormDTO");
        smartValidator.validate(dto, resultado);
        new InstituicaoFinanceiraValidator(instituicaoFinanceiraRepository, messageSource, locale).validate(dto, resultado);
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
