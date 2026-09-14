package br.com.diegocordeiro.dscproject.support;

import br.com.diegocordeiro.dscproject.enums.Genero;
import br.com.diegocordeiro.dscproject.model.perfil.Perfil;
import br.com.diegocordeiro.dscproject.model.perfil.PerfilPermissao;
import br.com.diegocordeiro.dscproject.model.perfil.Permissao;
import br.com.diegocordeiro.dscproject.model.usuario.Usuario;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/** Objetos de domínio prontos para os testes de unidade / fatia. */
public final class TestFixtures {

    private TestFixtures() {
    }

    public static Perfil perfil(String codigo, String... codigosPermissao) {
        Perfil perfil = new Perfil(codigo, codigo, null, true);
        Set<PerfilPermissao> vinculos = new HashSet<>();
        for (String codigoPermissao : codigosPermissao) {
            Permissao permissao = new Permissao(codigoPermissao, codigoPermissao, null, "Usuários");
            vinculos.add(new PerfilPermissao(perfil, permissao));
        }
        perfil.setVinculosPermissao(vinculos);
        return perfil;
    }

    public static Usuario usuario(Long id, String login, Perfil perfil) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setNome("Fulano de Tal");
        usuario.setGenero(Genero.OUTRO);
        usuario.setNascimento(LocalDate.of(1990, 1, 1));
        usuario.setEmail(login + "@test.com");
        usuario.setLogin(login);
        usuario.setSenha("$2a$10$hashExistente");
        usuario.setPerfil(perfil);
        return usuario;
    }
}
