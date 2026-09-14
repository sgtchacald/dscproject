package br.com.diegocordeiro.dscproject.web.sistema.controller.perfil;

import br.com.diegocordeiro.dscproject.dto.permissao.PermissaoCatalogoDTO;
import br.com.diegocordeiro.dscproject.dto.catalogo.SincronizacaoCatalogoDTO;
import br.com.diegocordeiro.dscproject.service.perfil.PermissaoCatalogoService;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Fronteira web do catálogo de permissões: leitura e sincronização com o código. */
@Controller
public class PermissaoController {

    private final PermissaoCatalogoService permissaoCatalogoService;
    private final MessageSource messageSource;

    public PermissaoController(PermissaoCatalogoService permissaoCatalogoService, MessageSource messageSource) {
        this.permissaoCatalogoService = permissaoCatalogoService;
        this.messageSource = messageSource;
    }

    @GetMapping("/permissoes/listar-dados")
    @ResponseBody
    public List<PermissaoCatalogoDTO> listarDados() {
        return permissaoCatalogoService.listar();
    }

    @PostMapping("/permissoes/sincronizar-catalogo")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> sincronizarCatalogo(Locale locale) {
        SincronizacaoCatalogoDTO resultado = permissaoCatalogoService.sincronizar();
        String mensagem = messageSource.getMessage("msg.permissao.catalogo.sincronizado", new Object[]{resultado.inseridas(), resultado.orfas()}, locale);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("sucesso", true);
        body.put("mensagem", mensagem);
        body.put("inseridas", resultado.inseridas());
        body.put("orfas", resultado.orfas());
        return ResponseEntity.ok(body);
    }
}
