package br.com.diegocordeiro.dscproject.web.sistema.controller.parametro;

import br.com.diegocordeiro.dscproject.dto.parametro.ParametroGlobalEdicaoDTO;
import br.com.diegocordeiro.dscproject.dto.parametro.ParametroGlobalListaDTO;
import br.com.diegocordeiro.dscproject.dto.parametro.ParametroValorFormDTO;
import br.com.diegocordeiro.dscproject.dto.parametro.RestaurarPadraoFormDTO;
import br.com.diegocordeiro.dscproject.dto.parametro.RevisaoParametroDTO;
import br.com.diegocordeiro.dscproject.enums.TipoParametro;
import br.com.diegocordeiro.dscproject.service.parametro.ParametroGlobalService;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Fronteira web do agregado Parâmetro Global: tela de listagem, edição inline do
 * valor (com motivo obrigatório), restauração ao padrão e histórico de revisões.
 * Não há criação nem exclusão de parâmetro, nem alteração de tipo pela tela.
 */
@Controller
public class ParametroController {

    private final ParametroGlobalService parametroGlobalService;
    private final MessageSource messageSource;
    private final SmartValidator smartValidator;

    public ParametroController(ParametroGlobalService parametroGlobalService, MessageSource messageSource, SmartValidator smartValidator) {
        this.parametroGlobalService = parametroGlobalService;
        this.messageSource = messageSource;
        this.smartValidator = smartValidator;
    }

    @InitBinder
    public void binderComum(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    @GetMapping("/parametros/listar")
    public String listar(Model model) {
        model.addAttribute("tiposParametro", TipoParametro.values());
        return "sistema/modulos/parametro/listar";
    }

    @GetMapping("/parametros/listar-dados")
    @ResponseBody
    public List<ParametroGlobalListaDTO> listarDados() {
        return parametroGlobalService.listarParaGrid();
    }

    @GetMapping("/parametros/buscar/{id}")
    @ResponseBody
    public ParametroGlobalEdicaoDTO buscar(@PathVariable Long id) {
        return parametroGlobalService.buscarParaEdicao(id);
    }

    @PutMapping("/parametros/editar/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> editar(@PathVariable Long id, @ModelAttribute ParametroValorFormDTO dto, Locale locale) {
        BindingResult resultado = new BeanPropertyBindingResult(dto, "parametroValorFormDTO");
        smartValidator.validate(dto, resultado);
        if (resultado.hasErrors()) {
            return respostaErros(resultado);
        }
        parametroGlobalService.editarValor(id, dto);
        return ResponseEntity.ok(Map.of("sucesso", true, "mensagem", mensagem("msg.parametro.atualizado", locale)));
    }

    @PutMapping("/parametros/restaurar-padrao/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> restaurarPadrao(@PathVariable Long id, @ModelAttribute RestaurarPadraoFormDTO dto, Locale locale) {
        BindingResult resultado = new BeanPropertyBindingResult(dto, "restaurarPadraoFormDTO");
        smartValidator.validate(dto, resultado);
        if (resultado.hasErrors()) {
            return respostaErros(resultado);
        }
        parametroGlobalService.restaurarPadrao(id, dto.getMotivo());
        return ResponseEntity.ok(Map.of("sucesso", true, "mensagem", mensagem("msg.parametro.restaurado", locale)));
    }

    @GetMapping("/parametros/historico/{id}")
    @ResponseBody
    public Page<RevisaoParametroDTO> historico(@PathVariable Long id, @RequestParam(defaultValue = "0") int pagina, @RequestParam(defaultValue = "20") int tamanho) {
        return parametroGlobalService.buscarHistorico(id, PageRequest.of(pagina, tamanho));
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
