package br.com.diegocordeiro.dscproject.web.sistema.controller.dashboards;

import br.com.diegocordeiro.dscproject.service.dashboard.DashboardService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.YearMonth;

@Controller
@RequestMapping("/dashboards")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }
    @GetMapping
    public String carregarDashboard(
            @RequestParam(value = "competencia", required = false) String competenciaStr,
            HttpServletRequest request,
            Model model) {

        YearMonth competencia = resolverCompetencia(competenciaStr);

        model.addAttribute("competencia", competencia.toString());
        model.addAttribute("uriAtual", request.getRequestURI());

        return "sistema/modulos/dashboards/dashboards";
    }

    @GetMapping("/fragmentos/saldo-consolidado")
    public String fragmentoSaldoConsolidado(Model model) {
        model.addAttribute("dados", dashboardService.carregarSaldoConsolidado());
        return "sistema/modulos/dashboards/fragments/card1-saldo-consolidado :: card";
    }
    @GetMapping("/fragmentos/saldo-conta")
    public String fragmentoSaldoConta(Model model) {
        model.addAttribute("dados", dashboardService.carregarSaldoConta());
        return "sistema/modulos/dashboards/fragments/card2-saldo-conta :: card";
    }

    @GetMapping("/fragmentos/receitas-despesas")
    public String fragmentoReceitasDespesas(@RequestParam String competencia, Model model) {
        model.addAttribute("dados", dashboardService.carregarReceitasDespesas(YearMonth.parse(competencia)));
        return "sistema/modulos/dashboards/fragments/card3-receitas-despesas :: card";
    }

    @GetMapping("/fragmentos/despesas-categoria")
    public String fragmentoDespesasCategoria(@RequestParam String competencia, Model model) {
        model.addAttribute("dados", dashboardService.carregarDespesasPorCategoria(YearMonth.parse(competencia)));
        return "sistema/modulos/dashboards/fragments/card4-despesas-categoria :: card";
    }

    @GetMapping("/fragmentos/status-pagamento")
    public String fragmentoStatusPagamento(@RequestParam String competencia, Model model) {
        model.addAttribute("dados", dashboardService.carregarStatusPagamento(YearMonth.parse(competencia)));
        return "sistema/modulos/dashboards/fragments/card5-pagas-pendentes :: card";
    }

    @GetMapping("/fragmentos/rateio-contato")
    public String fragmentoRateioContato(@RequestParam String competencia, Model model) {
        model.addAttribute("dados", dashboardService.carregarRateioPorContato(YearMonth.parse(competencia)));
        return "sistema/modulos/dashboards/fragments/card6-total-rateado :: card";
    }

    @GetMapping("/fragmentos/limite-cartao")
    public String fragmentoLimiteCartao(Model model) {
        model.addAttribute("dados", dashboardService.carregarLimiteCartoes());
        return "sistema/modulos/dashboards/fragments/card7-limite-cartao :: card";
    }

    @GetMapping("/fragmentos/evolucao")
    public String fragmentoEvolucao(
            @RequestParam String competencia,
            @RequestParam(value = "anoInicio", required = false) Integer anoInicio,
            @RequestParam(value = "anoFim", required = false) Integer anoFim,
            Model model) {

        model.addAttribute("dados", dashboardService.carregarEvolucao(YearMonth.parse(competencia), anoInicio, anoFim));
        return "sistema/modulos/dashboards/fragments/card8-evolucao :: card";
    }

    // METODOS AUXILIARES
    private YearMonth resolverCompetencia(String competenciaStr) {
        return (competenciaStr != null && !competenciaStr.isBlank())
                ? YearMonth.parse(competenciaStr)
                : YearMonth.now().minusMonths(1);
    }
}

