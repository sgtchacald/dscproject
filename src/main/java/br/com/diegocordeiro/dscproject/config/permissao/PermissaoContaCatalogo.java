package br.com.diegocordeiro.dscproject.config.permissao;

/**
 * Catálogo, no código, das permissões do módulo Contas.
 * Convenção domínio-primeiro; cada uma vira a autoridade {@code PERM_{CODIGO}}.
 */
public enum PermissaoContaCatalogo implements PermissaoDefinida {

    CONTAS_LISTAR("Listar minhas contas", "Abrir a tela Minhas Contas, listar e filtrar as próprias contas, e obter a lista de contas ativas para os comboboxes de lançamento. Controla a visibilidade do menu 'Minhas Contas'."),
    CONTAS_INSERIR("Cadastrar contas", "Cadastrar novas contas bancárias ou carteiras."),
    CONTAS_EDITAR("Editar contas", "Alterar dados cadastrais de contas existentes."),
    CONTAS_EXCLUIR("Excluir contas", "Excluir logicamente contas que não possuem movimentações associadas."),
    CONTAS_DESATIVAR("Desativar contas", "Desativar contas ativas impedindo novos lançamentos."),
    CONTAS_ATIVAR("Ativar contas", "Reativar contas inativas permitindo novos lançamentos."),
    CONTAS_AJUSTAR_SALDO("Ajustar saldo de contas", "Realizar ajuste manual do saldo com registro de motivo na auditoria.");

    public static final String MODULO = "Contas";

    private final String nome;
    private final String descricao;

    PermissaoContaCatalogo(String nome, String descricao) {
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
