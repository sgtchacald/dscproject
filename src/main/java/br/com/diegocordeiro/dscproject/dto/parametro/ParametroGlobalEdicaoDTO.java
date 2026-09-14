package br.com.diegocordeiro.dscproject.dto.parametro;

import br.com.diegocordeiro.dscproject.enums.TipoParametro;
import br.com.diegocordeiro.dscproject.model.parametro.ParametroGlobal;
import lombok.Getter;

/**
 * Estado atual de um parâmetro — relido pela tela antes de confirmar uma
 * alteração inline, para detectar edição concorrente.
 */
@Getter
public class ParametroGlobalEdicaoDTO {

    private final Long id;
    private final String codigo;
    private final String nome;
    private final String descricao;
    private final String modulo;
    private final TipoParametro tipoDado;
    private final String valor;
    private final String valorDefault;
    private final boolean orfa;

    public ParametroGlobalEdicaoDTO(ParametroGlobal parametro) {
        this.id = parametro.getId();
        this.codigo = parametro.getCodigo();
        this.nome = parametro.getNome();
        this.descricao = parametro.getDescricao();
        this.modulo = parametro.getModulo();
        this.tipoDado = parametro.getTipoDado();
        this.valor = parametro.getValor();
        this.valorDefault = parametro.getValorDefault();
        this.orfa = parametro.isOrfa();
    }
}
