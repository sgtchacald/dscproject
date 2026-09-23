package br.com.diegocordeiro.dscproject.service.dashboard;

import br.com.diegocordeiro.dscproject.dto.dashboards.DashboardFinanceiroDTO;
import br.com.diegocordeiro.dscproject.model.conta.Conta;
import br.com.diegocordeiro.dscproject.model.usuario.Usuario;
import br.com.diegocordeiro.dscproject.repository.cartao.CartaoCreditoRepository;
import br.com.diegocordeiro.dscproject.repository.conta.ContaRepository;
import br.com.diegocordeiro.dscproject.repository.despesa.DespesaRepository;
import br.com.diegocordeiro.dscproject.repository.despesa.DespesaUsuarioRepository;
import br.com.diegocordeiro.dscproject.repository.receita.ReceitaRepository;
import br.com.diegocordeiro.dscproject.repository.usuario.UsuarioRepository;
import br.com.diegocordeiro.dscproject.service.despesa.DespesaService;
import br.com.diegocordeiro.dscproject.service.receita.ReceitaService;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final ContaRepository contaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ReceitaService receitaService;
    private final ReceitaRepository receitaRepository;
    private final DespesaRepository despesaRepository;
    private final DespesaService despesaService;
    private final CartaoCreditoRepository cartaoCreditoRepository;

    public DashboardService(ContaRepository contaRepository, UsuarioRepository usuarioRepository, ReceitaService receitaService, DespesaService despesaService, CartaoCreditoRepository cartaoCreditoRepository, ReceitaRepository  receitaRepository, DespesaRepository despesaRepository) {
        this.contaRepository = contaRepository;
        this.usuarioRepository = usuarioRepository;
        this.receitaService = receitaService;
        this.despesaService = despesaService;
        this.cartaoCreditoRepository = cartaoCreditoRepository;
        this.receitaRepository = receitaRepository;
        this.despesaRepository = despesaRepository;
    }

    @Transactional(readOnly = true)
    public DashboardFinanceiroDTO carregarDashboardFinanceiro(YearMonth competencia,Integer anoInicioParam, Integer anoFimParam) {
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

        // Card 4: Despesas por Categoria
        List<DashboardFinanceiroDTO.CategoriaResumoDTO> despesasPorCategoria =
                despesaService.buscarDespesasPorCategoriaParaDashboard(competencia, usuarioId);
        dto.setDespesasPorCategoria(despesasPorCategoria);
        dto.setConicGradientDespesas(calcularConicGradient(despesasPorCategoria));

        // Card 5: Pagas vs Pendentes
        DashboardFinanceiroDTO.StatusPagamentoResumoDTO statusPagamento =
                despesaService.buscarResumoStatusPagamento(competencia, usuarioId);
        dto.setStatusPagamento(statusPagamento);

        // Card 6: Total Rateado por Pessoa
        List<DashboardFinanceiroDTO.RateioPorContatoDTO> rateioPorContato =
               despesaService .buscarResumoRateioPorContato(competencia, usuarioId);
        dto.setRateioPorContato(rateioPorContato);

        // Card 7: Limite Usado × Disponível por Cartão
        List<DashboardFinanceiroDTO.CartaoLimiteResumoDTO> cartoesLimite =
                buscarResumoLimiteCartoes(usuarioId);
        dto.setCartoesLimite(cartoesLimite);

        processarCard8Evolucao(dto, usuarioId, anoInicioParam, anoFimParam);

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
    private String calcularConicGradient(List<DashboardFinanceiroDTO.CategoriaResumoDTO> categorias) {
        if (categorias == null || categorias.isEmpty()) {
            return "conic-gradient(#69747f 0% 100%)";
        }

        // 1. soma o valor total de todas as categorias do mês
        BigDecimal somaTotal = categorias.stream()
                .map(DashboardFinanceiroDTO.CategoriaResumoDTO::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (somaTotal.compareTo(BigDecimal.ZERO) == 0) {
            return "conic-gradient(#69747f 0% 100%)";
        }

        // 2. constrói dinamicamente a string do conic-gradient com base nas percentagens
        StringBuilder sb = new StringBuilder("conic-gradient(");
        double acumuladoPercentual = 0.0;

        for (int i = 0; i < categorias.size(); i++) {
            DashboardFinanceiroDTO.CategoriaResumoDTO cat = categorias.get(i);

            // calcula a percentagem da categoria
            double percentual = cat.getTotal().doubleValue() / somaTotal.doubleValue() * 100.0;
            double proximoAcumulado = acumuladoPercentual + percentual;

            // Adiciona a cor e o intervalo de graus/percentagem no padrão CSS
            sb.append(cat.getCor())
                    .append(" ")
                    .append(String.format(java.util.Locale.US, "%.2f", acumuladoPercentual))
                    .append("% ")
                    .append(String.format(java.util.Locale.US, "%.2f", proximoAcumulado))
                    .append("%");

            if (i < categorias.size() - 1) {
                sb.append(", ");
            }
            acumuladoPercentual = proximoAcumulado;
        }
        sb.append(")");
        return sb.toString();
    }

    @Transactional(readOnly = true)
    public List<DashboardFinanceiroDTO.CartaoLimiteResumoDTO> buscarResumoLimiteCartoes(Long usuarioId) {
        // Executa a query C7 do documento de análise
        List<Object[]> resultados = cartaoCreditoRepository.buscarLimiteUsadoPorCartao(usuarioId);

        return resultados.stream().map(obj -> {
            Long cartaoId = ((Number) obj[0]).longValue();
            String descricao = (String) obj[1];
            BigDecimal limite = (BigDecimal) obj[2]; // Pode ser nulo (RN11)
            BigDecimal usado = (BigDecimal) obj[3];

            return new DashboardFinanceiroDTO.CartaoLimiteResumoDTO(cartaoId, descricao, limite, usado);
        }).toList();
    }
    @Transactional(readOnly = true)
    public void processarCard8Evolucao(DashboardFinanceiroDTO dto, Long usuarioId, Integer anoInicioParam, Integer anoFimParam) {
        List<Integer> anosDisponiveis = despesaRepository.buscarAnosDisponiveisParaEvolucao(usuarioId);
        dto.setAnosDisponiveis(anosDisponiveis);

        if (anosDisponiveis == null || anosDisponiveis.isEmpty()) {
            dto.setRotulosGrafico(List.of());
            dto.setReceitasGrafico(List.of());
            dto.setDespesasGrafico(List.of());
            return;
        }

        int anoCorrente = java.time.LocalDate.now().getYear();
        int menorAno = anosDisponiveis.get(0);
        int maiorAno = anosDisponiveis.get(anosDisponiveis.size() - 1);

        int anoFimDefault = anosDisponiveis.contains(anoCorrente) ? anoCorrente : maiorAno;
        int anoFim = (anoFimParam != null) ? anoFimParam : anoFimDefault;
        int anoInicio = (anoInicioParam != null) ? anoInicioParam : menorAno;

        if (anoInicio > anoFim) {
            int temp = anoInicio;
            anoInicio = anoFim;
            anoFim = temp;
        }

        dto.setAnoInicioSelecionado(anoInicio);
        dto.setAnoFimSelecionado(anoFim);

        if (anoInicio == anoFim) {
            dto.setModoMensal(true);
            processarEvolucaoMensal(dto, usuarioId, anoInicio);
        } else {
            dto.setModoMensal(false);
            processarEvolucaoAnual(dto, usuarioId, anoInicio, anoFim);
        }
    }

    private void processarEvolucaoMensal(DashboardFinanceiroDTO dto, Long usuarioId, int ano) {
        String anoStr = String.valueOf(ano);
        List<Object[]> receitasBrutas = receitaRepository.somarReceitasPorMesEAnual(usuarioId, anoStr);
        List<Object[]> despesasBrutas = despesaRepository.somarDespesasPorMesEAnual(usuarioId, anoStr);

        java.util.Map<String, BigDecimal> mapReceitas = converterParaMapa(receitasBrutas);
        java.util.Map<String, BigDecimal> mapDespesas = converterParaMapa(despesasBrutas);

        List<String> rotulos = List.of("Jan", "Fev", "Mar", "Abr", "Mai", "Jun", "Jul", "Ago", "Set", "Out", "Nov", "Dez");
        List<BigDecimal> receitas = new ArrayList<>();
        List<BigDecimal> despesas = new ArrayList<>();

        for (int i = 1; i <= 12; i++) {
            String mesKey = String.format("%02d", i);
            receitas.add(mapReceitas.getOrDefault(mesKey, BigDecimal.ZERO));
            despesas.add(mapDespesas.getOrDefault(mesKey, BigDecimal.ZERO));
        }

        dto.setRotulosGrafico(rotulos);
        dto.setReceitasGrafico(receitas);
        dto.setDespesasGrafico(despesas);
    }

    private void processarEvolucaoAnual(DashboardFinanceiroDTO dto, Long usuarioId, int anoInicio, int anoFim) {
        String inicioStr = String.valueOf(anoInicio);
        String fimStr = String.valueOf(anoFim);

        List<Object[]> receitasBrutas = receitaRepository.somarReceitasPorIntervaloAnos(usuarioId, inicioStr, fimStr);
        List<Object[]> despesasBrutas = despesaRepository.somarDespesasPorIntervaloAnos(usuarioId, inicioStr, fimStr);

        java.util.Map<String, BigDecimal> mapReceitas = converterParaMapa(receitasBrutas);
        java.util.Map<String, BigDecimal> mapDespesas = converterParaMapa(despesasBrutas);

        List<String> rotulos = new ArrayList<>();
        List<BigDecimal> receitas = new ArrayList<>();
        List<BigDecimal> despesas = new ArrayList<>();

        for (int ano = anoInicio; ano <= anoFim; ano++) {
            String anoKey = String.valueOf(ano);
            rotulos.add(anoKey);
            receitas.add(mapReceitas.getOrDefault(anoKey, BigDecimal.ZERO));
            despesas.add(mapDespesas.getOrDefault(anoKey, BigDecimal.ZERO));
        }

        dto.setRotulosGrafico(rotulos);
        dto.setReceitasGrafico(receitas);
        dto.setDespesasGrafico(despesas);
    }

    private java.util.Map<String, BigDecimal> converterParaMapa(List<Object[]> resultados) {
        java.util.Map<String, BigDecimal> mapa = new java.util.HashMap<>();
        if (resultados != null) {
            for (Object[] obj : resultados) {
                if (obj[0] != null) {
                    String chave = obj[0].toString();
                    BigDecimal valor = (BigDecimal) obj[1];
                    mapa.put(chave, valor != null ? valor : BigDecimal.ZERO);
                }
            }
        }
        return mapa;
    }
}