package br.com.diegocordeiro.dscproject.config.permissao;

/**
 * Catálogo, no código, das permissões do módulo Usuários.
 * A carga inicial reflete estas entradas na tabela {@code PERMISSOES}.
 * Cada uma vira a autoridade {@code PERM_{CODIGO}}.
 */
public enum PermissaoUsuarioCatalogo implements PermissaoDefinida {

    USUARIOS_LISTAR("Listar usuários", "Abrir a tela de Usuários, listar e filtrar. Controla a visibilidade do menu 'Usuários'."),
    USUARIOS_INSERIR("Inserir usuário", "Cadastrar novo usuário, inclusive escolher o perfil."),
    USUARIOS_EDITAR("Editar usuário", "Editar usuário existente, inclusive trocar o perfil e a senha."),
    USUARIOS_EXCLUIR("Excluir usuário", "Exclusão lógica de usuário, respeitadas as travas."),
    USUARIOS_VER_HISTORICO("Ver histórico de usuário", "Ver o histórico de alterações de um usuário.");

    public static final String MODULO = "Usuários";

    private final String nome;
    private final String descricao;

    PermissaoUsuarioCatalogo(String nome, String descricao) {
        this.nome = nome;
        this.descricao = descricao;
    }

    public String getCodigo() {
        return name();
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getModulo() {
        return MODULO;
    }
}
