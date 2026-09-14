package br.com.diegocordeiro.dscproject.dto.minhaconta;

import br.com.diegocordeiro.dscproject.model.conta.Conta;
import br.com.diegocordeiro.dscproject.enums.Genero;
import br.com.diegocordeiro.dscproject.model.usuario.Usuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * Formulário da tela self-service Configurações da Conta.
 * Não tem id nem perfil: o usuário mexe só no próprio registro e nunca no próprio perfil.
 * {@code senha} e {@code confirmacaoSenha} são opcionais — vazias mantêm a senha atual.
 */
@Getter
@Setter
@NoArgsConstructor
public class MinhaContaDTO {

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

    public MinhaContaDTO(Usuario usuario) {
        this.nome = usuario.getNome();
        this.genero = usuario.getGenero();
        this.nascimento = usuario.getNascimento();
        this.email = usuario.getEmail();
        this.login = usuario.getLogin();
    }

    public boolean senhaInformada() {
        return senha != null && !senha.isBlank();
    }

    public boolean senhasConferem() {
        return senha != null && senha.equals(confirmacaoSenha);
    }
}
