package br.com.diegocordeiro.dscproject.dto.usuario;

import br.com.diegocordeiro.dscproject.enums.TipoRedeSocial;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class UsuarioRedeSocialFormDTO {
    private Long id;

    @NotNull(message = "{msg.validacao.campo.obrigatorio}")
    private TipoRedeSocial tipo;

    @NotBlank(message = "{msg.validacao.campo.obrigatorio}")
    private String url;

    private String identificador;

    @Builder.Default
    private boolean ativo = true;
}
