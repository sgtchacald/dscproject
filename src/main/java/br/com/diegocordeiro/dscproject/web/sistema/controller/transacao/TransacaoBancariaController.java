package br.com.diegocordeiro.dscproject.web.sistema.controller.transacao;

import br.com.diegocordeiro.dscproject.dto.conta.ContaOpcaoDTO;
import br.com.diegocordeiro.dscproject.dto.transacao.ResultadoImportacaoOFXDTO;
import br.com.diegocordeiro.dscproject.dto.transacao.TransacaoBancariaEdicaoDTO;
import br.com.diegocordeiro.dscproject.dto.transacao.TransacaoBancariaFiltroDTO;
import br.com.diegocordeiro.dscproject.dto.transacao.TransacaoBancariaFormDTO;
import br.com.diegocordeiro.dscproject.dto.transacao.TransacaoBancariaGridDTO;
import br.com.diegocordeiro.dscproject.dto.transacao.TransacaoBancariaTotaisDTO;
import br.com.diegocordeiro.dscproject.enums.NaturezaMovimento;
import br.com.diegocordeiro.dscproject.model.usuario.Usuario;
import br.com.diegocordeiro.dscproject.repository.categoria.CategoriaRepository;
import br.com.diegocordeiro.dscproject.repository.conta.ContaRepository;
import br.com.diegocordeiro.dscproject.repository.transacao.TransacaoBancariaRepository;
import br.com.diegocordeiro.dscproject.repository.usuario.UsuarioRepository;
import br.com.diegocordeiro.dscproject.service.conta.ContaService;
import br.com.diegocordeiro.dscproject.service.exceptions.RegistroNaoEncontradoException;
import br.com.diegocordeiro.dscproject.service.transacao.TransacaoBancariaImportacaoService;
import br.com.diegocordeiro.dscproject.service.transacao.TransacaoBancariaService;
import br.com.diegocordeiro.dscproject.util.SecurityUtils;
import br.com.diegocordeiro.dscproject.web.sistema.validator.transacao.TransacaoBancariaValidator;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.context.MessageSource;
import org.springframework.format.annotation.DateTimeFormat;
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
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Controller
public class TransacaoBancariaController {

    private final TransacaoBancariaService transacaoBancariaService;
    private final TransacaoBancariaImportacaoService transacaoBancariaImportacaoService;
    private final TransacaoBancariaRepository transacaoBancariaRepository;
    private final ContaRepository contaRepository;
    private final ContaService contaService;
    private final CategoriaRepository categoriaRepository;
    private final UsuarioRepository usuarioRepository;
    private final MessageSource messageSource;
    private final SmartValidator smartValidator;

    public TransacaoBancariaController(TransacaoBancariaService transacaoBancariaService,
                                       TransacaoBancariaImportacaoService transacaoBancariaImportacaoService,
                                       TransacaoBancariaRepository transacaoBancariaRepository,
                                       ContaRepository contaRepository,
                                       ContaService contaService,
                                       CategoriaRepository categoriaRepository,
                                       UsuarioRepository usuarioRepository,
                                       MessageSource messageSource,
                                       SmartValidator smartValidator) {
        this.transacaoBancariaService = transacaoBancariaService;
        this.transacaoBancariaImportacaoService = transacaoBancariaImportacaoService;
        this.transacaoBancariaRepository = transacaoBancariaRepository;
        this.contaRepository = contaRepository;
        this.contaService = contaService;
        this.categoriaRepository = categoriaRepository;
        this.usuarioRepository = usuarioRepository;
        this.messageSource = messageSource;
        this.smartValidator = smartValidator;
    }

    @InitBinder
    public void binderComum(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    @GetMapping("/transacoes-bancarias/listar")
    public String listar(Model model, Principal principal) {
        Usuario usuario = obterUsuarioAutenticado(principal);
        YearMonth mesAnterior = YearMonth.now().minusMonths(1);

        List<ContaOpcaoDTO> contas = contaService.listarOpcoesCombobox(usuario.getId());
        model.addAttribute("contas", contas);
        model.addAttribute("competenciaPadrao", mesAnterior.toString());
        model.addAttribute("naturezas", NaturezaMovimento.values());
        return "sistema/modulos/transacao-bancaria/listar";
    }

    @GetMapping("/transacoes-bancarias/listar-dados")
    @ResponseBody
    public Map<String, Object> listarDados(TransacaoBancariaFiltroDTO filtro, Principal principal) {
        Usuario usuario = obterUsuarioAutenticado(principal);
        if (filtro.getCompetenciaInicio() == null && filtro.getCompetenciaFim() == null) {
            String padrao = YearMonth.now().minusMonths(1).toString();
            filtro.setCompetenciaInicio(padrao);
            filtro.setCompetenciaFim(padrao);
        }

        List<TransacaoBancariaGridDTO> lista = transacaoBancariaService.listar(filtro, usuario.getId());
        TransacaoBancariaTotaisDTO totais = transacaoBancariaService.calcularTotais(filtro, usuario.getId());

        return Map.of(
            "dados", lista,
            "totais", totais
        );
    }

    @GetMapping("/transacoes-bancarias/buscar/{id}")
    @ResponseBody
    public ResponseEntity<TransacaoBancariaEdicaoDTO> buscar(@PathVariable Long id, Principal principal) {
        Usuario usuario = obterUsuarioAutenticado(principal);
        return ResponseEntity.ok(transacaoBancariaService.buscarParaEdicao(id, usuario.getId()));
    }

    @PostMapping("/transacoes-bancarias/inserir")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> inserir(@ModelAttribute TransacaoBancariaFormDTO dto, Principal principal, Locale locale) {
        Usuario usuario = obterUsuarioAutenticado(principal);
        dto.setId(null);

        BindingResult resultado = validar(dto, usuario.getId(), locale);
        if (resultado.hasErrors()) {
            return respostaErros(resultado);
        }

        transacaoBancariaService.inserir(dto, usuario.getId(), usuario.getLogin());
        return ResponseEntity.ok(Map.of("sucesso", true, "mensagem", mensagem("msg.transacao.cadastrada", locale)));
    }

    @PutMapping("/transacoes-bancarias/editar/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> editar(@PathVariable Long id, @ModelAttribute TransacaoBancariaFormDTO dto, Principal principal, Locale locale) {
        Usuario usuario = obterUsuarioAutenticado(principal);
        dto.setId(id);

        BindingResult resultado = validar(dto, usuario.getId(), locale);
        if (resultado.hasErrors()) {
            return respostaErros(resultado);
        }

        transacaoBancariaService.editar(id, dto, usuario.getId(), usuario.getLogin());
        return ResponseEntity.ok(Map.of("sucesso", true, "mensagem", mensagem("msg.transacao.atualizada", locale)));
    }

    @DeleteMapping("/transacoes-bancarias/excluir/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> excluir(@PathVariable Long id, Principal principal, Locale locale) {
        Usuario usuario = obterUsuarioAutenticado(principal);
        transacaoBancariaService.excluir(id, usuario.getId(), usuario.getLogin());
        return ResponseEntity.ok(Map.of("sucesso", true, "mensagem", mensagem("msg.transacao.excluida", locale)));
    }

    @PostMapping("/transacoes-bancarias/duplicar/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> duplicar(@PathVariable Long id, @RequestParam(required = false) String novaCompetencia, @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate novaData, Principal principal, Locale locale) {
        Usuario usuario = obterUsuarioAutenticado(principal);
        transacaoBancariaService.duplicar(id, novaCompetencia, novaData, usuario.getId(), usuario.getLogin());
        return ResponseEntity.ok(Map.of("sucesso", true, "mensagem", mensagem("msg.transacao.duplicada", locale)));
    }

    @PostMapping("/transacoes-bancarias/importar-ofx")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> importarOFX(@RequestParam("arquivo") MultipartFile arquivo, @RequestParam("contaId") Long contaId, Principal principal, Locale locale) {
        Usuario usuario = obterUsuarioAutenticado(principal);
        ResultadoImportacaoOFXDTO res = transacaoBancariaImportacaoService.importarOFX(arquivo, contaId, usuario.getId());
        String msg = messageSource.getMessage("msg.transacao.importar.sucesso",
            new Object[]{res.getTotalImportados(), res.getTotalIgnorados()}, locale);
        return ResponseEntity.ok(Map.of(
            "sucesso", true,
            "mensagem", msg,
            "totalLidos", res.getTotalLidos(),
            "totalImportados", res.getTotalImportados(),
            "totalIgnorados", res.getTotalIgnorados()
        ));
    }

    private BindingResult validar(TransacaoBancariaFormDTO dto, Long usuarioId, Locale locale) {
        BindingResult resultado = new BeanPropertyBindingResult(dto, "transacaoBancariaFormDTO");
        smartValidator.validate(dto, resultado);
        new TransacaoBancariaValidator(transacaoBancariaRepository, contaRepository, categoriaRepository, messageSource, locale, usuarioId)
            .validate(dto, resultado);
        return resultado;
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
