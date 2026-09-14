package br.com.diegocordeiro.dscproject.model.usuario;

import br.com.diegocordeiro.dscproject.model.comum.AbstractAuditoria;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;

import java.time.Instant;

/**
 * Tabela auxiliar do fluxo de recuperação de senha.
 * Guarda apenas o hash de um token de uso único, com validade e limite de tentativas.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Audited
@Table(
    name = "USUARIOS_RECUPERACAO_SENHA",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_usuarios_recuperacao_senha_token",
        columnNames = "URSE_TOKEN_HASH"
    )
)
public class UsuarioRecuperacaoSenha extends AbstractAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "URSE_ID")
    private Long id;

    @Column(name = "URSE_TOKEN_HASH", length = 255, nullable = false, unique = true)
    private String tokenHash;

    @Column(name = "URSE_EXPIRA_EM", nullable = false)
    private Instant expiraEm;

    @Column(name = "URSE_TENTATIVAS", nullable = false)
    private short tentativas = 0;

    @Column(name = "URSE_FL_UTILIZADO", nullable = false)
    private boolean utilizado = false;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "USU_ID", nullable = false)
    private Usuario usuario;

    public boolean expirado(Instant agora) {
        return expiraEm.isBefore(agora);
    }

    public void registrarTentativa() {
        this.tentativas++;
    }
}
