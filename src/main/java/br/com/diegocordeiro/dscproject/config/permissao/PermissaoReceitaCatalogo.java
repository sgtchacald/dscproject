package br.com.diegocordeiro.dscproject.config.permissao;

/**
 * Catálogo, no código, das permissões do módulo Receitas.
 * Convenção domínio-primeiro; cada uma vira a autoridade {@code PERM_{CODIGO}}.
 */
public enum PermissaoReceitaCatalogo implements PermissaoDefinida {

    RECEITAS_LISTAR("Listar minhas receitas", "Abrir a tela Receitas, listar e filtrar as próprias receitas. Controla a visibilidade do menu 'Receitas'."),
    RECEITAS_INSERIR("Cadastrar receitas", "Cadastrar novas receitas e duplicar receitas para si próprio."),
    RECEITAS_EDITAR("Editar receitas", "Alterar dados de receitas existentes do próprio usuário."),
    RECEITAS_EXCLUIR("Excluir receitas", "Excluir logicamente receitas manuais do próprio usuário."),
    RECEITAS_REGISTRAR_RECEBIMENTO("Registrar recebimento de receitas", "Registrar o recebimento de receitas previstas.");

    public static final String MODULO = "Receitas";

    private final String nome;
    private final String descricao;

    PermissaoReceitaCatalogo(String nome, String descricao) {
        this.nome = nome;
        this.descricao = descricao;
    }

    @Override
    public String getCodigo() {
        return name();
    }

    @Override
    public String getNome() {
        return nome;
    }

    @Override
    public String getDescricao() {
        return descricao;
    }

    @Override
    public String getModulo() {
        return MODULO;
    }
}
