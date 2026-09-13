package br.com.diegocordeiro.dscproject.service.despesa;

import br.com.diegocordeiro.dscproject.dto.despesa.ContatoRapidoDTO;
import br.com.diegocordeiro.dscproject.dto.despesa.ContatoRateioDTO;
import br.com.diegocordeiro.dscproject.dto.despesa.DespesaEdicaoDTO;
import br.com.diegocordeiro.dscproject.dto.despesa.DespesaFormDTO;
import br.com.diegocordeiro.dscproject.dto.despesa.DespesaGridDTO;
import br.com.diegocordeiro.dscproject.dto.despesa.DespesaRateioDTO;
import br.com.diegocordeiro.dscproject.dto.despesa.UsuarioRateioDTO;
import br.com.diegocordeiro.dscproject.enums.AplicaA;
import br.com.diegocordeiro.dscproject.enums.MeioPagamento;
import br.com.diegocordeiro.dscproject.enums.OrigemLancamento;
import br.com.diegocordeiro.dscproject.enums.StatusContato;
import br.com.diegocordeiro.dscproject.enums.StatusPagamento;
import br.com.diegocordeiro.dscproject.enums.TipoConta;
import br.com.diegocordeiro.dscproject.enums.TipoContato;
import br.com.diegocordeiro.dscproject.model.cartao.CartaoCredito;
import br.com.diegocordeiro.dscproject.model.categoria.Categoria;
import br.com.diegocordeiro.dscproject.model.conta.Conta;
import br.com.diegocordeiro.dscproject.model.contato.Contato;
import br.com.diegocordeiro.dscproject.model.despesa.Despesa;
import br.com.diegocordeiro.dscproject.model.despesa.DespesaUsuario;
import br.com.diegocordeiro.dscproject.model.usuario.Usuario;
import br.com.diegocordeiro.dscproject.repository.cartao.CartaoCreditoRepository;
import br.com.diegocordeiro.dscproject.repository.categoria.CategoriaRepository;
import br.com.diegocordeiro.dscproject.repository.conta.ContaRepository;
import br.com.diegocordeiro.dscproject.repository.contato.ContatoRepository;
import br.com.diegocordeiro.dscproject.repository.despesa.DespesaRepository;
import br.com.diegocordeiro.dscproject.repository.despesa.DespesaUsuarioRepository;
import br.com.diegocordeiro.dscproject.repository.usuario.UsuarioRepository;
import br.com.diegocordeiro.dscproject.service.exceptions.RegistroNaoEncontradoException;
import br.com.diegocordeiro.dscproject.service.exceptions.RegraNegocioException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Service
public class DespesaService {

    private final DespesaRepository despesaRepository;
    private final DespesaUsuarioRepository despesaUsuarioRepository;
    private final ContaRepository contaRepository;
    private final CartaoCreditoRepository cartaoCreditoRepository;
    private final CategoriaRepository categoriaRepository;
    private final ContatoRepository contatoRepository;
    private final UsuarioRepository usuarioRepository;

    public DespesaService(
            DespesaRepository despesaRepository,
            DespesaUsuarioRepository despesaUsuarioRepository,
            ContaRepository contaRepository,
            CartaoCreditoRepository cartaoCreditoRepository,
            CategoriaRepository categoriaRepository,
            ContatoRepository contatoRepository,
            UsuarioRepository usuarioRepository) {
        this.despesaRepository = despesaRepository;
        this.despesaUsuarioRepository = despesaUsuarioRepository;
        this.contaRepository = contaRepository;
        this.cartaoCreditoRepository = cartaoCreditoRepository;
        this.categoriaRepository = categoriaRepository;
        this.contatoRepository = contatoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public List<DespesaGridDTO> listarParaGrid(Long usuarioId) {
        return despesaRepository.listarPorUsuario(usuarioId).stream()
                .map(DespesaGridDTO::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public DespesaEdicaoDTO buscarParaEdicao(Long id, Long usuarioId) {
        Despesa d = despesaRepository.buscarPorIdEUsuario(id, usuarioId)
                .orElseThrow(() -> new RegistroNaoEncontradoException("msg.despesa.nao-encontrada"));
        return new DespesaEdicaoDTO(d);
    }

    @Transactional(readOnly = true)
    public Despesa buscarPorIdEUsuario(Long id, Long usuarioId) {
        return despesaRepository.buscarPorIdEUsuario(id, usuarioId)
                .orElseThrow(() -> new RegistroNaoEncontradoException("msg.despesa.nao-encontrada"));
    }

    @Transactional
    public Despesa inserir(DespesaFormDTO dto, Long usuarioId, String loginAutor, boolean podeRatear) {
        Conta conta = null;
        CartaoCredito cartao = null;
        MeioPagamento meioPagamento;
        StatusPagamento statusPagamento;
        LocalDate dataPagamento = null;

        String forma = dto.getFormaPagamento() != null ? dto.getFormaPagamento().toUpperCase() : "";
        if ("CARTAO".equals(forma)) {
            cartao = cartaoCreditoRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(dto.getCartaoId(), usuarioId)
                    .orElse(null);
            meioPagamento = MeioPagamento.CREDITO;
            statusPagamento = StatusPagamento.NAO_SE_APLICA;
        } else if ("DINHEIRO".equals(forma)) {
            conta = contaRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(dto.getContaId(), usuarioId)
                    .orElse(null);
            meioPagamento = MeioPagamento.DINHEIRO;
            statusPagamento = dto.getStatusPagamento() != null ? dto.getStatusPagamento() : StatusPagamento.NAO;
            if (statusPagamento == StatusPagamento.SIM) {
                dataPagamento = dto.getDataPagamento();
            }
        } else {
            conta = contaRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(dto.getContaId(), usuarioId)
                    .orElse(null);
            meioPagamento = dto.getMeioPagamento() != null ? dto.getMeioPagamento() : MeioPagamento.DEBITO;
            statusPagamento = dto.getStatusPagamento() != null ? dto.getStatusPagamento() : StatusPagamento.NAO;
            if (statusPagamento == StatusPagamento.SIM) {
                dataPagamento = dto.getDataPagamento();
            }
        }

        Categoria categoria = null;
        if (dto.getCategoriaId() != null) {
            categoria = categoriaRepository.findByIdAndDataExclusaoIsNull(dto.getCategoriaId()).orElse(null);
        }

        YearMonth competencia = YearMonth.parse(dto.getCompetencia());

        if (dto.isParcelada()) {
            int n = dto.getQtdParcelas() != null ? dto.getQtdParcelas() : 2;
            BigDecimal total = dto.getValorTotalCompra() != null ? dto.getValorTotalCompra() : dto.getValor();
            BigDecimal parcelaBase = total.divide(BigDecimal.valueOf(n), 2, RoundingMode.DOWN);

            Despesa mae = new Despesa();
            mae.setNome(dto.getNome());
            mae.setDescricao(dto.getDescricao());
            mae.setDataLancamento(dto.getDataLancamento());
            mae.setDataVencimento(dto.getDataVencimento());
            mae.setCompetencia(competencia);
            mae.setValor(parcelaBase);
            mae.setValorTotalCompra(total);
            mae.setParcelada(true);
            mae.setNroParcela(1);
            mae.setQtdParcelas(n);
            mae.setParcelaPai(null);
            mae.setConta(conta);
            mae.setCartao(cartao);
            mae.setMeioPagamento(meioPagamento);
            mae.setStatusPagamento(statusPagamento);
            mae.setDataPagamento(dataPagamento);
            mae.setCategoria(categoria);
            mae.setOrigem(OrigemLancamento.MANUAL);
            mae.setRecorrente(false);
            mae.setRecorrentePai(null);
            mae.setCriadoPor(loginAutor);
            mae.setAlteradoPor(loginAutor);

            mae = despesaRepository.save(mae);
            salvarRateios(mae, dto.getRateio(), 1, n, podeRatear, loginAutor);

            for (int i = 2; i <= n; i++) {
                Despesa filha = new Despesa();
                filha.setNome(dto.getNome());
                filha.setDescricao(dto.getDescricao());
                filha.setDataLancamento(dto.getDataLancamento());
                if (dto.getDataVencimento() != null) {
                    filha.setDataVencimento(dto.getDataVencimento().plusMonths(i - 1));
                }
                filha.setCompetencia(competencia.plusMonths(i - 1));
                if (i < n) {
                    filha.setValor(parcelaBase);
                } else {
                    BigDecimal residuo = total.subtract(parcelaBase.multiply(BigDecimal.valueOf(n - 1)));
                    filha.setValor(residuo);
                }
                filha.setValorTotalCompra(total);
                filha.setParcelada(true);
                filha.setNroParcela(i);
                filha.setQtdParcelas(n);
                filha.setParcelaPai(mae);
                filha.setRecorrente(false);
                filha.setRecorrentePai(null);
                filha.setConta(conta);
                filha.setCartao(cartao);
                filha.setMeioPagamento(meioPagamento);
                filha.setStatusPagamento(statusPagamento);
                filha.setDataPagamento(dataPagamento);
                filha.setCategoria(categoria);
                filha.setOrigem(OrigemLancamento.MANUAL);
                filha.setCriadoPor(loginAutor);
                filha.setAlteradoPor(loginAutor);

                filha = despesaRepository.save(filha);
                salvarRateios(filha, dto.getRateio(), i, n, podeRatear, loginAutor);
            }

            return mae;
        } else if (dto.isRecorrente()) {
            int meses = dto.getQtdMesesRecorrencia() != null ? dto.getQtdMesesRecorrencia() : 12;

            Despesa mae = new Despesa();
            mae.setNome(dto.getNome());
            mae.setDescricao(dto.getDescricao());
            mae.setDataLancamento(dto.getDataLancamento());
            mae.setDataVencimento(dto.getDataVencimento());
            mae.setCompetencia(competencia);
            mae.setValor(dto.getValor());
            mae.setValorTotalCompra(null);
            mae.setParcelada(false);
            mae.setNroParcela(null);
            mae.setQtdParcelas(null);
            mae.setParcelaPai(null);
            mae.setRecorrente(true);
            mae.setRecorrentePai(null);
            mae.setConta(conta);
            mae.setCartao(cartao);
            mae.setMeioPagamento(meioPagamento);
            mae.setStatusPagamento(statusPagamento);
            mae.setDataPagamento(dataPagamento);
            mae.setCategoria(categoria);
            mae.setOrigem(OrigemLancamento.MANUAL);
            mae.setCriadoPor(loginAutor);
            mae.setAlteradoPor(loginAutor);

            mae = despesaRepository.save(mae);
            salvarRateios(mae, dto.getRateio(), 1, 1, podeRatear, loginAutor);

            StatusPagamento statusFilhas = "CARTAO".equals(forma) ? StatusPagamento.NAO_SE_APLICA : StatusPagamento.NAO;

            for (int i = 2; i <= meses; i++) {
                Despesa filha = new Despesa();
                filha.setNome(dto.getNome());
                filha.setDescricao(dto.getDescricao());
                filha.setDataLancamento(dto.getDataLancamento());
                if (dto.getDataVencimento() != null) {
                    filha.setDataVencimento(dto.getDataVencimento().plusMonths(i - 1));
                }
                filha.setCompetencia(competencia.plusMonths(i - 1));
                filha.setValor(dto.getValor());
                filha.setValorTotalCompra(null);
                filha.setParcelada(false);
                filha.setNroParcela(null);
                filha.setQtdParcelas(null);
                filha.setParcelaPai(null);
                filha.setRecorrente(true);
                filha.setRecorrentePai(mae);
                filha.setConta(conta);
                filha.setCartao(cartao);
                filha.setMeioPagamento(meioPagamento);
                filha.setStatusPagamento(statusFilhas);
                filha.setDataPagamento(null);
                filha.setCategoria(categoria);
                filha.setOrigem(OrigemLancamento.MANUAL);
                filha.setCriadoPor(loginAutor);
                filha.setAlteradoPor(loginAutor);

                filha = despesaRepository.save(filha);
                salvarRateios(filha, dto.getRateio(), 1, 1, podeRatear, loginAutor);
            }

            return mae;
        } else {
            Despesa d = new Despesa();
            d.setNome(dto.getNome());
            d.setDescricao(dto.getDescricao());
            d.setDataLancamento(dto.getDataLancamento());
            d.setDataVencimento(dto.getDataVencimento());
            d.setCompetencia(competencia);
            d.setValor(dto.getValor());
            d.setValorTotalCompra(null);
            d.setParcelada(false);
            d.setNroParcela(null);
            d.setQtdParcelas(null);
            d.setParcelaPai(null);
            d.setRecorrente(false);
            d.setRecorrentePai(null);
            d.setConta(conta);
            d.setCartao(cartao);
            d.setMeioPagamento(meioPagamento);
            d.setStatusPagamento(statusPagamento);
            d.setDataPagamento(dataPagamento);
            d.setCategoria(categoria);
            d.setOrigem(OrigemLancamento.MANUAL);
            d.setCriadoPor(loginAutor);
            d.setAlteradoPor(loginAutor);

            d = despesaRepository.save(d);
            salvarRateios(d, dto.getRateio(), 1, 1, podeRatear, loginAutor);
            return d;
        }
    }

    private void salvarRateios(
            Despesa d,
            List<DespesaRateioDTO> rateiosDTO,
            int parcelaAtual,
            int totalParcelas,
            boolean podeRatear,
            String loginAutor) {
        if (!podeRatear || rateiosDTO == null || rateiosDTO.isEmpty()) {
            return;
        }

        for (DespesaRateioDTO item : rateiosDTO) {
            Long cid = item.getContatoId();
            if (cid == null || item.getValor() == null) {
                continue;
            }

            Contato contato = contatoRepository.findById(cid).orElse(null);
            if (contato == null) {
                continue;
            }

            BigDecimal valorFatia;
            if (totalParcelas > 1) {
                BigDecimal fatiaBase = item.getValor().divide(BigDecimal.valueOf(totalParcelas), 2, RoundingMode.DOWN);
                if (parcelaAtual < totalParcelas) {
                    valorFatia = fatiaBase;
                } else {
                    valorFatia = item.getValor().subtract(fatiaBase.multiply(BigDecimal.valueOf(totalParcelas - 1)));
                }
            } else {
                valorFatia = item.getValor();
            }

            DespesaUsuario du = new DespesaUsuario();
            du.setDespesa(d);
            du.setContato(contato);
            du.setValor(valorFatia);
            du.setStatusPagamento(item.getStatusPagamento() != null ? item.getStatusPagamento() : StatusPagamento.NAO);
            du.setDataAcerto(item.getDataAcerto());
            du.setCriadoPor(loginAutor);
            du.setAlteradoPor(loginAutor);

            despesaUsuarioRepository.save(du);
            d.getRateios().add(du);
        }
    }

    @Transactional
    public Despesa editar(Long id, DespesaFormDTO dto, Long usuarioId, String loginAutor, boolean podeRatear) {
        Despesa d = despesaRepository.buscarPorIdEUsuario(id, usuarioId)
                .orElseThrow(() -> new RegistroNaoEncontradoException("msg.despesa.nao-encontrada"));

        d.setNome(dto.getNome());
        d.setDescricao(dto.getDescricao());
        d.setCompetencia(YearMonth.parse(dto.getCompetencia()));
        d.setValor(dto.getValor());
        d.setDataLancamento(dto.getDataLancamento());
        d.setDataVencimento(dto.getDataVencimento());

        if (dto.getCategoriaId() != null) {
            d.setCategoria(categoriaRepository.findByIdAndDataExclusaoIsNull(dto.getCategoriaId()).orElse(null));
        } else {
            d.setCategoria(null);
        }

        if (d.getOrigem() == OrigemLancamento.MANUAL) {
            String forma = dto.getFormaPagamento() != null ? dto.getFormaPagamento().toUpperCase() : "";
            if ("CARTAO".equals(forma)) {
                CartaoCredito cc = cartaoCreditoRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(dto.getCartaoId(), usuarioId).orElse(null);
                d.setCartao(cc);
                d.setConta(null);
                d.setMeioPagamento(MeioPagamento.CREDITO);
                d.setStatusPagamento(StatusPagamento.NAO_SE_APLICA);
                d.setDataPagamento(null);
            } else if ("DINHEIRO".equals(forma)) {
                Conta c = contaRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(dto.getContaId(), usuarioId).orElse(null);
                d.setConta(c);
                d.setCartao(null);
                d.setMeioPagamento(MeioPagamento.DINHEIRO);
                atualizarStatusPagamento(d, dto);
            } else {
                Conta c = contaRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(dto.getContaId(), usuarioId).orElse(null);
                d.setConta(c);
                d.setCartao(null);
                d.setMeioPagamento(dto.getMeioPagamento() != null ? dto.getMeioPagamento() : MeioPagamento.DEBITO);
                atualizarStatusPagamento(d, dto);
            }
        } else {
            if (d.getCartao() == null || d.getOrigem() == OrigemLancamento.IMPORTACAO) {
                atualizarStatusPagamento(d, dto);
            }
        }

        d.setAlteradoPor(loginAutor);

        if (podeRatear && dto.getRateio() != null) {
            sincronizarRateio(d, dto.getRateio(), loginAutor);
        }

        return despesaRepository.save(d);
    }

    private void atualizarStatusPagamento(Despesa d, DespesaFormDTO dto) {
        if (dto.getStatusPagamento() == StatusPagamento.SIM) {
            d.setStatusPagamento(StatusPagamento.SIM);
            d.setDataPagamento(dto.getDataPagamento());
        } else {
            d.setStatusPagamento(StatusPagamento.NAO);
            d.setDataPagamento(null);
        }
    }

    private void sincronizarRateio(Despesa d, List<DespesaRateioDTO> novosRateios, String loginAutor) {
        List<DespesaUsuario> existentes = new ArrayList<>(d.getRateios());
        for (DespesaUsuario du : existentes) {
            boolean manter = novosRateios.stream().anyMatch(nr -> nr.getContatoId() != null && du.getContato() != null && nr.getContatoId().equals(du.getContato().getId()));
            if (!manter) {
                du.setDataExclusao(Instant.now());
                du.setExcluidoPor(loginAutor);
                despesaUsuarioRepository.save(du);
            }
        }

        for (DespesaRateioDTO item : novosRateios) {
            Long cid = item.getContatoId();
            if (cid == null) {
                continue;
            }
            DespesaUsuario du = existentes.stream()
                    .filter(e -> e.getDataExclusao() == null && e.getContato() != null && cid.equals(e.getContato().getId()))
                    .findFirst()
                    .orElse(null);

            if (du != null) {
                du.setValor(item.getValor());
                if (item.getStatusPagamento() != null) {
                    du.setStatusPagamento(item.getStatusPagamento());
                }
                du.setDataAcerto(item.getDataAcerto());
                du.setAlteradoPor(loginAutor);
                despesaUsuarioRepository.save(du);
            } else {
                Contato c = contatoRepository.findById(cid).orElse(null);
                if (c != null) {
                    DespesaUsuario novo = new DespesaUsuario();
                    novo.setDespesa(d);
                    novo.setContato(c);
                    novo.setValor(item.getValor());
                    novo.setStatusPagamento(item.getStatusPagamento() != null ? item.getStatusPagamento() : StatusPagamento.NAO);
                    novo.setDataAcerto(item.getDataAcerto());
                    novo.setCriadoPor(loginAutor);
                    novo.setAlteradoPor(loginAutor);
                    despesaUsuarioRepository.save(novo);
                    d.getRateios().add(novo);
                }
            }
        }
    }

    @Transactional
    public void excluir(Long id, Long usuarioId, String loginAutor) {
        Despesa d = despesaRepository.buscarPorIdEUsuario(id, usuarioId)
                .orElseThrow(() -> new RegistroNaoEncontradoException("msg.despesa.nao-encontrada"));

        if (d.getOrigem() == OrigemLancamento.OPEN_FINANCE) {
            throw new RegraNegocioException("msg.despesa.importada.nao-excluivel");
        }

        Instant agora = Instant.now();

        if (d.isParcelada() && d.getParcelaPai() == null) {
            List<Despesa> serie = despesaRepository.buscarParcelasDaSerie(d.getId());
            for (Despesa p : serie) {
                p.setDataExclusao(agora);
                p.setExcluidoPor(loginAutor);
                for (DespesaUsuario du : p.getRateios()) {
                    du.setDataExclusao(agora);
                    du.setExcluidoPor(loginAutor);
                    despesaUsuarioRepository.save(du);
                }
                despesaRepository.save(p);
            }
        } else if (d.isRecorrente() && d.getRecorrentePai() == null) {
            List<Despesa> serie = despesaRepository.buscarOcorrenciasRecorrentes(d.getId());
            for (Despesa r : serie) {
                r.setDataExclusao(agora);
                r.setExcluidoPor(loginAutor);
                for (DespesaUsuario du : r.getRateios()) {
                    du.setDataExclusao(agora);
                    du.setExcluidoPor(loginAutor);
                    despesaUsuarioRepository.save(du);
                }
                despesaRepository.save(r);
            }
        } else {
            d.setDataExclusao(agora);
            d.setExcluidoPor(loginAutor);
            for (DespesaUsuario du : d.getRateios()) {
                du.setDataExclusao(agora);
                du.setExcluidoPor(loginAutor);
                despesaUsuarioRepository.save(du);
            }
            despesaRepository.save(d);
        }
    }

    @Transactional
    public void registrarPagamento(Long id, LocalDate dataPagamento, Long usuarioId, String loginAutor) {
        Despesa d = despesaRepository.buscarPorIdEUsuario(id, usuarioId)
                .orElseThrow(() -> new RegistroNaoEncontradoException("msg.despesa.nao-encontrada"));

        if (d.getStatusPagamento() == StatusPagamento.NAO_SE_APLICA && d.getOrigem() != OrigemLancamento.IMPORTACAO) {
            throw new RegraNegocioException("msg.despesa.pagamento.cartao-invalido");
        }

        d.setStatusPagamento(StatusPagamento.SIM);
        d.setDataPagamento(dataPagamento);
        d.setAlteradoPor(loginAutor);
        despesaRepository.save(d);
    }

    @Transactional
    public int registrarPagamentoLote(List<Long> ids, LocalDate dataPagamento, Long usuarioId, String loginAutor) {
        long totalEncontrado = despesaRepository.countPorIdsEUsuario(ids, usuarioId);
        if (totalEncontrado != ids.size()) {
            throw new RegistroNaoEncontradoException("msg.despesa.nao-encontrada");
        }

        List<Despesa> lista = despesaRepository.buscarPorIdsEUsuario(ids, usuarioId);
        int alteradas = 0;
        for (Despesa d : lista) {
            if (d.getStatusPagamento() != StatusPagamento.NAO_SE_APLICA || d.getOrigem() == OrigemLancamento.IMPORTACAO) {
                d.setStatusPagamento(StatusPagamento.SIM);
                d.setDataPagamento(dataPagamento);
                d.setAlteradoPor(loginAutor);
                despesaRepository.save(d);
                alteradas++;
            }
        }
        return alteradas;
    }

    @Transactional
    public List<Despesa> duplicar(List<Long> ids, String competenciaDestino, Long usuarioId, String loginAutor) {
        if (ids == null || ids.isEmpty()) {
            throw new RegraNegocioException("msg.despesa.duplicar.vazio");
        }
        long totalEncontrado = despesaRepository.countPorIdsEUsuario(ids, usuarioId);
        if (totalEncontrado != ids.size()) {
            throw new RegistroNaoEncontradoException("msg.despesa.nao-encontrada");
        }
        YearMonth comp = (competenciaDestino != null && !competenciaDestino.isBlank()) ? YearMonth.parse(competenciaDestino) : null;
        List<Despesa> originais = despesaRepository.buscarPorIdsEUsuario(ids, usuarioId);
        List<Despesa> duplicadas = new ArrayList<>();
        for (Despesa origem : originais) {
            Despesa copia = new Despesa();
            copia.setNome(origem.getNome());
            copia.setDescricao(origem.getDescricao());
            copia.setValor(origem.getValor());
            copia.setDataLancamento(origem.getDataLancamento());
            copia.setCompetencia(comp != null ? comp : origem.getCompetencia());
            if (origem.getDataVencimento() != null) {
                if (comp != null) {
                    int dia = Math.min(origem.getDataVencimento().getDayOfMonth(), comp.lengthOfMonth());
                    copia.setDataVencimento(comp.atDay(dia));
                } else {
                    copia.setDataVencimento(origem.getDataVencimento());
                }
            }
            copia.setConta(origem.getConta());
            copia.setCartao(origem.getCartao());
            copia.setMeioPagamento(origem.getMeioPagamento());
            copia.setCategoria(origem.getCategoria());
            copia.setOrigem(OrigemLancamento.MANUAL);
            copia.setStatusPagamento(origem.getCartao() != null ? StatusPagamento.NAO_SE_APLICA : StatusPagamento.NAO);
            copia.setDataPagamento(null);
            copia.setParcelada(false);
            copia.setNroParcela(null);
            copia.setQtdParcelas(null);
            copia.setParcelaPai(null);
            copia.setValorTotalCompra(null);
            copia.setRecorrente(false);
            copia.setRecorrentePai(null);
            copia.setCriadoPor(loginAutor);
            copia.setAlteradoPor(loginAutor);
            copia = despesaRepository.save(copia);
            if (origem.getRateios() != null) {
                for (DespesaUsuario du : origem.getRateios()) {
                    if (du.getDataExclusao() == null && du.getContato() != null) {
                        DespesaUsuario novoDu = new DespesaUsuario();
                        novoDu.setDespesa(copia);
                        novoDu.setContato(du.getContato());
                        novoDu.setValor(du.getValor());
                        novoDu.setStatusPagamento(StatusPagamento.NAO);
                        novoDu.setDataAcerto(null);
                        novoDu.setCriadoPor(loginAutor);
                        novoDu.setAlteradoPor(loginAutor);
                        despesaUsuarioRepository.save(novoDu);
                        copia.getRateios().add(novoDu);
                    }
                }
            }
            duplicadas.add(copia);
        }
        return duplicadas;
    }

    @Transactional
    public Despesa atualizarNome(Long id, String nome, Long usuarioId, String loginAutor) {
        if (nome == null || nome.isBlank()) {
            throw new RegraNegocioException("nome", "msg.despesa.nome-obrigatorio");
        }
        Despesa d = buscarPorIdEUsuario(id, usuarioId);
        d.setNome(nome.trim());
        d.setAlteradoPor(loginAutor);
        return despesaRepository.save(d);
    }

    @Transactional
    public Despesa atualizarValor(Long id, BigDecimal novoValor, Long usuarioId, String loginAutor) {
        if (novoValor == null || novoValor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RegraNegocioException("valor", "msg.despesa.valor-invalido");
        }
        Despesa d = buscarPorIdEUsuario(id, usuarioId);
        d.setValor(novoValor);
        d.setAlteradoPor(loginAutor);
        return despesaRepository.save(d);
    }

    @Transactional
    public Despesa atualizarCompetencia(Long id, String novaCompetenciaStr, Long usuarioId, String loginAutor) {
        if (novaCompetenciaStr == null || novaCompetenciaStr.isBlank()) {
            throw new RegraNegocioException("competencia", "msg.despesa.competencia-invalida");
        }
        YearMonth novaCompetencia;
        try {
            novaCompetencia = YearMonth.parse(novaCompetenciaStr.trim());
        } catch (DateTimeParseException e) {
            throw new RegraNegocioException("competencia", "msg.despesa.competencia-invalida");
        }
        Despesa d = buscarPorIdEUsuario(id, usuarioId);
        d.setCompetencia(novaCompetencia);
        d.setAlteradoPor(loginAutor);
        return despesaRepository.save(d);
    }

    @Transactional
    public Despesa atualizarCategoria(Long id, Long novaCategoriaId, Long usuarioId, String loginAutor) {
        Despesa d = buscarPorIdEUsuario(id, usuarioId);
        if (novaCategoriaId != null) {
            Categoria categoria = categoriaRepository.findByIdAndDataExclusaoIsNull(novaCategoriaId)
                    .orElseThrow(() -> new RegraNegocioException("categoriaId", "msg.despesa.categoria-invalida"));
            if (!categoria.isAtivo() || (categoria.getAplicaA() != AplicaA.DESPESA && categoria.getAplicaA() != AplicaA.AMBOS)) {
                throw new RegraNegocioException("categoriaId", "msg.despesa.categoria-invalida");
            }
            d.setCategoria(categoria);
        } else {
            d.setCategoria(null);
        }
        d.setAlteradoPor(loginAutor);
        return despesaRepository.save(d);
    }

    @Transactional
    public Despesa atualizarFormaPagamento(
            Long id,
            String formaPagamento,
            Long cartaoId,
            Long contaId,
            MeioPagamento meioPagamento,
            Long usuarioId,
            String loginAutor) {
        Despesa d = buscarPorIdEUsuario(id, usuarioId);

        if (d.getOrigem() == OrigemLancamento.OPEN_FINANCE) {
            throw new RegraNegocioException("msg.despesa.importada.nao-editavel");
        }

        if (formaPagamento == null || formaPagamento.isBlank()) {
            throw new RegraNegocioException("despesa.validacao.formaPagamento.obrigatoria");
        }

        String forma = formaPagamento.trim().toUpperCase();
        if ("CARTAO".equals(forma)) {
            if (cartaoId == null) {
                throw new RegraNegocioException("msg.despesa.forma-pagamento.invalida");
            }
            CartaoCredito cc = cartaoCreditoRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(cartaoId, usuarioId)
                    .orElseThrow(() -> new RegraNegocioException("msg.despesa.cartao.invalido"));
            if (!cc.isAtivo()) {
                throw new RegraNegocioException("msg.despesa.cartao.invalido");
            }
            d.setCartao(cc);
            d.setConta(null);
            d.setMeioPagamento(MeioPagamento.CREDITO);
            if (d.getOrigem() != OrigemLancamento.IMPORTACAO) {
                d.setStatusPagamento(StatusPagamento.NAO_SE_APLICA);
                d.setDataPagamento(null);
            } else if (d.getStatusPagamento() == StatusPagamento.NAO_SE_APLICA) {
                d.setStatusPagamento(StatusPagamento.NAO);
            }
        } else if ("DINHEIRO".equals(forma)) {
            Conta c = null;
            if (contaId != null) {
                c = contaRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(contaId, usuarioId)
                        .orElseThrow(() -> new RegraNegocioException("msg.despesa.conta.invalida"));
            } else {
                List<Conta> ativas = contaRepository.listarAtivasPorUsuario(usuarioId);
                c = ativas.stream()
                        .filter(a -> a.getTipo() == TipoConta.CARTEIRA)
                        .findFirst()
                        .orElse(null);
                if (c == null && !ativas.isEmpty()) {
                    c = ativas.get(0);
                }
            }
            if (c == null || !c.isAtivo()) {
                throw new RegraNegocioException("msg.despesa.conta.invalida");
            }
            d.setConta(c);
            d.setCartao(null);
            d.setMeioPagamento(MeioPagamento.DINHEIRO);
            if (d.getStatusPagamento() == StatusPagamento.NAO_SE_APLICA) {
                d.setStatusPagamento(StatusPagamento.NAO);
            }
        } else if ("CONTA".equals(forma)) {
            if (contaId == null) {
                throw new RegraNegocioException("msg.despesa.forma-pagamento.invalida");
            }
            Conta c = contaRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(contaId, usuarioId)
                    .orElseThrow(() -> new RegraNegocioException("msg.despesa.conta.invalida"));
            if (!c.isAtivo()) {
                throw new RegraNegocioException("msg.despesa.conta.invalida");
            }
            d.setConta(c);
            d.setCartao(null);
            d.setMeioPagamento(meioPagamento != null ? meioPagamento : MeioPagamento.DEBITO);
            if (d.getStatusPagamento() == StatusPagamento.NAO_SE_APLICA) {
                d.setStatusPagamento(StatusPagamento.NAO);
            }
        } else {
            throw new RegraNegocioException("msg.despesa.forma-pagamento.invalida");
        }

        d.setAlteradoPor(loginAutor);
        return despesaRepository.save(d);
    }

    @Transactional
    public void registrarAcertoRateio(
            Long despId,
            Long contatoId,
            boolean acertado,
            LocalDate dataAcerto,
            Long usuarioId,
            String loginAutor) {
        despesaRepository.buscarPorIdEUsuario(despId, usuarioId)
                .orElseThrow(() -> new RegistroNaoEncontradoException("msg.despesa.nao-encontrada"));

        DespesaUsuario du = despesaUsuarioRepository.findByDespesaIdAndContatoIdAndDataExclusaoIsNull(despId, contatoId)
                .orElseThrow(() -> new RegistroNaoEncontradoException("msg.despesa.rateio.usuario-invalido"));

        du.setStatusPagamento(acertado ? StatusPagamento.SIM : StatusPagamento.NAO);
        du.setDataAcerto(acertado ? dataAcerto : null);
        du.setAlteradoPor(loginAutor);
        despesaUsuarioRepository.save(du);
    }

    @Transactional(readOnly = true)
    public List<ContatoRateioDTO> buscarContatosParaRateio(String termo, Long usuarioIdLogado) {
        if (termo == null || termo.trim().length() < 3) {
            return List.of();
        }
        return contatoRepository.buscarAtivosPorDonoETermo(usuarioIdLogado, termo.trim()).stream()
                .limit(20)
                .map(ContatoRateioDTO::new)
                .toList();
    }

    @Transactional
    public ContatoRateioDTO cadastrarContatoRapido(ContatoRapidoDTO dto, Long usuarioIdLogado, String loginAutor) {
        Usuario dono = usuarioRepository.findById(usuarioIdLogado)
                .orElseThrow(() -> new RegistroNaoEncontradoException("usuario.nao.encontrado"));

        Contato contato = new Contato();
        contato.setUsuarioDono(dono);
        contato.setTipo(TipoContato.EXTERNO);
        contato.setNome(dto.getNome().trim());
        contato.setEmail(dto.getEmail() != null && !dto.getEmail().isBlank() ? dto.getEmail().trim() : null);
        contato.setTelefone(dto.getTelefone() != null && !dto.getTelefone().isBlank() ? dto.getTelefone().trim() : null);
        contato.setChavePix(dto.getChavePix() != null && !dto.getChavePix().isBlank() ? dto.getChavePix().trim() : null);
        contato.setStatus(StatusContato.ATIVO);
        contato.setCriadoPor(loginAutor);
        contato.setAlteradoPor(loginAutor);

        Contato salvo = contatoRepository.save(contato);
        return new ContatoRateioDTO(salvo);
    }

    @Transactional(readOnly = true)
    public List<UsuarioRateioDTO> buscarUsuariosParaRateio(String termo, Long usuarioIdLogado) {
        if (termo == null || termo.trim().length() < 3) {
            return List.of();
        }
        return usuarioRepository.buscarAtivosParaRateio(termo.trim(), usuarioIdLogado).stream()
                .limit(20)
                .map(UsuarioRateioDTO::new)
                .toList();
    }
}
