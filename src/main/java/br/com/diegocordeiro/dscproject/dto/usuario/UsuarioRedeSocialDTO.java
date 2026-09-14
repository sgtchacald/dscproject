package br.com.diegocordeiro.dscproject.dto.usuario;

import br.com.diegocordeiro.dscproject.enums.TipoRedeSocial;
import br.com.diegocordeiro.dscproject.model.usuario.UsuarioRedeSocial;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioRedeSocialDTO {
    private Long id;
    private TipoRedeSocial tipo;
    private String tipoDescricao;
    private String tipoIcone;
    private String url;
    private String identificador;
    private boolean ativo;

    public static UsuarioRedeSocialDTO fromEntity(UsuarioRedeSocial entity) {
        if (entity == null) return null;
        return UsuarioRedeSocialDTO.builder()
                .id(entity.getId())
                .tipo(entity.getTipo())
                .tipoDescricao(entity.getTipo() != null ? entity.getTipo().getDescricao() : null)
                .tipoIcone(entity.getTipo() != null ? entity.getTipo().getIcone() : null)
                .url(entity.getUrl())
                .identificador(entity.getIdentificador())
                .ativo(entity.isAtivo())
                .build();
    }
}
