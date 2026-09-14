package br.com.diegocordeiro.dscproject.dto.parametro;

import br.com.diegocordeiro.dscproject.model.parametro.ParametroGlobal;
import lombok.Getter;
import org.springframework.data.history.Revision;
import org.springframework.data.history.RevisionMetadata;

import java.time.Instant;

/** Uma revisão do histórico de um parâmetro global (Hibernate Envers). */
@Getter
public class RevisaoParametroDTO {

    private final Integer revisao;
    private final String tipo;
    private final Instant dataHora;
    private final String autor;
    private final String valor;
    private final String motivo;

    public RevisaoParametroDTO(Revision<Integer, ParametroGlobal> revision) {
        this.revisao = revision.getRequiredRevisionNumber();
        this.tipo = traduzirTipo(revision.getMetadata().getRevisionType());
        this.dataHora = revision.getMetadata().getRequiredRevisionInstant();
        ParametroGlobal snapshot = revision.getEntity();
        this.autor = snapshot.getAlteradoPor() != null ? snapshot.getAlteradoPor() : snapshot.getCriadoPor();
        this.valor = snapshot.getValor();
        this.motivo = snapshot.getMotivo();
    }

    private static String traduzirTipo(RevisionMetadata.RevisionType tipo) {
        return switch (tipo) {
            case INSERT -> "Criação";
            case UPDATE -> "Alteração";
            case DELETE -> "Exclusão";
            default -> "Desconhecido";
        };
    }
}
