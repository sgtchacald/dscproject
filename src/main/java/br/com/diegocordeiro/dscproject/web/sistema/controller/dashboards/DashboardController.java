package br.com.diegocordeiro.dscproject.web.sistema.controller.dashboards;

import br.com.diegocordeiro.dscproject.dto.dashboards.DashboardFinanceiroDTO;
import br.com.diegocordeiro.dscproject.service.dashboard.DashboardService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.YearMonth;

@Controller
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/dashboards")
    public String carregarDashboard(
            @RequestParam(value = "competencia", required = false) String competenciaStr,
            HttpServletRequest request,
            Model model) {

        // Regra RN02 / Requisito RF11: Padrão inicial mes_atual - 1 se não vier informado
        YearMonth competencia = (competenciaStr != null && !competenciaStr.isBlank())
                ? YearMonth.parse(competenciaStr)
                : YearMonth.now().minusMonths(1);

        DashboardFinanceiroDTO dashboard = dashboardService.carregarDashboardFinanceiro(competencia);

        model.addAttribute("dashboard", dashboard);
        model.addAttribute("competencia", competencia.toString());
        model.addAttribute("uriAtual", request.getRequestURI());


        return "sistema/modulos/dashboards/dashboards";
    }
}