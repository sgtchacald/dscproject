package br.com.diegocordeiro.dscproject.service.transacao;

import br.com.diegocordeiro.dscproject.dto.transacao.TransacaoBancariaEdicaoDTO;
import br.com.diegocordeiro.dscproject.dto.transacao.TransacaoBancariaFiltroDTO;
import br.com.diegocordeiro.dscproject.dto.transacao.TransacaoBancariaFormDTO;
import br.com.diegocordeiro.dscproject.dto.transacao.TransacaoBancariaGridDTO;
import br.com.diegocordeiro.dscproject.dto.transacao.TransacaoBancariaTotaisDTO;
import br.com.diegocordeiro.dscproject.enums.NaturezaMovimento;
import br.com.diegocordeiro.dscproject.enums.OrigemLancamento;
import br.com.diegocordeiro.dscproject.model.categoria.Categoria;
import br.com.diegocordeiro.dscproject.model.conta.Conta;
import br.com.diegocordeiro.dscproject.model.transacao.TransacaoBancaria;
import br.com.diegocordeiro.dscproject.repository.categoria.CategoriaRepository;
import br.com.diegocordeiro.dscproject.repository.conta.ContaRepository;
import br.com.diegocordeiro.dscproject.repository.transacao.TransacaoBancariaRepository;
import br.com.diegocordeiro.dscproject.service.exceptions.RegistroNaoEncontradoException;
import br.com.diegocordeiro.dscproject.service.exceptions.RegraNegocioException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
public class TransacaoBancariaService {

    private final TransacaoBancariaRepository transacaoBancariaRepository;
    private final ContaRepository contaRepository;
    private final CategoriaRepository categoriaRepository;

    public TransacaoBancariaService(TransacaoBancariaRepository transacaoBancariaRepository, ContaRepository contaRepository, CategoriaRepository categoriaRepository) {
        this.transacaoBancariaRepository = transacaoBancariaRepository;
        this.contaRepository = contaRepository;
        this.categoriaRepository = categoriaRepository;
    }

    @Transactional(readOnly = true)
    public List<TransacaoBancariaGridDTO> listar(TransacaoBancariaFiltroDTO filtro, Long usuarioId) {
        String busca = (filtro.getBusca() != null && !filtro.getBusca().isBlank()) ? filtro.getBusca().trim() : null;
        YearMonth compInicio = filtro.getCompetenciaInicioYearMonth();
        YearMonth compFim = filtro.getCompetenciaFimYearMonth();

        List<TransacaoBancaria> transacoes = transacaoBancariaRepository.listarComFiltros(
            usuarioId,
            busca,
            compInicio,
            compFim,
            filtro.getContaId(),
            filtro.getNatureza(),
            filtro.getCategoriaId()
        );

        return transacoes.stream()
            .map(this::converterParaGridDTO)
            .toList();
    }

    @Transactional(readOnly = true)
    public TransacaoBancariaTotaisDTO calcularTotais(TransacaoBancariaFiltroDTO filtro, Long usuarioId) {
        if (!filtro.isCompetenciaUnica()) {
            return TransacaoBancariaTotaisDTO.vazio();
        }
        YearMonth comp = filtro.getCompetenciaInicioYearMonth();
        BigDecimal creditos = transacaoBancariaRepository.somarPorCompetenciaENaturezaEUsuario(comp, NaturezaMovimento.CREDITO, usuarioId);
        BigDecimal debitos = transacaoBancariaRepository.somarPorCompetenciaENaturezaEUsuario(comp, NaturezaMovimento.DEBITO, usuarioId);
        BigDecimal liquido = creditos.subtract(debitos);

        return TransacaoBancariaTotaisDTO.builder()
            .totalCreditos(creditos)
            .totalDebitos(debitos)
            .saldoLiquido(liquido)
            .exibir(true)
            .build();
    }

    @Transactional(readOnly = true)
    public TransacaoBancaria buscarPorIdEUsuario(Long id, Long usuarioId) {
        return transacaoBancariaRepository.findByIdAndContaUsuarioIdAndDataExclusaoIsNull(id, usuarioId)
            .orElseThrow(() -> new RegistroNaoEncontradoException("msg.transacao.nao-encontrada"));
    }

    @Transactional(readOnly = true)
    public TransacaoBancariaEdicaoDTO buscarParaEdicao(Long id, Long usuarioId) {
        TransacaoBancaria t = buscarPorIdEUsuario(id, usuarioId);
        return TransacaoBancariaEdicaoDTO.builder()
            .id(t.getId())
            .descricao(t.getDescricao())
            .valor(t.getValor())
            .natureza(t.getNatureza())
            .dataLancamento(t.getDataLancamento())
            .competencia(t.getCompetencia())
            .contaId(t.getConta() != null ? t.getConta().getId() : null)
            .categoriaId(t.getCategoria() != null ? t.getCategoria().getId() : null)
            .pagamentoFatura(t.isPagamentoFatura())
            .origem(t.getOrigem())
            .build();
    }

    @Transactional
    public TransacaoBancaria inserir(TransacaoBancariaFormDTO dto, Long usuarioId, String usuarioAuditoria) {
        Conta conta = validarConta(dto.getContaId(), usuarioId);
        Categoria categoria = validarCategoriaSeInformada(dto.getCategoriaId());

        TransacaoBancaria transacao = new TransacaoBancaria();
        transacao.setDescricao(normalizarTexto(dto.getDescricao()));
        transacao.setValor(dto.getValor());
        transacao.setNatureza(dto.getNatureza());
        transacao.setDataLancamento(dto.getDataLancamento());
        transacao.setCompetencia(YearMonth.parse(dto.getCompetencia()));
        transacao.setConta(conta);
        transacao.setCategoria(categoria);
        transacao.setOrigem(OrigemLancamento.MANUAL);
        transacao.setPagamentoFatura(false);

        return transacaoBancariaRepository.save(transacao);
    }

    @Transactional
    public TransacaoBancaria editar(Long id, TransacaoBancariaFormDTO dto, Long usuarioId, String usuarioAuditoria) {
        TransacaoBancaria transacao = buscarPorIdEUsuario(id, usuarioId);

        if (transacao.isPagamentoFatura()) {
            if (dto.getValor() != null && transacao.getValor().compareTo(dto.getValor()) != 0) {
                throw new RegraNegocioException("valor", "msg.transacao.pagamento-fatura.imutavel");
            }
            if (dto.getContaId() != null && !transacao.getConta().getId().equals(dto.getContaId())) {
                throw new RegraNegocioException("contaId", "msg.transacao.pagamento-fatura.imutavel");
            }
        } else if (transacao.getOrigem() == OrigemLancamento.OPEN_FINANCE) {
            if (dto.getContaId() != null && !transacao.getConta().getId().equals(dto.getContaId())) {
                throw new RegraNegocioException("contaId", "msg.transacao.open-finance.imutavel");
            }
        } else if (transacao.getOrigem() == OrigemLancamento.MANUAL) {
            transacao.setConta(validarConta(dto.getContaId(), usuarioId));
            transacao.setValor(dto.getValor());
            transacao.setNatureza(dto.getNatureza());
        }

        transacao.setDescricao(normalizarTexto(dto.getDescricao()));
        transacao.setDataLancamento(dto.getDataLancamento());
        transacao.setCompetencia(YearMonth.parse(dto.getCompetencia()));
        transacao.setCategoria(validarCategoriaSeInformada(dto.getCategoriaId()));

        return transacaoBancariaRepository.save(transacao);
    }

    @Transactional
    public void excluir(Long id, Long usuarioId, String usuarioAuditoria) {
        TransacaoBancaria transacao = buscarPorIdEUsuario(id, usuarioId);

        if (transacao.isPagamentoFatura()) {
            throw new RegraNegocioException("msg.transacao.pagamento-fatura.exclusao-bloqueada");
        }

        if (transacao.getOrigem() == OrigemLancamento.OPEN_FINANCE) {
            throw new RegraNegocioException("msg.transacao.open-finance.exclusao-bloqueada");
        }

        transacao.setDataExclusao(Instant.now());
        transacao.setExcluidoPor(usuarioAuditoria);
        transacaoBancariaRepository.save(transacao);
    }

    @Transactional
    public TransacaoBancaria duplicar(Long id, String novaCompetenciaStr, LocalDate novaData, Long usuarioId, String usuarioAuditoria) {
        TransacaoBancaria original = buscarPorIdEUsuario(id, usuarioId);

        if (original.isPagamentoFatura() || original.getOrigem() == OrigemLancamento.OPEN_FINANCE) {
            throw new RegraNegocioException("msg.transacao.duplicada.origemInvalida");
        }

        YearMonth novaCompetencia = (novaCompetenciaStr != null && !novaCompetenciaStr.isBlank())
            ? YearMonth.parse(novaCompetenciaStr.trim())
            : original.getCompetencia();

        if (novaCompetencia.equals(original.getCompetencia())) {
            throw new RegraNegocioException("novaCompetencia", "msg.transacao.duplicada.mesmaCompetencia");
        }

        LocalDate dataFinal;
        if (novaData != null) {
            dataFinal = novaData;
        } else {
            int dia = Math.min(original.getDataLancamento().getDayOfMonth(), novaCompetencia.lengthOfMonth());
            dataFinal = novaCompetencia.atDay(dia);
        }

        TransacaoBancaria clone = new TransacaoBancaria();
        clone.setDescricao(original.getDescricao());
        clone.setValor(original.getValor());
        clone.setNatureza(original.getNatureza());
        clone.setDataLancamento(dataFinal);
        clone.setCompetencia(novaCompetencia);
        clone.setConta(original.getConta());
        clone.setCategoria(original.getCategoria());
        clone.setOrigem(OrigemLancamento.MANUAL);
        clone.setPagamentoFatura(false);

        return transacaoBancariaRepository.save(clone);
    }

    private Conta validarConta(Long contaId, Long usuarioId) {
        return contaRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(contaId, usuarioId)
            .filter(Conta::isAtivo)
            .orElseThrow(() -> new RegraNegocioException("contaId", "msg.transacao.conta.invalida"));
    }

    private Categoria validarCategoriaSeInformada(Long categoriaId) {
        if (categoriaId == null) {
            return null;
        }
        return categoriaRepository.findByIdAndDataExclusaoIsNull(categoriaId)
            .filter(Categoria::isAtivo)
            .orElseThrow(() -> new RegraNegocioException("categoriaId", "msg.transacao.categoria.invalida"));
    }

    private TransacaoBancariaGridDTO converterParaGridDTO(TransacaoBancaria t) {
        return TransacaoBancariaGridDTO.builder()
            .id(t.getId())
            .competencia(t.getCompetencia())
            .descricao(t.getDescricao())
            .valor(t.getValor())
            .natureza(t.getNatureza())
            .dataLancamento(t.getDataLancamento())
            .pagamentoFatura(t.isPagamentoFatura())
            .idExterno(t.getIdExterno())
            .origem(t.getOrigem())
            .contaId(t.getConta() != null ? t.getConta().getId() : null)
            .contaDescricao(t.getConta() != null ? t.getConta().getDescricao() : null)
            .categoriaId(t.getCategoria() != null ? t.getCategoria().getId() : null)
            .categoriaNome(t.getCategoria() != null ? t.getCategoria().getNome() : null)
            .excluido(t.isExcluido())
            .build();
    }

    private String normalizarTexto(String texto) {
        return (texto != null) ? texto.trim() : "";
    }
}
