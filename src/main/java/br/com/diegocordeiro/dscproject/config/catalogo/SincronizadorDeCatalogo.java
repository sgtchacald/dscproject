package br.com.diegocordeiro.dscproject.config.catalogo;

import br.com.diegocordeiro.dscproject.dto.catalogo.SincronizacaoCatalogoDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Reflete um catálogo do código numa tabela: <b>insere</b> o que falta,
 * <b>atualiza</b> os metadados do que mudou, <b>marca como órfã</b> a linha que
 * sumiu do código e <b>desmarca</b> a que voltou. <b>Nunca apaga linha</b> — o
 * vínculo histórico depende dela.
 *
 * <p>Cada catálogo concreto é um {@code @Service} que estende esta classe e diz
 * de onde vêm os itens do código, qual o repositório da tabela e como uma linha
 * nasce e recebe os metadados. O algoritmo mora aqui, uma vez só.</p>
 *
 * @param <D> item do catálogo no código (ex.: {@code PermissaoDefinida})
 * @param <L> linha da tabela que o espelha (ex.: {@code Permissao})
 */
public abstract class SincronizadorDeCatalogo<D extends ItemCatalogo, L extends LinhaCatalogo> {

    private final Logger log = LoggerFactory.getLogger(getClass());

    /** Itens do código, fonte da verdade — tipicamente via {@link DescobridorDeCatalogo}. */
    protected abstract List<D> itensDoCodigo();

    /** Repositório da tabela que espelha o catálogo. */
    protected abstract JpaRepository<L, ?> repositorio();

    /** Linha nova a partir de um item do código (valores derivados do próprio item). */
    protected abstract L novaLinha(D definicao);

    /** Copia para a linha existente os metadados que a tela não edita (nome, descrição, módulo, …). */
    protected abstract void copiarMetadados(D definicao, L linha);

    @Transactional
    public SincronizacaoCatalogoDTO sincronizar() {

        List<D> catalogo = itensDoCodigo();
        log.info("{}: {} itens no código.", getClass().getSimpleName(), catalogo.size());

        Set<String> codigosDoCodigo = catalogo.stream()
            .map(ItemCatalogo::getCodigo)
            .collect(Collectors.toSet());

        Map<String, L> existentes = repositorio().findAll().stream()
            .collect(Collectors.toMap(LinhaCatalogo::getCodigo, Function.identity()));

        int inseridas = 0;
        for (D definicao : catalogo) {
            L linha = existentes.get(definicao.getCodigo());
            if (linha == null) {
                repositorio().save(novaLinha(definicao));
                inseridas++;
            } else {
                copiarMetadados(definicao, linha);
                linha.setOrfa(false);
            }
        }

        int orfas = 0;
        for (L linha : existentes.values()) {
            if (!codigosDoCodigo.contains(linha.getCodigo()) && !linha.isOrfa()) {
                linha.setOrfa(true);
                orfas++;
            }
        }

        return new SincronizacaoCatalogoDTO(inseridas, orfas);
    }
}
