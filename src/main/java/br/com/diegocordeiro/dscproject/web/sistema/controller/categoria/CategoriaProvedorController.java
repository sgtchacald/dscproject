package br.com.diegocordeiro.dscproject.web.sistema.controller.categoria;

import br.com.diegocordeiro.dscproject.dto.categoriaprovedor.CategoriaProvedorFormDTO;
import br.com.diegocordeiro.dscproject.dto.categoriaprovedor.CategoriaProvedorGridDTO;
import br.com.diegocordeiro.dscproject.repository.categoria.CategoriaProvedorRepository;
import br.com.diegocordeiro.dscproject.repository.categoria.CategoriaRepository;
import br.com.diegocordeiro.dscproject.repository.instituicaofinanceira.OpfiProvedorRepository;
import br.com.diegocordeiro.dscproject.service.categoria.CategoriaProvedorService;
import br.com.diegocordeiro.dscproject.service.exceptions.RegraNegocioException;
import br.com.diegocordeiro.dscproject.web.sistema.validator.categoria.CategoriaProvedorValidator;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;
import java.util.Locale;

@Controller
public class CategoriaProvedorController {

    private final CategoriaProvedorService categoriaProvedorService;
    private final CategoriaProvedorRepository categoriaProvedorRepository;
    private final CategoriaRepository categoriaRepository;
    private final OpfiProvedorRepository opfiProvedorRepository;
    private final MessageSource messageSource;

    public CategoriaProvedorController(CategoriaProvedorService categoriaProvedorService, CategoriaProvedorRepository categoriaProvedorRepository, CategoriaRepository categoriaRepository, OpfiProvedorRepository opfiProvedorRepository, MessageSource messageSource) {
        this.categoriaProvedorService = categoriaProvedorService;
        this.categoriaProvedorRepository = categoriaProvedorRepository;
        this.categoriaRepository = categoriaRepository;
        this.opfiProvedorRepository = opfiProvedorRepository;
        this.messageSource = messageSource;
    }

    @InitBinder
    public void binderComum(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    @GetMapping("/categorias-provedor/listar")
    public String listar(@RequestParam(value = "provedorId", required = false) Long provedorId, Model model) {
        carregarDadosListagem(provedorId, model);
        if (!model.containsAttribute("vinculoForm")) {
            model.addAttribute("vinculoForm", new CategoriaProvedorFormDTO());
        }
        return "sistema/modulos/categoria-provedor/listar";
    }

    @PostMapping("/categorias-provedor/inserir")
    public String inserir(@Valid @ModelAttribute("vinculoForm") CategoriaProvedorFormDTO form, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model, Locale locale) {
        new CategoriaProvedorValidator(categoriaProvedorRepository, categoriaRepository, opfiProvedorRepository, messageSource, locale)
            .validate(form, bindingResult);

        if (bindingResult.hasErrors()) {
            carregarDadosListagem(null, model);
            model.addAttribute("abrirModalForm", true);
            model.addAttribute("modoModal", "INSERIR");
            return "sistema/modulos/categoria-provedor/listar";
        }

        try {
            categoriaProvedorService.inserir(form);
            redirectAttributes.addFlashAttribute("sucessoMensagem",
                messageSource.getMessage("msg.categoriaprovedor.cadastrado", null, locale));
            return "redirect:/categorias-provedor/listar";
        } catch (RegraNegocioException e) {
            vincularErroNegocio(e, bindingResult, locale);
            carregarDadosListagem(null, model);
            model.addAttribute("abrirModalForm", true);
            model.addAttribute("modoModal", "INSERIR");
            return "sistema/modulos/categoria-provedor/listar";
        }
    }

    @PutMapping("/categorias-provedor/editar/{id}")
    public String editar(@PathVariable("id") Long id,
                         @Valid @ModelAttribute("vinculoForm") CategoriaProvedorFormDTO form,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes,
                         Model model,
                         Locale locale) {
        form.setId(id);
        new CategoriaProvedorValidator(categoriaProvedorRepository, categoriaRepository, opfiProvedorRepository, messageSource, locale)
            .validate(form, bindingResult);

        if (bindingResult.hasErrors()) {
            carregarDadosListagem(null, model);
            model.addAttribute("abrirModalForm", true);
            model.addAttribute("modoModal", "EDITAR");
            return "sistema/modulos/categoria-provedor/listar";
        }

        try {
            categoriaProvedorService.editar(id, form);
            redirectAttributes.addFlashAttribute("sucessoMensagem",
                messageSource.getMessage("msg.categoriaprovedor.atualizado", null, locale));
            return "redirect:/categorias-provedor/listar";
        } catch (RegraNegocioException e) {
            vincularErroNegocio(e, bindingResult, locale);
            carregarDadosListagem(null, model);
            model.addAttribute("abrirModalForm", true);
            model.addAttribute("modoModal", "EDITAR");
            return "sistema/modulos/categoria-provedor/listar";
        }
    }

    @DeleteMapping("/categorias-provedor/excluir/{id}")
    public String excluir(@PathVariable("id") Long id, RedirectAttributes redirectAttributes, Principal principal, Locale locale) {
        String usuario = principal != null ? principal.getName() : "SISTEMA";
        categoriaProvedorService.excluir(id, usuario);
        redirectAttributes.addFlashAttribute("sucessoMensagem",
            messageSource.getMessage("msg.categoriaprovedor.excluido", null, locale));
        return "redirect:/categorias-provedor/listar";
    }

    private void carregarDadosListagem(Long provedorId, Model model) {
        List<CategoriaProvedorGridDTO> vinculos = categoriaProvedorService.listar(provedorId);
        model.addAttribute("vinculos", vinculos);
        model.addAttribute("provedores", opfiProvedorRepository.findByAtivoTrueAndDataExclusaoIsNullOrderByNomeAsc());
        model.addAttribute("todosProvedores", opfiProvedorRepository.findByDataExclusaoIsNullOrderByNomeAsc());
        model.addAttribute("categorias", categoriaRepository.findByAtivoTrueAndDataExclusaoIsNullOrderByNomeAsc());
        model.addAttribute("provedorIdFiltro", provedorId);
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
