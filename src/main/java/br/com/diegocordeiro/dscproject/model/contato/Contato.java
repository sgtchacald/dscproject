package br.com.diegocordeiro.dscproject.model.contato;

import br.com.diegocordeiro.dscproject.model.comum.AbstractAuditoria;
import br.com.diegocordeiro.dscproject.model.usuario.Usuario;
import br.com.diegocordeiro.dscproject.enums.StatusContato;
import br.com.diegocordeiro.dscproject.enums.TipoContato;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

/**
 * Contato pertencente à agenda privada do usuário ou conexão entre usuários do sistema.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Audited
@Table(name = "CONTATOS")
public class Contato extends AbstractAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CONT_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USU_ID_DONO", nullable = false)
    private Usuario usuarioDono;

    @Enumerated(EnumType.STRING)
    @Column(name = "CONT_TIPO", length = 20, nullable = false)
    private TipoContato tipo = TipoContato.EXTERNO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USU_ID_CONECTADO")
    private Usuario usuarioConectado;

    @Column(name = "CONT_NOME", length = 150, nullable = false)
    private String nome;

    @Column(name = "CONT_EMAIL", length = 100)
    private String email;

    @Column(name = "CONT_TELEFONE", length = 20)
    private String telefone;

    @Column(name = "CONT_CHAVE_PIX", length = 100)
    private String chavePix;

    @Enumerated(EnumType.STRING)
    @Column(name = "CONT_STATUS", length = 20, nullable = false)
    private StatusContato status = StatusContato.ATIVO;

    public boolean isAtivo() {
        return status == StatusContato.ATIVO && getDataExclusao() == null;
    }
}
