package br.com.diegocordeiro.dscproject.model.usuario;

import br.com.diegocordeiro.dscproject.model.comum.AbstractAuditoria;
import br.com.diegocordeiro.dscproject.model.perfil.Perfil;
import br.com.diegocordeiro.dscproject.model.perfil.PerfilPermissao;
import br.com.diegocordeiro.dscproject.enums.Genero;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Audited
@Table(name = "USUARIOS")
public class Usuario extends AbstractAuditoria implements UserDetails {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "USU_ID", nullable = false)
    private Long id;

    @Column(name = "USU_NOME", length = 100, nullable = false)
    private String nome;

    @Column(name = "USU_GENERO", length = 1, nullable = false)
    private Genero genero;

    @Column(name = "USU_DT_NASCIMENTO")
    private LocalDate nascimento;

    @Column(name = "USU_EMAIL", length = 512, nullable = false, unique = true)
    private String email;

    @Column(name = "USU_LOGIN", length = 40, nullable = false, unique = true)
    private String login;

    @Column(name = "USU_SENHA", length = 1024, nullable = false)
    private String senha;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "PERF_ID", nullable = false)
    private Perfil perfil;

    /**
     * Autoridades resolvidas do perfil: {@code ROLE_{PERF_CODIGO}} mais
     * {@code PERM_{CODIGO}} de cada permissão vinculada ao perfil.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> authorities = new HashSet<>();
        if (perfil != null) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + perfil.getCodigo()));
            for (PerfilPermissao vinculo : perfil.getVinculosPermissao()) {
                if (!vinculo.isExcluido() && vinculo.getPermissao() != null) {
                    authorities.add(new SimpleGrantedAuthority("PERM_" + vinculo.getPermissao().getCodigo()));
                }
            }
        }
        return authorities;
    }

    @Override
    public String getPassword() {
        return this.senha;
    }

    @Override
    public String getUsername() {
        return this.login;
    }

    /** Usuário excluído logicamente não autentica. */
    @Override
    public boolean isEnabled() {
        return !isExcluido();
    }
}
