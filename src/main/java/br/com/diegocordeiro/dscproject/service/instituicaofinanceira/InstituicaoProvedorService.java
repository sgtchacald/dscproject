package br.com.diegocordeiro.dscproject.service.instituicaofinanceira;

import br.com.diegocordeiro.dscproject.dto.instituicaoprovedor.InstituicaoProvedorEdicaoDTO;
import br.com.diegocordeiro.dscproject.dto.instituicaoprovedor.InstituicaoProvedorFormDTO;
import br.com.diegocordeiro.dscproject.dto.instituicaoprovedor.InstituicaoProvedorGridDTO;
import br.com.diegocordeiro.dscproject.dto.instituicaoprovedor.ProvedorOpcaoDTO;
import br.com.diegocordeiro.dscproject.model.instituicaofinanceira.InstituicaoFinanceira;
import br.com.diegocordeiro.dscproject.model.instituicaofinanceira.OpfiInstituicaoProvedor;
import br.com.diegocordeiro.dscproject.model.instituicaofinanceira.OpfiProvedor;
import br.com.diegocordeiro.dscproject.repository.instituicaofinanceira.InstituicaoFinanceiraRepository;
import br.com.diegocordeiro.dscproject.repository.instituicaofinanceira.OpfiInstituicaoProvedorRepository;
import br.com.diegocordeiro.dscproject.repository.instituicaofinanceira.OpfiProvedorRepository;
import br.com.diegocordeiro.dscproject.service.exceptions.RegistroNaoEncontradoException;
import br.com.diegocordeiro.dscproject.service.exceptions.RegraNegocioException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class InstituicaoProvedorService {

    private final OpfiInstituicaoProvedorRepository opfiInstituicaoProvedorRepository;
    private final InstituicaoFinanceiraRepository instituicaoFinanceiraRepository;
    private final OpfiProvedorRepository opfiProvedorRepository;

    public InstituicaoProvedorService(OpfiInstituicaoProvedorRepository opfiInstituicaoProvedorRepository, InstituicaoFinanceiraRepository instituicaoFinanceiraRepository, OpfiProvedorRepository opfiProvedorRepository) {
        this.opfiInstituicaoProvedorRepository = opfiInstituicaoProvedorRepository;
        this.instituicaoFinanceiraRepository = instituicaoFinanceiraRepository;
        this.opfiProvedorRepository = opfiProvedorRepository;
    }

    @Transactional(readOnly = true)
    public List<InstituicaoProvedorGridDTO> listarParaGrid() {
        return opfiInstituicaoProvedorRepository.listarParaGrid().stream()
            .map(m -> InstituicaoProvedorGridDTO.builder()
                .id(m.getId())
                .idExterno(m.getIdExterno())
                .provedorId(m.getProvedor().getId())
                .provedorNome(m.getProvedor().getNome())
                .instituicaoId(m.getInstituicao().getId())
                .instituicaoNome(m.getInstituicao().getNome())
                .build())
            .toList();
    }

    @Transactional(readOnly = true)
    public InstituicaoProvedorEdicaoDTO buscarParaEdicao(Long id) {
        OpfiInstituicaoProvedor entity = opfiInstituicaoProvedorRepository.findByIdAndDataExclusaoIsNull(id)
            .orElseThrow(() -> new RegistroNaoEncontradoException("msg.instituicaoprovedor.nao-encontrado"));

        return InstituicaoProvedorEdicaoDTO.builder()
            .id(entity.getId())
            .idExterno(entity.getIdExterno())
            .provedorId(entity.getProvedor().getId())
            .instituicaoId(entity.getInstituicao().getId())
            .build();
    }

    @Transactional
    public OpfiInstituicaoProvedor inserir(InstituicaoProvedorFormDTO dto) {
        validarUnicidade(dto.getInstituicaoId(), dto.getProvedorId(), dto.getIdExterno(), null);

        InstituicaoFinanceira instituicao = obterInstituicaoAtiva(dto.getInstituicaoId());
        OpfiProvedor provedor = obterProvedorAtivo(dto.getProvedorId());

        OpfiInstituicaoProvedor entity = new OpfiInstituicaoProvedor();
        entity.setIdExterno(dto.getIdExterno().trim());
        entity.setInstituicao(instituicao);
        entity.setProvedor(provedor);

        return opfiInstituicaoProvedorRepository.save(entity);
    }

    @Transactional
    public OpfiInstituicaoProvedor editar(Long id, InstituicaoProvedorFormDTO dto) {
        OpfiInstituicaoProvedor entity = opfiInstituicaoProvedorRepository.findByIdAndDataExclusaoIsNull(id)
            .orElseThrow(() -> new RegistroNaoEncontradoException("msg.instituicaoprovedor.nao-encontrado"));

        validarUnicidade(dto.getInstituicaoId(), dto.getProvedorId(), dto.getIdExterno(), id);

        InstituicaoFinanceira instituicao = obterInstituicaoAtiva(dto.getInstituicaoId());
        OpfiProvedor provedor = obterProvedorAtivo(dto.getProvedorId());

        entity.setIdExterno(dto.getIdExterno().trim());
        entity.setInstituicao(instituicao);
        entity.setProvedor(provedor);

        return opfiInstituicaoProvedorRepository.save(entity);
    }

    @Transactional
    public void excluir(Long id, String usuarioLogado) {
        OpfiInstituicaoProvedor entity = opfiInstituicaoProvedorRepository.findByIdAndDataExclusaoIsNull(id)
            .orElseThrow(() -> new RegistroNaoEncontradoException("msg.instituicaoprovedor.nao-encontrado"));

        entity.setDataExclusao(Instant.now());
        entity.setExcluidoPor(usuarioLogado);
        opfiInstituicaoProvedorRepository.save(entity);
    }

    @Transactional(readOnly = true)
    public List<ProvedorOpcaoDTO> listarProvedoresOpcoes() {
        return opfiProvedorRepository.findByDataExclusaoIsNullOrderByNomeAsc().stream()
            .map(p -> new ProvedorOpcaoDTO(p.getId(), p.getCodigo(), p.getNome(), p.isAtivo()))
            .toList();
    }

    private void validarUnicidade(Long instituicaoId, Long provedorId, String idExterno, Long idAtual) {
        if (opfiInstituicaoProvedorRepository.contarPorInstituicaoEProvedor(instituicaoId, provedorId, idAtual) > 0) {
            throw new RegraNegocioException("instituicaoId", "msg.instituicaoprovedor.instituicao.duplicada");
        }

        if (idExterno != null && opfiInstituicaoProvedorRepository.contarPorProvedorEIdExterno(provedorId, idExterno.trim(), idAtual) > 0) {
            throw new RegraNegocioException("idExterno", "msg.instituicaoprovedor.idexterno.duplicado");
        }
    }

    private InstituicaoFinanceira obterInstituicaoAtiva(Long instituicaoId) {
        InstituicaoFinanceira i = instituicaoFinanceiraRepository.findByIdAndDataExclusaoIsNull(instituicaoId)
            .orElseThrow(() -> new RegraNegocioException("instituicaoId", "msg.instituicaoprovedor.instituicao.invalida"));

        if (!i.isAtivo()) {
            throw new RegraNegocioException("instituicaoId", "msg.instituicaoprovedor.instituicao.invalida");
        }
        return i;
    }

    private OpfiProvedor obterProvedorAtivo(Long provedorId) {
        OpfiProvedor p = opfiProvedorRepository.findById(provedorId)
            .orElseThrow(() -> new RegraNegocioException("provedorId", "msg.instituicaoprovedor.provedor.invalido"));

        if (!p.isAtivo() || p.isExcluido()) {
            throw new RegraNegocioException("provedorId", "msg.instituicaoprovedor.provedor.invalido");
        }
        return p;
    }
}
