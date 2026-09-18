package br.com.diegocordeiro.dscproject.service.dashboard;

import br.com.diegocordeiro.dscproject.dto.dashboards.DashboardFinanceiroDTO;
import br.com.diegocordeiro.dscproject.model.conta.Conta;
import br.com.diegocordeiro.dscproject.model.usuario.Usuario;
import br.com.diegocordeiro.dscproject.repository.conta.ContaRepository;
import br.com.diegocordeiro.dscproject.repository.usuario.UsuarioRepository;
import br.com.diegocordeiro.dscproject.service.despesa.DespesaService;
import br.com.diegocordeiro.dscproject.service.receita.ReceitaService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final ContaRepository contaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ReceitaService receitaService;
    private final DespesaService despesaService;

    public DashboardService(ContaRepository contaRepository, UsuarioRepository usuarioRepository, ReceitaService receitaService, DespesaService despesaService) {
        this.contaRepository = contaRepository;
        this.usuarioRepository = usuarioRepository;
        this.receitaService = receitaService;
        this.despesaService = despesaService;
    }

    @Transactional(readOnly = true)
    public DashboardFinanceiroDTO carregarDashboardFinanceiro(YearMonth competencia) {
        Long usuarioId = obterUsuarioIdAutenticado();

        DashboardFinanceiroDTO dto = new DashboardFinanceiroDTO();
        dto.setCompetenciaSelecionada(competencia.toString());

        // Card 1: Saldo Consolidado (C1)
        BigDecimal saldoConsolidado = contaRepository.calcularSaldoConsolidado(usuarioId);
        dto.setSaldoConsolidado(saldoConsolidado != null ? saldoConsolidado : BigDecimal.ZERO);

        // Card 2: Saldo por Conta Bancária (C2)
        List<Conta> contasAtivas = contaRepository.listarContasParaDashboard(usuarioId);
        List<DashboardFinanceiroDTO.ContaResumoDTO> contasResumo = contasAtivas.stream()
                .map(conta -> new DashboardFinanceiroDTO.ContaResumoDTO(
                        conta.getId(),
                        conta.getDescricao(),
                        conta.getTipo() != null ? conta.getTipo().getDescricao() : "",
                        conta.getSaldo(),
                        conta.isConsideraSaldo()
                ))
                .collect(Collectors.toList());

        dto.setContas(contasResumo);


        //card 3 pt1
        BigDecimal totalReceitas = receitaService.somarPorCompetencia(competencia, usuarioId);
        dto.setTotalReceitas(totalReceitas != null ? totalReceitas : BigDecimal.ZERO);

        //card3 pt2
        BigDecimal totalDespesas = despesaService.somarCotaLiquidaPorCompetencia(competencia, usuarioId);
        dto.setTotalDespesas(totalDespesas != null ? totalDespesas : BigDecimal.ZERO);

        return dto;
    }

    private Long obterUsuarioIdAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            String login = ((UserDetails) authentication.getPrincipal()).getUsername();

            // busca o usuario no banco pelo login usando o metodo dentro do UsuarioRepository
            Usuario usuario = usuarioRepository.findByLogin(login)
                    .orElseThrow(() -> new IllegalStateException("Usuário não encontrado: " + login));

            return usuario.getId();
        }
        throw new IllegalStateException("Usuário autenticado não encontrado no contexto de segurança.");
    }


}