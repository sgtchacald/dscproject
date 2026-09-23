package br.com.diegocordeiro.dscproject.web.sistema.controller.openfinance;

import br.com.diegocordeiro.dscproject.dto.openfinance.OpenFinanceCallbackConexaoDTO;
import br.com.diegocordeiro.dscproject.dto.openfinance.OpenFinanceConexaoGridDTO;
import br.com.diegocordeiro.dscproject.dto.openfinance.OpenFinanceConnectTokenRequestDTO;
import br.com.diegocordeiro.dscproject.dto.openfinance.OpenFinanceContaExternaDTO;
import br.com.diegocordeiro.dscproject.dto.openfinance.OpenFinanceCredencialDTO;
import br.com.diegocordeiro.dscproject.dto.openfinance.OpenFinanceCredencialExibicaoDTO;
import br.com.diegocordeiro.dscproject.dto.openfinance.OpenFinanceSincronizacaoDTO;
import br.com.diegocordeiro.dscproject.dto.openfinance.OpenFinanceVinculoContaDTO;
import br.com.diegocordeiro.dscproject.enums.TipoSincronizacao;
import br.com.diegocordeiro.dscproject.model.instituicaofinanceira.OpenFinanceProvedor;
import br.com.diegocordeiro.dscproject.model.usuario.Usuario;
import br.com.diegocordeiro.dscproject.repository.instituicaofinanceira.OpenFinanceProvedorRepository;
import br.com.diegocordeiro.dscproject.repository.usuario.UsuarioRepository;
import br.com.diegocordeiro.dscproject.service.exceptions.RegistroNaoEncontradoException;
import br.com.diegocordeiro.dscproject.service.openfinance.OpenFinanceConexaoService;
import br.com.diegocordeiro.dscproject.service.openfinance.OpenFinanceCredencialService;
import br.com.diegocordeiro.dscproject.service.openfinance.OpenFinanceSincronizacaoService;
import br.com.diegocordeiro.dscproject.util.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Controller
public class OpenFinanceController {

    private final OpenFinanceConexaoService conexaoService;
    private final OpenFinanceCredencialService credencialService;
    private final OpenFinanceSincronizacaoService sincronizacaoService;
    private final OpenFinanceProvedorRepository provedorRepository;
    private final UsuarioRepository usuarioRepository;
    private final MessageSource messageSource;

    public OpenFinanceController(
            OpenFinanceConexaoService conexaoService,
            OpenFinanceCredencialService credencialService,
            OpenFinanceSincronizacaoService sincronizacaoService,
            OpenFinanceProvedorRepository provedorRepository,
            UsuarioRepository usuarioRepository,
            MessageSource messageSource
    ) {
        this.conexaoService = conexaoService;
        this.credencialService = credencialService;
        this.sincronizacaoService = sincronizacaoService;
        this.provedorRepository = provedorRepository;
        this.usuarioRepository = usuarioRepository;
        this.messageSource = messageSource;
    }

    @GetMapping("/open-finance/conexoes")
    public String listar(Model model) {
        List<OpenFinanceProvedor> provedores = provedorRepository.findByAtivoTrueAndDataExclusaoIsNullOrderByNomeAsc();
        model.addAttribute("provedores", provedores);
        return "sistema/modulos/open-finance/listar";
    }

    @GetMapping("/open-finance/dados")
    @ResponseBody
    public List<OpenFinanceConexaoGridDTO> listarDados(Principal principal) {
        Usuario usuario = obterUsuarioAutenticado(principal);
        return conexaoService.listarConexoes(usuario.getId());
    }

    @GetMapping("/open-finance/provedores/ativos")
    @ResponseBody
    public List<OpenFinanceProvedor> listarProvedoresAtivos() {
        return provedorRepository.findByAtivoTrueAndDataExclusaoIsNullOrderByNomeAsc();
    }

    @GetMapping("/open-finance/credenciais")
    @ResponseBody
    public OpenFinanceCredencialExibicaoDTO buscarCredenciais(@RequestParam(required = false) Long provedorId, Principal principal) {
        Usuario usuario = obterUsuarioAutenticado(principal);
        Long idProvedor = provedorId;
        if (idProvedor == null) {
            OpenFinanceProvedor defaultProv = provedorRepository.findByCodigo("PLUGGY")
                    .orElseThrow(() -> new RegistroNaoEncontradoException("openfinance.provedor.nao-encontrado"));
            idProvedor = defaultProv.getId();
        }
        return credencialService.buscarCredencialExibicao(usuario.getId(), idProvedor);
    }

    @PostMapping("/open-finance/credenciais/testar")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> testarCredencial(@RequestBody @Valid OpenFinanceCredencialDTO dto, Locale locale) {
        boolean valida = credencialService.testarCredencial(dto);
        if (valida) {
            return ResponseEntity.ok(Map.of("sucesso", true, "mensagem", mensagem("msg.openfinance.credenciais.validas", locale)));
        } else {
            return ResponseEntity.unprocessableEntity().body(Map.of("sucesso", false, "mensagem", mensagem("msg.openfinance.credenciais.invalidas", locale)));
        }
    }

    @PostMapping("/open-finance/credenciais")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> salvarCredencial(@RequestBody @Valid OpenFinanceCredencialDTO dto, Principal principal, Locale locale) {
        Usuario usuario = obterUsuarioAutenticado(principal);
        credencialService.salvarCredencial(usuario.getId(), dto);
        return ResponseEntity.ok(Map.of("sucesso", true, "mensagem", mensagem("msg.openfinance.credenciais.salvas", locale)));
    }

    @PostMapping("/open-finance/connect-token")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> gerarConnectToken(@RequestBody(required = false) OpenFinanceConnectTokenRequestDTO request, Principal principal) {
        Usuario usuario = obterUsuarioAutenticado(principal);
        Long conexaoId = request != null ? request.getConexaoId() : null;
        String token = conexaoService.gerarConnectToken(usuario.getId(), conexaoId);
        return ResponseEntity.ok(Map.of("sucesso", true, "connectToken", token));
    }

    @PostMapping("/open-finance/conexoes/callback")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> callback(@RequestBody @Valid OpenFinanceCallbackConexaoDTO dto, Principal principal, Locale locale) {
        Usuario usuario = obterUsuarioAutenticado(principal);
        conexaoService.processarCallback(usuario.getId(), dto.getItemId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("sucesso", true, "mensagem", mensagem("msg.openfinance.conexao.criada", locale)));
    }

    @PostMapping("/open-finance/conexoes/{id}/sincronizar")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> sincronizar(@PathVariable Long id, Principal principal, Locale locale) {
        Usuario usuario = obterUsuarioAutenticado(principal);
        sincronizacaoService.sincronizarConexao(id, TipoSincronizacao.MANUAL, usuario.getId());
        return ResponseEntity.ok(Map.of("sucesso", true, "mensagem", mensagem("msg.openfinance.sincronizacao.iniciada", locale)));
    }

    @DeleteMapping("/open-finance/conexoes/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> desconectar(@PathVariable Long id, Principal principal, Locale locale) {
        Usuario usuario = obterUsuarioAutenticado(principal);
        conexaoService.desconectar(usuario.getId(), id);
        return ResponseEntity.ok(Map.of("sucesso", true, "mensagem", mensagem("msg.openfinance.conexao.desconectada", locale)));
    }

    @PutMapping("/open-finance/contas-externas/{id}/vincular")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> vincular(@PathVariable Long id, @RequestBody OpenFinanceVinculoContaDTO dto, Principal principal, Locale locale) {
        Usuario usuario = obterUsuarioAutenticado(principal);
        conexaoService.vincularContaExterna(usuario.getId(), id, dto.getContaId(), dto.getCartaoId());
        return ResponseEntity.ok(Map.of("sucesso", true, "mensagem", mensagem("msg.openfinance.vinculo.atualizado", locale)));
    }

    @GetMapping("/open-finance/conexoes/{id}/historico")
    @ResponseBody
    public Page<OpenFinanceSincronizacaoDTO> historico(@PathVariable Long id, @PageableDefault(size = 10) Pageable pageable, Principal principal) {
        Usuario usuario = obterUsuarioAutenticado(principal);
        return sincronizacaoService.buscarHistorico(usuario.getId(), id, pageable);
    }

    @GetMapping("/open-finance/conexoes/{id}/contas")
    @ResponseBody
    public List<OpenFinanceContaExternaDTO> contasPorConexao(@PathVariable Long id, Principal principal) {
        Usuario usuario = obterUsuarioAutenticado(principal);
        return conexaoService.buscarContasExternasPorConexao(usuario.getId(), id);
    }

    private Usuario obterUsuarioAutenticado(Principal principal) {
        String login = principal != null ? principal.getName() : SecurityUtils.loginAtual();
        return usuarioRepository.findByLogin(login)
                .orElseThrow(() -> new RegistroNaoEncontradoException("msg.usuario.nao-encontrado"));
    }

    private String mensagem(String chave, Locale locale, Object... args) {
        return messageSource.getMessage(chave, args, locale);
    }
}
