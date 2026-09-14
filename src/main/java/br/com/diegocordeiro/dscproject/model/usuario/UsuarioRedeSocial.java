package br.com.diegocordeiro.dscproject.model.usuario;

import br.com.diegocordeiro.dscproject.model.comum.AbstractAuditoria;
import br.com.diegocordeiro.dscproject.enums.TipoRedeSocial;
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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;

import java.io.Serial;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Audited
@Table(name = "USUARIOS_REDES_SOCIAIS")
public class UsuarioRedeSocial extends AbstractAuditoria {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "USRS_ID", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USU_ID", nullable = false)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(name = "USRS_TIPO", length = 30, nullable = false)
    private TipoRedeSocial tipo;

    @Column(name = "USRS_URL", length = 500, nullable = false)
    private String url;

    @Column(name = "USRS_IDENTIFICADOR", length = 100)
    private String identificador;

    @Builder.Default
    @Column(name = "USRS_FL_ATIVO", nullable = false)
    private boolean ativo = true;
}
