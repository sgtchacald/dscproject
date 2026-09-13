package br.com.diegocordeiro.dscproject.web.sistema.controller.despesa;

import br.com.diegocordeiro.dscproject.dto.despesa.ContatoRapidoDTO;
import br.com.diegocordeiro.dscproject.dto.despesa.ContatoRateioDTO;
import br.com.diegocordeiro.dscproject.dto.despesa.DespesaEdicaoDTO;
import br.com.diegocordeiro.dscproject.dto.despesa.DespesaFormDTO;
import br.com.diegocordeiro.dscproject.dto.despesa.DespesaGridDTO;
import br.com.diegocordeiro.dscproject.dto.despesa.DespesaPagamentoDTO;
import br.com.diegocordeiro.dscproject.dto.despesa.DespesaPagamentoLoteDTO;
import br.com.diegocordeiro.dscproject.dto.despesa.DespesaRateioAcertoDTO;
import br.com.diegocordeiro.dscproject.dto.despesa.UsuarioRateioDTO;
import br.com.diegocordeiro.dscproject.model.despesa.Despesa;
import br.com.diegocordeiro.dscproject.model.usuario.Usuario;
import br.com.diegocordeiro.dscproject.config.permissao.PermissaoDespesaCatalogo;
import br.com.diegocordeiro.dscproject.repository.cartao.CartaoCreditoRepository;
import br.com.diegocordeiro.dscproject.repository.categoria.CategoriaRepository;
import br.com.diegocordeiro.dscproject.repository.conta.ContaRepository;
import br.com.diegocordeiro.dscproject.repository.contato.ContatoRepository;
import br.com.diegocordeiro.dscproject.repository.despesa.DespesaRepository;
import br.com.diegocordeiro.dscproject.repository.usuario.UsuarioRepository;
import br.com.diegocordeiro.dscproject.service.despesa.DespesaService;
import br.com.diegocordeiro.dscproject.service.exceptions.RegistroNaoEncontradoException;
import br.com.diegocordeiro.dscproject.util.SecurityUtils;
import br.com.diegocordeiro.dscproject.web.sistema.validator.despesa.DespesaValidator;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import br.com.diegocordeiro.dscproject.service.despesa.DespesaImportacaoService;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Controller
public class DespesaController {

    private final DespesaService despesaService;
    private final DespesaImportacaoService despesaImportacaoService;
    private final DespesaRepository despesaRepository;
    private final ContaRepository contaRepository;
    private final CartaoCreditoRepository cartaoCreditoRepository;
    private final CategoriaRepository categoriaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ContatoRepository contatoRepository;
    private final MessageSource messageSource;
    private final SmartValidator smartValidator;

    public DespesaController(
            DespesaService despesaService,
            DespesaImportacaoService despesaImportacaoService,
            DespesaRepository despesaRepository,
            ContaRepository contaRepository,
            CartaoCreditoRepository cartaoCreditoRepository,
            CategoriaRepository categoriaRepository,
            UsuarioRepository usuarioRepository,
            ContatoRepository contatoRepository,
            MessageSource messageSource,
            SmartValidator smartValidator) {
        this.despesaService = despesaService;
        this.despesaImportacaoService = despesaImportacaoService;
        this.despesaRepository = despesaRepository;
        this.contaRepository = contaRepository;
        this.cartaoCreditoRepository = cartaoCreditoRepository;
        this.categoriaRepository = categoriaRepository;
        this.usuarioRepository = usuarioRepository;
        this.contatoRepository = contatoRepository;
        this.messageSource = messageSource;
        this.smartValidator = smartValidator;
    }

    @InitBinder
    public void binderComum(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    @GetMapping("/despesas/listar")
    public String listar(Model model, Principal principal) {
        boolean podeRatear = usuarioPodeRatear(principal);
        model.addAttribute("podeRatear", podeRatear);
        return "sistema/modulos/despesa/listar";
    }

    @GetMapping("/despesas/listar-dados")
    @ResponseBody
    public List<DespesaGridDTO> listarDados(Principal principal) {
        Usuario usuario = obterUsuarioAutenticado(principal);
        return despesaService.listarParaGrid(usuario.getId());
    }

    @PostMapping("/despesas/inserir")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> inserir(@ModelAttribute DespesaFormDTO dto, Principal principal, Locale locale) {
        Usuario usuario = obterUsuarioAutenticado(principal);
        dto.setId(null);
        boolean podeRatear = usuarioPodeRatear(principal);

        BindingResult resultado = validar(dto, usuario.getId(), podeRatear, locale);
        if (resultado.hasErrors()) {
            return respostaErros(resultado);
        }

        Despesa salva = despesaService.inserir(dto, usuario.getId(), usuario.getLogin(), podeRatear);
        String mensagemRetorno;
        if (salva.isParcelada()) {
            mensagemRetorno = messageSource.getMessage("msg.despesa.parcelada.sucesso", new Object[]{salva.getQtdParcelas()}, locale);
        } else if (salva.isRecorrente()) {
            int meses = dto.getQtdMesesRecorrencia() != null ? dto.getQtdMesesRecorrencia() : 12;
            mensagemRetorno = messageSource.getMessage("msg.despesa.recorrente.sucesso", new Object[]{meses}, locale);
        } else {
            mensagemRetorno = mensagem("msg.despesa.cadastrada", locale);
        }
        return ResponseEntity.ok(Map.of("sucesso", true, "mensagem", mensagemRetorno));
    }

    @GetMapping("/despesas/buscar/{id}")
    @ResponseBody
    public DespesaEdicaoDTO buscar(@PathVariable Long id, Principal principal) {
        Usuario usuario = obterUsuarioAutenticado(principal);
        return despesaService.buscarParaEdicao(id, usuario.getId());
    }

    @PutMapping("/despesas/editar/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> editar(@PathVariable Long id, @ModelAttribute DespesaFormDTO dto, Principal principal, Locale locale) {
        Usuario usuario = obterUsuarioAutenticado(principal);
        despesaService.buscarPorIdEUsuario(id, usuario.getId());
        dto.setId(id);
        boolean podeRatear = usuarioPodeRatear(principal);

        BindingResult resultado = validar(dto, usuario.getId(), podeRatear, locale);
        if (resultado.hasErrors()) {
            return respostaErros(resultado);
        }

        despesaService.editar(id, dto, usuario.getId(), usuario.getLogin(), podeRatear);
        return ResponseEntity.ok(Map.of("sucesso", true, "mensagem", mensagem("msg.despesa.atualizada", locale)));
    }

    @DeleteMapping("/despesas/excluir/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> excluir(@PathVariable Long id, Principal principal, Locale locale) {
        Usuario usuario = obterUsuarioAutenticado(principal);
        despesaService.excluir(id, usuario.getId(), usuario.getLogin());
        return ResponseEntity.ok(Map.of("sucesso", true, "mensagem", mensagem("msg.despesa.excluida", locale)));
    }

    @PutMapping("/despesas/registrar-pagamento/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> registrarPagamento(@PathVariable Long id, @ModelAttribute DespesaPagamentoDTO dto, Principal principal, Locale locale) {
        Usuario usuario = obterUsuarioAutenticado(principal);

        BindingResult resultado = new BeanPropertyBindingResult(dto, "despesaPagamentoDTO");
        smartValidator.validate(dto, resultado);
        if (resultado.hasErrors()) {
            return respostaErros(resultado);
        }

        despesaService.registrarPagamento(id, dto.getDataPagamento(), usuario.getId(), usuario.getLogin());
        return ResponseEntity.ok(Map.of("sucesso", true, "mensagem", mensagem("msg.despesa.pagamento.registrado", locale)));
    }

    @PostMapping("/despesas/registrar-pagamento-lote")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> registrarPagamentoLote(@ModelAttribute DespesaPagamentoLoteDTO dto, Principal principal, Locale locale) {
        Usuario usuario = obterUsuarioAutenticado(principal);

        BindingResult resultado = new BeanPropertyBindingResult(dto, "despesaPagamentoLoteDTO");
        smartValidator.validate(dto, resultado);
        if (resultado.hasErrors()) {
            return respostaErros(resultado);
        }

        int alteradas = despesaService.registrarPagamentoLote(dto.getIds(), dto.getDataPagamento(), usuario.getId(), usuario.getLogin());
        String msg = messageSource.getMessage("msg.despesa.pagamento-lote.registrado", new Object[]{alteradas}, locale);
        return ResponseEntity.ok(Map.of("sucesso", true, "mensagem", msg));
    }

    @PutMapping("/despesas/{id}/rateio-acerto")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> registrarAcertoRateio(@PathVariable Long id, @ModelAttribute DespesaRateioAcertoDTO dto, Principal principal, Locale locale) {
        Usuario usuario = obterUsuarioAutenticado(principal);

        BindingResult resultado = new BeanPropertyBindingResult(dto, "despesaRateioAcertoDTO");
        smartValidator.validate(dto, resultado);
        if (resultado.hasErrors()) {
            return respostaErros(resultado);
        }

        despesaService.registrarAcertoRateio(id, dto.getContatoId(), dto.isAcertado(), dto.getDataAcerto(), usuario.getId(), usuario.getLogin());
        return ResponseEntity.ok(Map.of("sucesso", true, "mensagem", mensagem("msg.despesa.rateio.acerto-registrado", locale)));
    }

    @GetMapping("/despesas/usuarios-rateio")
    @ResponseBody
    public List<UsuarioRateioDTO> buscarUsuariosRateio(@RequestParam(required = false, defaultValue = "") String termo, Principal principal) {
        Usuario usuario = obterUsuarioAutenticado(principal);
        return despesaService.buscarUsuariosParaRateio(termo, usuario.getId());
    }

    @GetMapping("/despesas/contatos-rateio")
    @ResponseBody
    public List<ContatoRateioDTO> buscarContatosRateio(@RequestParam(required = false, defaultValue = "") String termo, Principal principal) {
        Usuario usuario = obterUsuarioAutenticado(principal);
        return despesaService.buscarContatosParaRateio(termo, usuario.getId());
    }

    @PostMapping("/despesas/contatos-rapido")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> cadastrarContatoRapido(@ModelAttribute ContatoRapidoDTO dto, Principal principal, Locale locale) {
        Usuario usuario = obterUsuarioAutenticado(principal);

        BindingResult resultado = new BeanPropertyBindingResult(dto, "contatoRapidoDTO");
        smartValidator.validate(dto, resultado);
        if (resultado.hasErrors()) {
            return respostaErros(resultado);
        }

        ContatoRateioDTO contatoSalvo = despesaService.cadastrarContatoRapido(dto, usuario.getId(), usuario.getLogin());
        return ResponseEntity.ok(Map.of(
                "sucesso", true,
                "mensagem", mensagem("msg.despesa.rateio.contato-cadastrado", locale),
                "contato", contatoSalvo));
    }

    @PostMapping("/despesas/duplicar")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> duplicar(@RequestParam(value = "despesaIds", required = false) List<Long> despesaIds, @RequestParam(value = "ids", required = false) List<Long> ids, @RequestParam(value = "competenciaDestino", required = false) String competenciaDestino, Principal principal, Locale locale) {
        List<Long> targetIds = despesaIds != null && !despesaIds.isEmpty() ? despesaIds : ids;
        if (targetIds == null || targetIds.isEmpty()) {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("sucesso", false);
            body.put("errosCampos", Map.of());
            body.put("errosNegocio", Map.of("despesaIds", mensagem("msg.despesa.duplicar.vazio", locale)));
            return ResponseEntity.unprocessableEntity().body(body);
        }
        Usuario usuario = obterUsuarioAutenticado(principal);
        despesaService.duplicar(targetIds, competenciaDestino, usuario.getId(), usuario.getLogin());
        return ResponseEntity.ok(Map.of("sucesso", true, "mensagem", mensagem("msg.despesa.duplicada", locale)));
    }

    @PatchMapping("/despesas/{id}/valor")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> atualizarValor(@PathVariable Long id, @RequestParam(value = "valor", required = false) BigDecimal valor, Principal principal, Locale locale) {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("sucesso", false);
            body.put("errosCampos", Map.of("valor", mensagem("msg.despesa.valor-invalido", locale)));
            body.put("errosNegocio", Map.of());
            return ResponseEntity.unprocessableEntity().body(body);
        }
        Usuario usuario = obterUsuarioAutenticado(principal);
        Despesa d = despesaService.atualizarValor(id, valor, usuario.getId(), usuario.getLogin());
        return ResponseEntity.ok(Map.of(
                "sucesso", true,
                "mensagem", mensagem("msg.despesa.valor-atualizado", locale),
                "id", d.getId(),
                "valor", d.getValor()));
    }

    @PatchMapping("/despesas/{id}/competencia")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> atualizarCompetencia(@PathVariable Long id, @RequestParam(value = "competencia", required = false) String competencia, Principal principal, Locale locale) {
        if (competencia == null || competencia.isBlank()) {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("sucesso", false);
            body.put("errosCampos", Map.of("competencia", mensagem("msg.despesa.competencia-invalida", locale)));
            body.put("errosNegocio", Map.of());
            return ResponseEntity.unprocessableEntity().body(body);
        }
        try {
            YearMonth.parse(competencia.trim());
        } catch (DateTimeParseException e) {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("sucesso", false);
            body.put("errosCampos", Map.of("competencia", mensagem("msg.despesa.competencia-invalida", locale)));
            body.put("errosNegocio", Map.of());
            return ResponseEntity.unprocessableEntity().body(body);
        }
        Usuario usuario = obterUsuarioAutenticado(principal);
        Despesa d = despesaService.atualizarCompetencia(id, competencia.trim(), usuario.getId(), usuario.getLogin());
        return ResponseEntity.ok(Map.of(
                "sucesso", true,
                "mensagem", mensagem("msg.despesa.competencia-atualizada", locale),
                "id", d.getId(),
                "competencia", d.getCompetencia() != null ? d.getCompetencia().toString() : ""));
    }

    @PatchMapping("/despesas/{id}/categoria")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> atualizarCategoria(@PathVariable Long id, @RequestParam(value = "categoriaId", required = false) Long categoriaId, Principal principal, Locale locale) {
        Usuario usuario = obterUsuarioAutenticado(principal);
        Despesa d = despesaService.atualizarCategoria(id, categoriaId, usuario.getId(), usuario.getLogin());
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("sucesso", true);
        resp.put("mensagem", mensagem("msg.despesa.categoria-atualizada", locale));
        resp.put("id", d.getId());
        resp.put("categoriaId", d.getCategoria() != null ? d.getCategoria().getId() : null);
        resp.put("categoriaNome", d.getCategoria() != null ? d.getCategoria().getNome() : "");
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/despesas/importar-extrato")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> importarExtrato(
            @RequestParam("arquivo") MultipartFile arquivo,
            @RequestParam("cartaoId") Long cartaoId,
            @RequestParam("competencia") String competencia,
            @RequestParam("formato") String formato,
            @RequestParam(value = "dtVencimento", required = false) String dtVencimento,
            @RequestParam(value = "categoriaId", required = false) Long categoriaId,
            Principal principal,
            Locale locale) {
        Usuario usuario = obterUsuarioAutenticado(principal);
        int total = despesaImportacaoService.importarFatura(arquivo, cartaoId, competencia, formato, dtVencimento, categoriaId, usuario.getId(), usuario.getLogin());
        String msg = messageSource.getMessage("msg.despesa.importacao.sucesso", new Object[]{total}, locale);
        return ResponseEntity.ok(Map.of("sucesso", true, "mensagem", msg, "total", total));
    }

    private boolean usuarioPodeRatear(Principal principal) {
        if (principal instanceof Authentication auth) {
            return auth.getAuthorities().stream()
                    .anyMatch(a -> PermissaoDespesaCatalogo.DESPESA_RATEAR_MULTIUSUARIO.getAuthority().equals(a.getAuthority()));
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            return auth.getAuthorities().stream()
                    .anyMatch(a -> PermissaoDespesaCatalogo.DESPESA_RATEAR_MULTIUSUARIO.getAuthority().equals(a.getAuthority()));
        }
        return false;
    }

    private Usuario obterUsuarioAutenticado(Principal principal) {
        String login = principal != null ? principal.getName() : SecurityUtils.loginAtual();
        return usuarioRepository.findByLogin(login)
                .orElseThrow(() -> new RegistroNaoEncontradoException("usuario.nao.encontrado"));
    }

    private BindingResult validar(DespesaFormDTO dto, Long usuarioId, boolean podeRatear, Locale locale) {
        BindingResult resultado = new BeanPropertyBindingResult(dto, "despesaFormDTO");
        smartValidator.validate(dto, resultado);
        new DespesaValidator(
                despesaRepository,
                contaRepository,
                cartaoCreditoRepository,
                categoriaRepository,
                contatoRepository,
                messageSource,
                locale,
                usuarioId,
                podeRatear)
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
