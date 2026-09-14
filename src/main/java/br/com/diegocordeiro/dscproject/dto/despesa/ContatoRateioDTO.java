package br.com.diegocordeiro.dscproject.dto.despesa;

import br.com.diegocordeiro.dscproject.enums.TipoContato;
import br.com.diegocordeiro.dscproject.model.contato.Contato;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ContatoRateioDTO {

    private Long id;
    private String nome;
    private String email;
    private String telefone;
    private TipoContato tipo;
    private String chavePix;

    public ContatoRateioDTO(Contato c) {
        this.id = c.getId();
        this.nome = c.getNome();
        this.email = c.getEmail();
        this.telefone = c.getTelefone();
        this.tipo = c.getTipo();
        this.chavePix = c.getChavePix();
    }
}
