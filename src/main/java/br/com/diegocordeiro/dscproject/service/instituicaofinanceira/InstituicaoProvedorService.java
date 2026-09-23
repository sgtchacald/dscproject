package br.com.diegocordeiro.dscproject.service.instituicaofinanceira;

import br.com.diegocordeiro.dscproject.dto.instituicaoprovedor.InstituicaoProvedorEdicaoDTO;
import br.com.diegocordeiro.dscproject.dto.instituicaoprovedor.InstituicaoProvedorFormDTO;
import br.com.diegocordeiro.dscproject.dto.instituicaoprovedor.InstituicaoProvedorGridDTO;
import br.com.diegocordeiro.dscproject.dto.instituicaoprovedor.ProvedorOpcaoDTO;
import br.com.diegocordeiro.dscproject.model.instituicaofinanceira.InstituicaoFinanceira;
import br.com.diegocordeiro.dscproject.model.instituicaofinanceira.OpenFinanceInstituicaoProvedor;
import br.com.diegocordeiro.dscproject.model.instituicaofinanceira.OpenFinanceProvedor;
import br.com.diegocordeiro.dscproject.repository.instituicaofinanceira.InstituicaoFinanceiraRepository;
import br.com.diegocordeiro.dscproject.repository.instituicaofinanceira.OpenFinanceInstituicaoProvedorRepository;
import br.com.diegocordeiro.dscproject.repository.instituicaofinanceira.OpenFinanceProvedorRepository;
import br.com.diegocordeiro.dscproject.service.exceptions.RegistroNaoEncontradoException;
import br.com.diegocordeiro.dscproject.service.exceptions.RegraNegocioException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class InstituicaoProvedorService {

    private final OpenFinanceInstituicaoProvedorRepository openFinanceInstituicaoProvedorRepository;
    private final InstituicaoFinanceiraRepository instituicaoFinanceiraRepository;
    private final OpenFinanceProvedorRepository openFinanceProvedorRepository;

    public InstituicaoProvedorService(OpenFinanceInstituicaoProvedorRepository openFinanceInstituicaoProvedorRepository, InstituicaoFinanceiraRepository instituicaoFinanceiraRepository, OpenFinanceProvedorRepository openFinanceProvedorRepository) {
        this.openFinanceInstituicaoProvedorRepository = openFinanceInstituicaoProvedorRepository;
        this.instituicaoFinanceiraRepository = instituicaoFinanceiraRepository;
        this.openFinanceProvedorRepository = openFinanceProvedorRepository;
    }

    @Transactional(readOnly = true)
    public List<InstituicaoProvedorGridDTO> listarParaGrid() {
        return openFinanceInstituicaoProvedorRepository.listarParaGrid().stream()
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
        OpenFinanceInstituicaoProvedor entity = openFinanceInstituicaoProvedorRepository.findByIdAndDataExclusaoIsNull(id)
            .orElseThrow(() -> new RegistroNaoEncontradoException("msg.instituicaoprovedor.nao-encontrado"));

        return InstituicaoProvedorEdicaoDTO.builder()
            .id(entity.getId())
            .idExterno(entity.getIdExterno())
            .provedorId(entity.getProvedor().getId())
            .instituicaoId(entity.getInstituicao().getId())
            .build();
    }

    @Transactional
    public OpenFinanceInstituicaoProvedor inserir(InstituicaoProvedorFormDTO dto) {
        validarUnicidade(dto.getInstituicaoId(), dto.getProvedorId(), dto.getIdExterno(), null);

        InstituicaoFinanceira instituicao = obterInstituicaoAtiva(dto.getInstituicaoId());
        OpenFinanceProvedor provedor = obterProvedorAtivo(dto.getProvedorId());

        OpenFinanceInstituicaoProvedor entity = new OpenFinanceInstituicaoProvedor();
        entity.setIdExterno(dto.getIdExterno().trim());
        entity.setInstituicao(instituicao);
        entity.setProvedor(provedor);

        return openFinanceInstituicaoProvedorRepository.save(entity);
    }

    @Transactional
    public OpenFinanceInstituicaoProvedor editar(Long id, InstituicaoProvedorFormDTO dto) {
        OpenFinanceInstituicaoProvedor entity = openFinanceInstituicaoProvedorRepository.findByIdAndDataExclusaoIsNull(id)
            .orElseThrow(() -> new RegistroNaoEncontradoException("msg.instituicaoprovedor.nao-encontrado"));

        validarUnicidade(dto.getInstituicaoId(), dto.getProvedorId(), dto.getIdExterno(), id);

        InstituicaoFinanceira instituicao = obterInstituicaoAtiva(dto.getInstituicaoId());
        OpenFinanceProvedor provedor = obterProvedorAtivo(dto.getProvedorId());

        entity.setIdExterno(dto.getIdExterno().trim());
        entity.setInstituicao(instituicao);
        entity.setProvedor(provedor);

        return openFinanceInstituicaoProvedorRepository.save(entity);
    }

    @Transactional
    public void excluir(Long id, String usuarioLogado) {
        OpenFinanceInstituicaoProvedor entity = openFinanceInstituicaoProvedorRepository.findByIdAndDataExclusaoIsNull(id)
            .orElseThrow(() -> new RegistroNaoEncontradoException("msg.instituicaoprovedor.nao-encontrado"));

        entity.setDataExclusao(Instant.now());
        entity.setExcluidoPor(usuarioLogado);
        openFinanceInstituicaoProvedorRepository.save(entity);
    }

    @Transactional(readOnly = true)
    public List<ProvedorOpcaoDTO> listarProvedoresOpcoes() {
        return openFinanceProvedorRepository.findByDataExclusaoIsNullOrderByNomeAsc().stream()
            .map(p -> new ProvedorOpcaoDTO(p.getId(), p.getCodigo(), p.getNome(), p.isAtivo()))
            .toList();
    }

    private void validarUnicidade(Long instituicaoId, Long provedorId, String idExterno, Long idAtual) {
        if (openFinanceInstituicaoProvedorRepository.contarPorInstituicaoEProvedor(instituicaoId, provedorId, idAtual) > 0) {
            throw new RegraNegocioException("instituicaoId", "msg.instituicaoprovedor.instituicao.duplicada");
        }

        if (idExterno != null && openFinanceInstituicaoProvedorRepository.contarPorProvedorEIdExterno(provedorId, idExterno.trim(), idAtual) > 0) {
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

    private OpenFinanceProvedor obterProvedorAtivo(Long provedorId) {
        OpenFinanceProvedor p = openFinanceProvedorRepository.findById(provedorId)
            .orElseThrow(() -> new RegraNegocioException("provedorId", "msg.instituicaoprovedor.provedor.invalido"));

        if (!p.isAtivo() || p.isExcluido()) {
            throw new RegraNegocioException("provedorId", "msg.instituicaoprovedor.provedor.invalido");
        }
        return p;
    }
}
