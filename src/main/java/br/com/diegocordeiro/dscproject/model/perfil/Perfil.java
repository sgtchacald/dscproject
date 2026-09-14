package br.com.diegocordeiro.dscproject.model.perfil;

import br.com.diegocordeiro.dscproject.model.comum.AbstractAuditoria;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Perfil de acesso do usuário. Substitui o enum {@code Perfis} da geração 1.
 * O {@code codigo} vira a authority {@code ROLE_{CODIGO}}.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Audited
@Table(name = "PERFIS")
public class Perfil extends AbstractAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PERF_ID")
    private Long id;

    @Column(name = "PERF_CODIGO", length = 30, nullable = false, unique = true)
    private String codigo;

    @Column(name = "PERF_NOME", length = 100, nullable = false)
    private String nome;

    @Column(name = "PERF_DESCRICAO", length = 255)
    private String descricao;

    @Column(name = "PERF_FL_SISTEMA", nullable = false)
    private boolean sistema = false;

    @NotAudited
    @OneToMany(mappedBy = "perfil", fetch = FetchType.EAGER)
    private Set<PerfilPermissao> vinculosPermissao = new HashSet<>();

    public Perfil(String codigo, String nome, String descricao, boolean sistema) {
        this.codigo = codigo;
        this.nome = nome;
        this.descricao = descricao;
        this.sistema = sistema;
    }

    /** Permissões ativas vinculadas a este perfil. */
    public Set<Permissao> getPermissoes() {
        return vinculosPermissao.stream()
            .filter(vinculo -> !vinculo.isExcluido())
            .map(PerfilPermissao::getPermissao)
            .collect(Collectors.toSet());
    }
}
