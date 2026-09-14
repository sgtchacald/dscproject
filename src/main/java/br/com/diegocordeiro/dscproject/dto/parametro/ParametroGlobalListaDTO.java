package br.com.diegocordeiro.dscproject.dto.parametro;

import br.com.diegocordeiro.dscproject.enums.TipoParametro;
import br.com.diegocordeiro.dscproject.model.parametro.ParametroGlobal;
import lombok.Getter;

import java.time.Instant;

/** Linha do grid de parâmetros globais. */
@Getter
public class ParametroGlobalListaDTO {

    private final Long id;
    private final String codigo;
    private final String nome;
    private final String descricao;
    private final String modulo;
    private final TipoParametro tipoDado;
    private final String valor;
    private final String valorDefault;
    private final boolean orfa;
    private final String motivo;
    /** Autor da última alteração pela tela — nulo enquanto o parâmetro só passou pela carga. */
    private final String alteradoPor;
    /** Data da última alteração pela tela — nula enquanto o parâmetro só passou pela carga. */
    private final Instant dataAlteracao;

    public ParametroGlobalListaDTO(ParametroGlobal parametro) {
        this.id = parametro.getId();
        this.codigo = parametro.getCodigo();
        this.nome = parametro.getNome();
        this.descricao = parametro.getDescricao();
        this.modulo = parametro.getModulo();
        this.tipoDado = parametro.getTipoDado();
        this.valor = parametro.getValor();
        this.valorDefault = parametro.getValorDefault();
        this.orfa = parametro.isOrfa();
        this.motivo = parametro.getMotivo();
        // "última alteração" é a última edição feita pela tela; a carga/sincronização não conta
        boolean editadoPelaTela = parametro.getMotivo() != null;
        this.alteradoPor = editadoPelaTela ? parametro.getAlteradoPor() : null;
        this.dataAlteracao = editadoPelaTela ? parametro.getDataAlteracao() : null;
    }
}
