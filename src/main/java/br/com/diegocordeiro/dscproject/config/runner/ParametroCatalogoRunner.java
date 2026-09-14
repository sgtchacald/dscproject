package br.com.diegocordeiro.dscproject.config.runner;

import br.com.diegocordeiro.dscproject.dto.catalogo.SincronizacaoCatalogoDTO;
import br.com.diegocordeiro.dscproject.config.parametro.ParametrosGlobaisCatalogo;
import br.com.diegocordeiro.dscproject.repository.parametro.ParametroGlobalRepository;
import br.com.diegocordeiro.dscproject.service.parametro.ParametroCatalogoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Sincroniza o catálogo de parâmetros globais (código -> tabela) na subida da
 * aplicação, quando o parâmetro {@code PARAMETROS_SYNC_CATALOGO_NA_INICIALIZACAO}
 * está {@code true}. Ausente (primeira subida) conta como habilitado, para a
 * própria linha de controle ser semeada. Roda depois da carga inicial do RBAC.
 */
@Component
@Order(20)
public class ParametroCatalogoRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(ParametroCatalogoRunner.class);

    private final ParametroGlobalRepository parametroGlobalRepository;
    private final ParametroCatalogoService parametroCatalogoService;

    public ParametroCatalogoRunner(ParametroGlobalRepository parametroGlobalRepository, ParametroCatalogoService parametroCatalogoService) {
        this.parametroGlobalRepository = parametroGlobalRepository;
        this.parametroCatalogoService = parametroCatalogoService;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!sincronizacaoHabilitada()) {
            log.info("Sincronização do catálogo de parâmetros na inicialização desativada (PARAMETROS_SYNC_CATALOGO_NA_INICIALIZACAO=false).");
            return;
        }
        SincronizacaoCatalogoDTO resultado = parametroCatalogoService.sincronizar();
        log.info("Catálogo de parâmetros sincronizado: {} novos, {} marcados como órfãos.",
            resultado.inseridas(), resultado.orfas());
    }

    private boolean sincronizacaoHabilitada() {
        return parametroGlobalRepository
            .findByCodigo(ParametrosGlobaisCatalogo.PARAMETROS_SYNC_CATALOGO_NA_INICIALIZACAO.getCodigo())
            .map(p -> Boolean.parseBoolean(p.getValor()))
            .orElse(true);
    }
}
