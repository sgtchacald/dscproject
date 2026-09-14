package br.com.diegocordeiro.dscproject.model.categoria;

import br.com.diegocordeiro.dscproject.model.comum.AbstractAuditoria;
import br.com.diegocordeiro.dscproject.enums.AplicaA;
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
 * Categoria financeira para classificação de receitas e despesas.
 * Possui suporte a categorias padrão do sistema e personalizadas.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Audited
@Table(name = "CATEGORIAS")
public class Categoria extends AbstractAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CATE_ID")
    private Long id;

    @Column(name = "CATE_CODIGO", length = 40, nullable = false, unique = true)
    private String codigo;

    @Column(name = "CATE_NOME", length = 100, nullable = false)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(name = "CATE_APLICA_A", length = 10, nullable = false)
    private AplicaA aplicaA;

    @Column(name = "CATE_COR", length = 7)
    private String cor;

    @Column(name = "CATE_ICONE", length = 40)
    private String icone;

    @Column(name = "CATE_FL_ATIVO", nullable = false)
    private boolean ativo = true;

    @Column(name = "CATE_FL_SISTEMA", nullable = false)
    private boolean sistema = false;
}
