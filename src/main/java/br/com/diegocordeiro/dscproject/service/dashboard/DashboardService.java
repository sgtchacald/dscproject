package br.com.diegocordeiro.dscproject.service.dashboard;

import br.com.diegocordeiro.dscproject.dto.dashboards.cards.*;
import br.com.diegocordeiro.dscproject.model.conta.Conta;
import br.com.diegocordeiro.dscproject.model.usuario.Usuario;
import br.com.diegocordeiro.dscproject.repository.cartao.CartaoCreditoRepository;
import br.com.diegocordeiro.dscproject.repository.conta.ContaRepository;
import br.com.diegocordeiro.dscproject.repository.despesa.DespesaRepository;
import br.com.diegocordeiro.dscproject.repository.receita.ReceitaRepository;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true) // Aplica transação de leitura otimizada para todos os métodos públicos
public class DashboardService {

    private final ContaRepository contaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ReceitaService receitaService;
    private final ReceitaRepository receitaRepository;
    private final DespesaRepository despesaRepository;
    private final DespesaService despesaService;
    private final CartaoCreditoRepository cartaoCreditoRepository;

    public DashboardService(ContaRepository contaRepository, UsuarioRepository usuarioRepository,
                            ReceitaService receitaService, DespesaService despesaService,
                            CartaoCreditoRepository cartaoCreditoRepository,
                            ReceitaRepository receitaRepository, DespesaRepository despesaRepository) {
        this.contaRepository = contaRepository;
        this.usuarioRepository = usuarioRepository;
        this.receitaService = receitaService;
        this.despesaService = despesaService;
        this.cartaoCreditoRepository = cartaoCreditoRepository;
        this.receitaRepository = receitaRepository;
        this.despesaRepository = despesaRepository;
    }

    // MÉTODOS INDEPENDENTES POR CARTÃO (Isolamento para Carregamento Assíncrono via AJAX)

    public Card1SaldoConsolidadoDTO carregarSaldoConsolidado() {
        Long usuarioId = obterUsuarioIdAutenticado();
        BigDecimal saldoConsolidado = contaRepository.calcularSaldoConsolidado(usuarioId);
        return new Card1SaldoConsolidadoDTO(saldoConsolidado);
    }

    public Card2SaldoContaDTO carregarSaldoConta() {
        Long usuarioId = obterUsuarioIdAutenticado();
        List<Conta> contasAtivas = contaRepository.listarContasParaDashboard(usuarioId);

        List<Card2SaldoContaDTO.ContaResumoDTO> contasResumo = contasAtivas.stream()
                .map(conta -> new Card2SaldoContaDTO.ContaResumoDTO(
                        conta.getId(),
                        conta.getDescricao(),
                        conta.getTipo() != null ? conta.getTipo().getDescricao() : "",
                        conta.getSaldo(),
                        conta.isConsideraSaldo()
                )).collect(Collectors.toList());

        return new Card2SaldoContaDTO(contasResumo);
    }

    public Card3ReceitasDespesasDTO carregarReceitasDespesas(YearMonth competencia) {
        Long usuarioId = obterUsuarioIdAutenticado();
        BigDecimal totalReceitas = receitaService.somarPorCompetencia(competencia, usuarioId);
        BigDecimal totalDespesas = despesaService.somarCotaLiquidaPorCompetencia(competencia, usuarioId);
        return new Card3ReceitasDespesasDTO(totalReceitas, totalDespesas);
    }

    public Card4DespesasCategoriaDTO carregarDespesasPorCategoria(YearMonth competencia) {
        Long usuarioId = obterUsuarioIdAutenticado();

        // Supondo que você adaptou o método do DespesaService para retornar a nova classe CategoriaResumoDTO
        List<Card4DespesasCategoriaDTO.CategoriaResumoDTO> despesasPorCategoria =
                despesaService.buscarDespesasPorCategoriaParaDashboard(competencia, usuarioId);

        String conicGradient = calcularConicGradient(despesasPorCategoria);
        return new Card4DespesasCategoriaDTO(despesasPorCategoria, conicGradient);
    }

    public Card5StatusPagamentoDTO carregarStatusPagamento(YearMonth competencia) {
        Long usuarioId = obterUsuarioIdAutenticado();
        return despesaService.buscarResumoStatusPagamento(competencia, usuarioId);
    }

    public Card6RateioContatoDTO carregarRateioPorContato(YearMonth competencia) {
        Long usuarioId = obterUsuarioIdAutenticado();
        List<Card6RateioContatoDTO.RateioPorContatoDTO> rateioPorContato =
                despesaService.buscarResumoRateioPorContato(competencia, usuarioId);
        return new Card6RateioContatoDTO(rateioPorContato);
    }

    public Card7LimiteCartaoDTO carregarLimiteCartoes() {
        Long usuarioId = obterUsuarioIdAutenticado();
        List<Object[]> resultados = cartaoCreditoRepository.buscarLimiteUsadoPorCartao(usuarioId);

        List<Card7LimiteCartaoDTO.CartaoLimiteResumoDTO> cartoes = resultados.stream().map(obj -> {
            Long cartaoId = ((Number) obj[0]).longValue();
            String descricao = (String) obj[1];
            BigDecimal limite = (BigDecimal) obj[2];
            BigDecimal usado = (BigDecimal) obj[3];
            return new Card7LimiteCartaoDTO.CartaoLimiteResumoDTO(cartaoId, descricao, limite, usado);
        }).toList();

        return new Card7LimiteCartaoDTO(cartoes);
    }

    public Card8EvolucaoDTO carregarEvolucao(YearMonth competenciaBase, Integer anoInicioParam, Integer anoFimParam) {
        Long usuarioId = obterUsuarioIdAutenticado();
        Card8EvolucaoDTO dto = new Card8EvolucaoDTO();
        dto.setCompetenciaSelecionada(competenciaBase.toString());

        List<Integer> anosDisponiveis = despesaRepository.buscarAnosDisponiveisParaEvolucao(usuarioId);
        dto.setAnosDisponiveis(anosDisponiveis);

        if (anosDisponiveis == null || anosDisponiveis.isEmpty()) {
            return dto; // Retorna DTO vazio se não houver anos disponíveis
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

        return dto;
    }

    // METODOS AUXILIARES PRIVADOS

    private Long obterUsuarioIdAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            String login = ((UserDetails) authentication.getPrincipal()).getUsername();
            Usuario usuario = usuarioRepository.findByLogin(login)
                    .orElseThrow(() -> new IllegalStateException("Usuário não encontrado: " + login));
            return usuario.getId();
        }
        throw new IllegalStateException("Usuário autenticado não encontrado no contexto de segurança.");
    }

    private String calcularConicGradient(List<Card4DespesasCategoriaDTO.CategoriaResumoDTO> categorias) {
        if (categorias == null || categorias.isEmpty()) {
            return "conic-gradient(#69747f 0% 100%)";
        }

        BigDecimal somaTotal = categorias.stream()
                .map(Card4DespesasCategoriaDTO.CategoriaResumoDTO::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (somaTotal.compareTo(BigDecimal.ZERO) == 0) {
            return "conic-gradient(#69747f 0% 100%)";
        }

        StringBuilder sb = new StringBuilder("conic-gradient(");
        double acumuladoPercentual = 0.0;

        for (int i = 0; i < categorias.size(); i++) {
            Card4DespesasCategoriaDTO.CategoriaResumoDTO cat = categorias.get(i);
            double percentual = cat.getTotal().doubleValue() / somaTotal.doubleValue() * 100.0;
            double proximoAcumulado = acumuladoPercentual + percentual;

            sb.append(cat.getCor())
                    .append(" ")
                    .append(String.format(java.util.Locale.US, "%.2f", acumuladoPercentual)).append("% ")
                    .append(String.format(java.util.Locale.US, "%.2f", proximoAcumulado)).append("%");

            if (i < categorias.size() - 1) {
                sb.append(", ");
            }
            acumuladoPercentual = proximoAcumulado;
        }
        sb.append(")");
        return sb.toString();
    }

    private void processarEvolucaoMensal(Card8EvolucaoDTO dto, Long usuarioId, int ano) {
        String anoStr = String.valueOf(ano);
        Map<String, BigDecimal> mapReceitas = converterParaMapa(receitaRepository.somarReceitasPorMesEAnual(usuarioId, anoStr));
        Map<String, BigDecimal> mapDespesas = converterParaMapa(despesaRepository.somarDespesasPorMesEAnual(usuarioId, anoStr));

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

    private void processarEvolucaoAnual(Card8EvolucaoDTO dto, Long usuarioId, int anoInicio, int anoFim) {
        String inicioStr = String.valueOf(anoInicio);
        String fimStr = String.valueOf(anoFim);

        Map<String, BigDecimal> mapReceitas = converterParaMapa(receitaRepository.somarReceitasPorIntervaloAnos(usuarioId, inicioStr, fimStr));
        Map<String, BigDecimal> mapDespesas = converterParaMapa(despesaRepository.somarDespesasPorIntervaloAnos(usuarioId, inicioStr, fimStr));

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

    private Map<String, BigDecimal> converterParaMapa(List<Object[]> resultados) {
        Map<String, BigDecimal> mapa = new java.util.HashMap<>();
        if (resultados != null) {
            for (Object[] obj : resultados) {
                if (obj[0] != null) {
                    mapa.put(obj[0].toString(), (BigDecimal) obj[1]);
                }
            }
        }
        return mapa;
    }
}