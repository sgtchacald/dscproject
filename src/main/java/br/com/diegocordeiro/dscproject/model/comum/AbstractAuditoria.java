package br.com.diegocordeiro.dscproject.model.comum;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.Instant;

/**
 * Superclasse de auditoria portada da geração 1 (prefixo {@code audit_}),
 * com os dois campos de exclusão lógica (soft delete) acrescentados.
 * Herdada por toda entidade de dados. Auditada via Hibernate Envers.
 */
@Getter
@Setter
@MappedSuperclass
@Audited
@EntityListeners(AuditingEntityListener.class)
public abstract class AbstractAuditoria implements Serializable {

    @JsonIgnore
    @CreatedDate
    @Column(name = "audit_data_criacao", nullable = false, updatable = false)
    private Instant dataCriacao = Instant.now();

    @JsonIgnore
    @CreatedBy
    @Column(name = "audit_criado_por", length = 400, nullable = false, updatable = false)
    private String criadoPor;

    @JsonIgnore
    @LastModifiedDate
    @Column(name = "audit_data_alteracao")
    private Instant dataAlteracao = Instant.now();

    @JsonIgnore
    @LastModifiedBy
    @Column(name = "audit_alterado_por", length = 400)
    private String alteradoPor;

    @JsonIgnore
    @Column(name = "audit_data_exclusao")
    private Instant dataExclusao;

    @JsonIgnore
    @Column(name = "audit_excluido_por", length = 400)
    private String excluidoPor;

    public boolean isExcluido() {
        return dataExclusao != null;
    }
}
