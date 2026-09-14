package br.com.diegocordeiro.dscproject.service.parametro;

import br.com.diegocordeiro.dscproject.config.catalogo.DescobridorDeCatalogo;
import br.com.diegocordeiro.dscproject.config.catalogo.SincronizadorDeCatalogo;
import br.com.diegocordeiro.dscproject.model.parametro.ParametroGlobal;
import br.com.diegocordeiro.dscproject.config.parametro.ParametroDefinido;
import br.com.diegocordeiro.dscproject.repository.parametro.ParametroGlobalRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Sincroniza a tabela {@code PARAMETROS_GLOBAIS} com o catálogo de parâmetros do
 * código. A tela nunca cria nem apaga parâmetro — só o sincronizador escreve a
 * existência de uma linha, e ele nunca remove uma (histórico de revisões +
 * leituras antigas por código). O algoritmo mora em {@link SincronizadorDeCatalogo};
 * aqui só a ligação com o pacote {@code parametro} e o repositório, e a regra de
 * que {@code PAGL_VALOR} pertence ao operador — nunca sobrescrito na sincronização.
 */
@Service
public class ParametroCatalogoService extends SincronizadorDeCatalogo<ParametroDefinido, ParametroGlobal> {

    private static final String PACOTE_PARAMETROS = "br.com.diegocordeiro.dscproject.config.parametro";

    private final ParametroGlobalRepository parametroGlobalRepository;

    public ParametroCatalogoService(ParametroGlobalRepository parametroGlobalRepository) {
        this.parametroGlobalRepository = parametroGlobalRepository;
    }

    @Override
    protected List<ParametroDefinido> itensDoCodigo() {
        return DescobridorDeCatalogo.noPacote(PACOTE_PARAMETROS, ParametroDefinido.class);
    }

    @Override
    protected JpaRepository<ParametroGlobal, ?> repositorio() {
        return parametroGlobalRepository;
    }

    @Override
    protected ParametroGlobal novaLinha(ParametroDefinido definicao) {
        return new ParametroGlobal(definicao);
    }

    @Override
    protected void copiarMetadados(ParametroDefinido definicao, ParametroGlobal linha) {
        linha.setNome(definicao.getNome());
        linha.setDescricao(definicao.getDescricao());
        linha.setModulo(definicao.getModulo());
        linha.setTipoDado(definicao.getTipo());
        linha.setValorDefault(definicao.getValorDefault());
        // PAGL_VALOR pertence ao operador — nunca sobrescrito aqui
    }
}
