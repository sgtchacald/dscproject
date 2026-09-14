package br.com.diegocordeiro.dscproject.dto.usuario;

import br.com.diegocordeiro.dscproject.web.sistema.validator.usuario.UsuarioValidator;
import br.com.diegocordeiro.dscproject.enums.Genero;
import br.com.diegocordeiro.dscproject.model.usuario.Usuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * Formulário de cadastro / edição / auto-cadastro.
 * Em edição, {@code senha} e {@code confirmacaoSenha} são opcionais —
 * a obrigatoriedade na criação é aplicada pelo {@code UsuarioValidator}.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioDTO {

    private Long id;

    @NotBlank(message = "{usuario.nome.obrigatorio}")
    @Size(max = 100, message = "{usuario.nome.tamanho}")
    private String nome;

    @NotNull(message = "{usuario.genero.obrigatorio}")
    private Genero genero;

    @NotNull(message = "{usuario.nascimento.obrigatoria}")
    @PastOrPresent(message = "{usuario.nascimento.futura}")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate nascimento;

    @NotBlank(message = "{usuario.email.obrigatorio}")
    @Email(message = "{usuario.email.invalido}")
    @Size(max = 512, message = "{usuario.email.tamanho}")
    private String email;

    @NotBlank(message = "{usuario.login.obrigatorio}")
    @Size(min = 4, max = 40, message = "{usuario.login.tamanho}")
    private String login;

    @Size(min = 6, message = "{usuario.senha.tamanho}")
    private String senha;

    private String confirmacaoSenha;

    /** Código do perfil (ADMIN / USER). Ignorado no auto-cadastro. */
    private String perfilCodigo;

    public UsuarioDTO(Usuario usuario) {
        this.id = usuario.getId();
        this.nome = usuario.getNome();
        this.genero = usuario.getGenero();
        this.nascimento = usuario.getNascimento();
        this.email = usuario.getEmail();
        this.login = usuario.getLogin();
        this.perfilCodigo = usuario.getPerfil() != null ? usuario.getPerfil().getCodigo() : null;
    }

    public boolean isEdicao() {
        return id != null;
    }

    public boolean senhaInformada() {
        return senha != null && !senha.isBlank();
    }
}
