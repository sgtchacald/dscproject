package br.com.diegocordeiro.dscproject.service.parametro;

import br.com.diegocordeiro.dscproject.dto.parametro.ParametroGlobalEdicaoDTO;
import br.com.diegocordeiro.dscproject.dto.parametro.ParametroGlobalListaDTO;
import br.com.diegocordeiro.dscproject.dto.parametro.ParametroValorFormDTO;
import br.com.diegocordeiro.dscproject.dto.parametro.RevisaoParametroDTO;
import br.com.diegocordeiro.dscproject.model.parametro.ParametroGlobal;
import br.com.diegocordeiro.dscproject.config.parametro.ParametroValorValidador;
import br.com.diegocordeiro.dscproject.repository.parametro.ParametroGlobalRepository;
import br.com.diegocordeiro.dscproject.service.exceptions.RegistroNaoEncontradoException;
import br.com.diegocordeiro.dscproject.service.exceptions.RegraNegocioException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Regra de negócio dos parâmetros globais: listagem, edição inline do valor
 * (sempre com motivo), restauração ao padrão e leitura das revisões (Envers).
 * A tela edita apenas {@code valor} e {@code motivo} — nome, código, módulo e
 * tipo vêm do catálogo do código e não são graváveis aqui.
 */
@Service
public class ParametroGlobalService {

    private final ParametroGlobalRepository parametroGlobalRepository;

    /**
     * Quando verdadeiro, um parâmetro órfão não pode ser editado pela tela
     * (retorna a mensagem de órfão). Padrão: falso — o parâmetro órfão pode ter
     * o valor ajustado, com o aviso exibido no modal, porque rotinas antigas
     * ainda podem estar lendo o código.
     */
    private final boolean orfaoEdicaoBloqueada;

    public ParametroGlobalService(ParametroGlobalRepository parametroGlobalRepository, @Value("${parametros.orfao-edicao-bloqueada:false}") boolean orfaoEdicaoBloqueada) {
        this.parametroGlobalRepository = parametroGlobalRepository;
        this.orfaoEdicaoBloqueada = orfaoEdicaoBloqueada;
    }

    @Transactional(readOnly = true)
    public List<ParametroGlobalListaDTO> listarParaGrid() {
        return parametroGlobalRepository.findByDataExclusaoIsNullOrderByModuloAscNomeAsc().stream()
            .map(ParametroGlobalListaDTO::new)
            .toList();
    }

    @Transactional(readOnly = true)
    public ParametroGlobalEdicaoDTO buscarParaEdicao(Long id) {
        return new ParametroGlobalEdicaoDTO(buscarAtivo(id));
    }

    /** Alteração inline do valor, confirmada no modal com o motivo. */
    @Transactional
    public void editarValor(Long id, ParametroValorFormDTO dto) {
        ParametroGlobal parametro = buscarAtivo(id);
        recusarSeOrfaoTravado(parametro);
        validarValorContraTipo(parametro, dto.getValor());
        parametro.setValor(dto.getValor());
        parametro.setMotivo(dto.getMotivo());
        parametroGlobalRepository.save(parametro);
    }

    /** Restaura o valor ao padrão semeado pelo catálogo do código. */
    @Transactional
    public void restaurarPadrao(Long id, String motivo) {
        ParametroGlobal parametro = buscarAtivo(id);
        recusarSeOrfaoTravado(parametro);
        parametro.setValor(parametro.getValorDefault());
        parametro.setMotivo(motivo);
        parametroGlobalRepository.save(parametro);
    }

    /** Revisões de um parâmetro (Envers), da mais recente para a mais antiga. */
    @Transactional(readOnly = true)
    public Page<RevisaoParametroDTO> buscarHistorico(Long id, Pageable pageable) {
        buscarAtivo(id);
        List<RevisaoParametroDTO> revisoes = parametroGlobalRepository.findRevisions(id).reverse().getContent().stream()
            .map(RevisaoParametroDTO::new)
            .toList();
        int inicio = (int) Math.min(pageable.getOffset(), revisoes.size());
        int fim = Math.min(inicio + pageable.getPageSize(), revisoes.size());
        return new PageImpl<>(revisoes.subList(inicio, fim), pageable, revisoes.size());
    }

    /** Contrato de leitura do valor por código, para o resto do sistema. */
    @Transactional(readOnly = true)
    public String buscarValorPorCodigo(String codigo) {
        return parametroGlobalRepository.findByCodigo(codigo)
            .map(ParametroGlobal::getValor)
            .orElseThrow(() -> new RegistroNaoEncontradoException("parametro.nao.encontrado"));
    }

    private void recusarSeOrfaoTravado(ParametroGlobal parametro) {
        if (parametro.isOrfa() && orfaoEdicaoBloqueada) {
            throw new RegraNegocioException("parametro.orfao.nao.editavel");
        }
    }

    private void validarValorContraTipo(ParametroGlobal parametro, String valor) {
        ParametroValorValidador.chaveErro(parametro.getTipoDado(), valor)
            .ifPresent(chave -> {
                throw new RegraNegocioException("valor", chave);
            });
    }

    private ParametroGlobal buscarAtivo(Long id) {
        return parametroGlobalRepository.findByIdAndDataExclusaoIsNull(id)
            .orElseThrow(() -> new RegistroNaoEncontradoException("parametro.nao.encontrado"));
    }
}
