package br.com.diegocordeiro.dscproject.model.parametro;

import br.com.diegocordeiro.dscproject.model.comum.AbstractAuditoria;
import br.com.diegocordeiro.dscproject.config.catalogo.LinhaCatalogo;
import br.com.diegocordeiro.dscproject.enums.TipoParametro;
import br.com.diegocordeiro.dscproject.config.parametro.ParametroDefinido;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;

/**
 * Parâmetro global de configuração — contrato lido em runtime por
 * {@code buscarValorPorCodigo(codigo)}. Tabela-raiz, sem FK. Código, nome,
 * descrição, módulo e tipo vêm do catálogo do código e são somente-leitura na
 * tela; a tela edita apenas {@code valor} e {@code motivo}. Auditada via Envers.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Audited
@Table(name = "PARAMETROS_GLOBAIS")
public class ParametroGlobal extends AbstractAuditoria implements LinhaCatalogo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PAGL_ID")
    private Long id;

    @Column(name = "PAGL_CODIGO", length = 50, nullable = false, unique = true)
    private String codigo;

    @Column(name = "PAGL_NOME", length = 200, nullable = false)
    private String nome;

    @Column(name = "PAGL_DESCRICAO", length = 512, nullable = false)
    private String descricao;

    @Column(name = "PAGL_MODULO", length = 200, nullable = false)
    private String modulo;

    @Enumerated(EnumType.STRING)
    @Column(name = "PAGL_TIPO_DADO", length = 15, nullable = false)
    private TipoParametro tipoDado;

    @Column(name = "PAGL_VALOR", columnDefinition = "TEXT", nullable = false)
    private String valor;

    @Column(name = "PAGL_VALOR_DEFAULT", columnDefinition = "TEXT", nullable = false)
    private String valorDefault;

    @Column(name = "PAGL_MOTIVO", length = 255)
    private String motivo;

    @Column(name = "PAGL_FL_ORFA", nullable = false)
    private boolean orfa = false;

    /** Semente do catálogo do código: valor corrente nasce igual ao default. */
    public ParametroGlobal(ParametroDefinido definicao) {
        this.codigo = definicao.getCodigo();
        this.nome = definicao.getNome();
        this.descricao = definicao.getDescricao();
        this.modulo = definicao.getModulo();
        this.tipoDado = definicao.getTipo();
        this.valorDefault = definicao.getValorDefault();
        this.valor = definicao.getValorDefault();
    }
}
