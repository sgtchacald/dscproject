package br.com.diegocordeiro.dscproject.model.perfil;

import br.com.diegocordeiro.dscproject.model.comum.AbstractAuditoria;
import br.com.diegocordeiro.dscproject.config.catalogo.LinhaCatalogo;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;

/**
 * Capacidade granular verificável em tela e serviço. Código domínio-primeiro
 * (ex.: {@code USUARIOS_LISTAR}); vira a authority {@code PERM_{CODIGO}}.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Audited
@Table(name = "PERMISSOES")
public class Permissao extends AbstractAuditoria implements LinhaCatalogo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PERM_ID")
    private Long id;

    @Column(name = "PERM_CODIGO", length = 60, nullable = false, unique = true)
    private String codigo;

    @Column(name = "PERM_NOME", length = 100, nullable = false)
    private String nome;

    @Column(name = "PERM_DESCRICAO", length = 255)
    private String descricao;

    @Column(name = "PERM_MODULO", length = 40, nullable = false)
    private String modulo;

    @Column(name = "PERM_FL_CONCEDIVEL_POR_PLANO", nullable = false)
    private boolean concedivelPorPlano = false;

    @Column(name = "PERM_FL_ORFA", nullable = false)
    private boolean orfa = false;

    public Permissao(String codigo, String nome, String descricao, String modulo) {
        this.codigo = codigo;
        this.nome = nome;
        this.descricao = descricao;
        this.modulo = modulo;
    }
}
