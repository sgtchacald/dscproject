package br.com.diegocordeiro.dscproject.web.sistema.controller.categoria;

import br.com.diegocordeiro.dscproject.dto.categoria.CategoriaFiltroDTO;
import br.com.diegocordeiro.dscproject.dto.categoria.CategoriaFormDTO;
import br.com.diegocordeiro.dscproject.dto.categoria.CategoriaGridDTO;
import br.com.diegocordeiro.dscproject.dto.categoria.CategoriaOpcaoDTO;
import br.com.diegocordeiro.dscproject.enums.AplicaA;
import br.com.diegocordeiro.dscproject.repository.categoria.CategoriaRepository;
import br.com.diegocordeiro.dscproject.service.categoria.CategoriaService;
import br.com.diegocordeiro.dscproject.service.exceptions.RegraNegocioException;
import br.com.diegocordeiro.dscproject.web.sistema.validator.categoria.CategoriaValidator;
import jakarta.validation.Valid;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;
import java.util.Locale;

@Controller
public class CategoriaController {

    private final CategoriaService categoriaService;
    private final CategoriaRepository categoriaRepository;
    private final MessageSource messageSource;

    public CategoriaController(CategoriaService categoriaService, CategoriaRepository categoriaRepository, MessageSource messageSource) {
        this.categoriaService = categoriaService;
        this.categoriaRepository = categoriaRepository;
        this.messageSource = messageSource;
    }

    @InitBinder
    public void binderComum(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    @GetMapping("/categorias/listar")
    public String listar(@ModelAttribute("filtro") CategoriaFiltroDTO filtro, Model model) {
        carregarDadosListagem(filtro, model);
        if (!model.containsAttribute("categoriaForm")) {
            model.addAttribute("categoriaForm", new CategoriaFormDTO());
        }
        return "sistema/modulos/categoria/listar";
    }

    @PostMapping("/categorias/inserir")
    public String inserir(@Valid @ModelAttribute("categoriaForm") CategoriaFormDTO form, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model, Locale locale) {
        new CategoriaValidator(categoriaRepository, messageSource, locale).validate(form, bindingResult);

        if (bindingResult.hasErrors()) {
            carregarDadosListagem(new CategoriaFiltroDTO(), model);
            model.addAttribute("abrirModalForm", true);
            model.addAttribute("modoModal", "INSERIR");
            return "sistema/modulos/categoria/listar";
        }

        try {
            categoriaService.inserir(form);
            redirectAttributes.addFlashAttribute("sucessoMensagem",
                messageSource.getMessage("msg.categoria.cadastrada", null, locale));
            return "redirect:/categorias/listar";
        } catch (RegraNegocioException e) {
            vincularErroNegocio(e, bindingResult, locale);
            carregarDadosListagem(new CategoriaFiltroDTO(), model);
            model.addAttribute("abrirModalForm", true);
            model.addAttribute("modoModal", "INSERIR");
            return "sistema/modulos/categoria/listar";
        }
    }

    @PutMapping("/categorias/editar/{id}")
    public String editar(@PathVariable("id") Long id,
                         @Valid @ModelAttribute("categoriaForm") CategoriaFormDTO form,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes,
                         Model model,
                         Locale locale) {
        form.setId(id);
        categoriaRepository.findById(id).ifPresent(c -> form.setSistema(c.isSistema()));
        new CategoriaValidator(categoriaRepository, messageSource, locale).validate(form, bindingResult);

        if (bindingResult.hasErrors()) {
            carregarDadosListagem(new CategoriaFiltroDTO(), model);
            model.addAttribute("abrirModalForm", true);
            model.addAttribute("modoModal", "EDITAR");
            return "sistema/modulos/categoria/listar";
        }

        try {
            categoriaService.editar(id, form);
            redirectAttributes.addFlashAttribute("sucessoMensagem",
                messageSource.getMessage("msg.categoria.atualizada", null, locale));
            return "redirect:/categorias/listar";
        } catch (RegraNegocioException e) {
            vincularErroNegocio(e, bindingResult, locale);
            carregarDadosListagem(new CategoriaFiltroDTO(), model);
            model.addAttribute("abrirModalForm", true);
            model.addAttribute("modoModal", "EDITAR");
            return "sistema/modulos/categoria/listar";
        }
    }

    @PutMapping("/categorias/desativar/{id}")
    public String desativar(@PathVariable("id") Long id, RedirectAttributes redirectAttributes, Locale locale) {
        categoriaService.desativar(id);
        redirectAttributes.addFlashAttribute("sucessoMensagem",
            messageSource.getMessage("msg.categoria.desativada", null, locale));
        return "redirect:/categorias/listar";
    }

    @DeleteMapping("/categorias/excluir/{id}")
    public String excluir(@PathVariable("id") Long id, RedirectAttributes redirectAttributes, Principal principal, Locale locale) {
        try {
            String usuario = principal != null ? principal.getName() : "SISTEMA";
            categoriaService.excluir(id, usuario);
            redirectAttributes.addFlashAttribute("sucessoMensagem",
                messageSource.getMessage("msg.categoria.excluida", null, locale));
        } catch (RegraNegocioException e) {
            String msg = messageSource.getMessage(e.getMessage(), null, locale);
            redirectAttributes.addFlashAttribute("erroMensagem", msg);
            if ("categoria.em-uso.bloqueada".equals(e.getMessage())) {
                redirectAttributes.addFlashAttribute("oferecerDesativarId", id);
            }
        }
        return "redirect:/categorias/listar";
    }

    @GetMapping("/categorias/opcoes")
    @ResponseBody
    public List<CategoriaOpcaoDTO> listarOpcoes(@RequestParam(value = "aplicaA", required = false) AplicaA aplicaA) {
        return categoriaService.listarOpcoesCombobox(aplicaA);
    }

    private void carregarDadosListagem(CategoriaFiltroDTO filtro, Model model) {
        List<CategoriaGridDTO> categorias = categoriaService.listar(filtro);
        model.addAttribute("categorias", categorias);
        model.addAttribute("aplicaAOpcoes", AplicaA.values());
        model.addAttribute("filtro", filtro);
    }

    private void vincularErroNegocio(RegraNegocioException e, BindingResult bindingResult, Locale locale) {
        String msg = messageSource.getMessage(e.getMessage(), null, locale);
        if (e.getCampo() != null && !e.getCampo().isBlank()) {
            bindingResult.rejectValue(e.getCampo(), "RegraNegocio." + e.getCampo(), msg);
        } else {
            bindingResult.reject("RegraNegocio", msg);
        }
    }
}
