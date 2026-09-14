package br.com.diegocordeiro.dscproject.service.receita;

import br.com.diegocordeiro.dscproject.dto.receita.ReceitaEdicaoDTO;
import br.com.diegocordeiro.dscproject.dto.receita.ReceitaFormDTO;
import br.com.diegocordeiro.dscproject.dto.receita.ReceitaGridDTO;
import br.com.diegocordeiro.dscproject.enums.AplicaA;
import br.com.diegocordeiro.dscproject.enums.OrigemLancamento;
import br.com.diegocordeiro.dscproject.model.categoria.Categoria;
import br.com.diegocordeiro.dscproject.model.conta.Conta;
import br.com.diegocordeiro.dscproject.model.receita.Receita;
import br.com.diegocordeiro.dscproject.repository.categoria.CategoriaRepository;
import br.com.diegocordeiro.dscproject.repository.conta.ContaRepository;
import br.com.diegocordeiro.dscproject.repository.receita.ReceitaRepository;
import br.com.diegocordeiro.dscproject.service.exceptions.RegistroNaoEncontradoException;
import br.com.diegocordeiro.dscproject.service.exceptions.RegraNegocioException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
public class ReceitaService {

    private final ReceitaRepository receitaRepository;
    private final ContaRepository contaRepository;
    private final CategoriaRepository categoriaRepository;

    public ReceitaService(ReceitaRepository receitaRepository, ContaRepository contaRepository, CategoriaRepository categoriaRepository) {
        this.receitaRepository = receitaRepository;
        this.contaRepository = contaRepository;
        this.categoriaRepository = categoriaRepository;
    }

    @Transactional
    public Receita inserir(ReceitaFormDTO dto, Long usuarioId, String usuarioAuditoria) {
        Conta conta = validarConta(dto.getContaId(), usuarioId);
        Categoria categoria = validarCategoriaSeInformada(dto.getCategoriaId());

        Receita receita = new Receita();
        receita.setNome(normalizarTexto(dto.getNome()));
        receita.setDescricao(normalizarTexto(dto.getDescricao()));
        receita.setValor(dto.getValor());
        receita.setDataLancamento(dto.getDataLancamento());
        receita.setCompetencia(YearMonth.parse(dto.getCompetencia()));
        receita.setConta(conta);
        receita.setCategoria(categoria);
        receita.setOrigem(OrigemLancamento.MANUAL);

        aplicarRecebimento(receita, dto.isRecebido(), dto.getDataRecebimento());

        return receitaRepository.save(receita);
    }

    /** A receita de outro usuário (ou já excluída) responde como "não encontrada". */
    @Transactional(readOnly = true)
    public Receita buscarPorIdEUsuario(Long id, Long usuarioId) {
        return receitaRepository.findByIdAndContaUsuarioIdAndDataExclusaoIsNull(id, usuarioId)
            .orElseThrow(() -> new RegistroNaoEncontradoException("msg.receita.nao-encontrada"));
    }

    @Transactional(readOnly = true)
    public ReceitaEdicaoDTO buscarParaEdicao(Long id, Long usuarioId) {
        Receita r = buscarPorIdEUsuario(id, usuarioId);
        return ReceitaEdicaoDTO.builder()
            .id(r.getId())
            .competencia(r.getCompetencia())
            .nome(r.getNome())
            .descricao(r.getDescricao())
            .valor(r.getValor())
            .dataLancamento(r.getDataLancamento())
            .dataRecebimento(r.getDataRecebimento())
            .recebido(r.isRecebido())
            .origem(r.getOrigem())
            .contaId(r.getConta() != null ? r.getConta().getId() : null)
            .categoriaId(r.getCategoria() != null ? r.getCategoria().getId() : null)
            .build();
    }

    @Transactional
    public Receita editar(Long id, ReceitaFormDTO dto, Long usuarioId, String usuarioAuditoria) {
        Receita receita = buscarPorIdEUsuario(id, usuarioId);

        // conta e origem de uma receita importada não são alteráveis; o contaId enviado é ignorado.
        if (receita.getOrigem() == OrigemLancamento.MANUAL) {
            receita.setConta(validarConta(dto.getContaId(), usuarioId));
        }

        receita.setNome(normalizarTexto(dto.getNome()));
        receita.setDescricao(normalizarTexto(dto.getDescricao()));
        receita.setValor(dto.getValor());
        receita.setDataLancamento(dto.getDataLancamento());
        receita.setCompetencia(YearMonth.parse(dto.getCompetencia()));
        receita.setCategoria(validarCategoriaSeInformada(dto.getCategoriaId()));

        aplicarRecebimento(receita, dto.isRecebido(), dto.getDataRecebimento());

        return receitaRepository.save(receita);
    }

    @Transactional
    public Receita marcarRecebida(Long id, LocalDate dataRecebimento, Long usuarioId, String usuarioAuditoria) {
        Receita receita = buscarPorIdEUsuario(id, usuarioId);
        receita.setRecebido(true);
        receita.setDataRecebimento(dataRecebimento);
        return receitaRepository.save(receita);
    }

    @Transactional
    public void excluir(Long id, Long usuarioId, String usuarioAuditoria) {
        Receita receita = buscarPorIdEUsuario(id, usuarioId);

        // só a receita lançada manualmente é excluível por esta tela; a receita é folha, sem checagem de uso.
        if (receita.getOrigem() != OrigemLancamento.MANUAL) {
            throw new RegraNegocioException("msg.receita.importada.nao-excluivel");
        }

        receita.setDataExclusao(java.time.Instant.now());
        receita.setExcluidoPor(usuarioAuditoria);
        receitaRepository.save(receita);
    }

    @Transactional
    public List<Receita> duplicar(List<Long> ids, String competenciaAlvo, Long usuarioId, String usuarioAuditoria) {
        if (ids == null || ids.isEmpty()) {
            throw new RegraNegocioException("msg.receita.duplicar.vazio");
        }
        YearMonth comp = (competenciaAlvo != null && !competenciaAlvo.isBlank()) ? YearMonth.parse(competenciaAlvo) : null;
        return ids.stream().map(id -> {
            Receita origem = buscarPorIdEUsuario(id, usuarioId);
            Receita copia = new Receita();
            copia.setNome(origem.getNome());
            copia.setDescricao(origem.getDescricao());
            copia.setValor(origem.getValor());
            copia.setDataLancamento(origem.getDataLancamento());
            copia.setCompetencia(comp != null ? comp : origem.getCompetencia());
            copia.setConta(origem.getConta());
            copia.setCategoria(origem.getCategoria());
            copia.setOrigem(OrigemLancamento.MANUAL);
            copia.setRecebido(false);
            copia.setDataRecebimento(null);
            copia.setCriadoPor(usuarioAuditoria);
            copia.setAlteradoPor(usuarioAuditoria);
            return receitaRepository.save(copia);
        }).toList();
    }

    private void aplicarRecebimento(Receita receita, boolean recebido, LocalDate dataRecebimento) {
        receita.setRecebido(recebido);
        receita.setDataRecebimento(recebido ? dataRecebimento : null);
    }

    private Conta validarConta(Long contaId, Long usuarioId) {
        return contaRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(contaId, usuarioId)
            .filter(Conta::isAtivo)
            .orElseThrow(() -> new RegraNegocioException("contaId", "msg.receita.conta.invalida"));
    }

    private Categoria validarCategoriaSeInformada(Long categoriaId) {
        if (categoriaId == null) {
            return null;
        }
        return categoriaRepository.findByIdAndDataExclusaoIsNull(categoriaId)
            .filter(Categoria::isAtivo)
            .filter(c -> c.getAplicaA() == AplicaA.RECEITA || c.getAplicaA() == AplicaA.AMBOS)
            .orElseThrow(() -> new RegraNegocioException("categoriaId", "msg.receita.categoria.invalida"));
    }

    private String normalizarTexto(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return valor.trim();
    }

    @Transactional(readOnly = true)
    public List<ReceitaGridDTO> listarParaGrid(Long usuarioId) {
        List<Receita> receitas = receitaRepository.listarPorUsuario(usuarioId);
        return receitas.stream().map(this::paraGridDTO).toList();
    }

    private ReceitaGridDTO paraGridDTO(Receita r) {
        return ReceitaGridDTO.builder()
            .id(r.getId())
            .competencia(r.getCompetencia())
            .nome(r.getNome())
            .descricao(r.getDescricao())
            .valor(r.getValor())
            .dataLancamento(r.getDataLancamento())
            .dataRecebimento(r.getDataRecebimento())
            .recebido(r.isRecebido())
            .origem(r.getOrigem())
            .contaId(r.getConta() != null ? r.getConta().getId() : null)
            .contaDescricao(r.getConta() != null ? r.getConta().getDescricao() : null)
            .categoriaId(r.getCategoria() != null ? r.getCategoria().getId() : null)
            .categoriaNome(r.getCategoria() != null ? r.getCategoria().getNome() : null)
            .excluido(r.isExcluido())
            .build();
    }
}
