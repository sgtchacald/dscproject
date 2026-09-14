package br.com.diegocordeiro.dscproject.service.perfil;

import br.com.diegocordeiro.dscproject.config.catalogo.DescobridorDeCatalogo;
import br.com.diegocordeiro.dscproject.config.catalogo.SincronizadorDeCatalogo;
import br.com.diegocordeiro.dscproject.dto.permissao.PermissaoCatalogoDTO;
import br.com.diegocordeiro.dscproject.model.perfil.Permissao;
import br.com.diegocordeiro.dscproject.config.permissao.PermissaoDefinida;
import br.com.diegocordeiro.dscproject.repository.perfil.PermissaoRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Catálogo de permissões: leitura para a tela e sincronização de {@code PERMISSOES}
 * com o catálogo do código. A tela nunca cria nem apaga permissão — só o
 * sincronizador escreve nesta tabela, e ele nunca remove uma linha. O algoritmo
 * de sincronização mora em {@link SincronizadorDeCatalogo}; aqui só a ligação com
 * o pacote {@code permissao} e o repositório.
 */
@Service
public class PermissaoCatalogoService extends SincronizadorDeCatalogo<PermissaoDefinida, Permissao> {

    private static final String PACOTE_PERMISSOES = "br.com.diegocordeiro.dscproject.config.permissao";

    private final PermissaoRepository permissaoRepository;

    public PermissaoCatalogoService(PermissaoRepository permissaoRepository) {
        this.permissaoRepository = permissaoRepository;
    }

    @Transactional(readOnly = true)
    public List<PermissaoCatalogoDTO> listar() {
        return permissaoRepository.findByDataExclusaoIsNullOrderByModuloAscNomeAsc().stream()
            .map(PermissaoCatalogoDTO::new)
            .toList();
    }

    @Override
    protected List<PermissaoDefinida> itensDoCodigo() {
        return DescobridorDeCatalogo.noPacote(PACOTE_PERMISSOES, PermissaoDefinida.class);
    }

    @Override
    protected JpaRepository<Permissao, ?> repositorio() {
        return permissaoRepository;
    }

    @Override
    protected Permissao novaLinha(PermissaoDefinida definicao) {
        Permissao linha = new Permissao(definicao.getCodigo(), definicao.getNome(), definicao.getDescricao(), definicao.getModulo());
        linha.setConcedivelPorPlano(definicao.isConcedivelPorPlano());
        return linha;
    }

    @Override
    protected void copiarMetadados(PermissaoDefinida definicao, Permissao linha) {
        linha.setNome(definicao.getNome());
        linha.setDescricao(definicao.getDescricao());
        linha.setModulo(definicao.getModulo());
        linha.setConcedivelPorPlano(definicao.isConcedivelPorPlano());
    }
}
