package br.com.diegocordeiro.dscproject.config.permissao;

/**
 * Catálogo das permissões do módulo Open Finance (Conectar Conta).
 * Cada item vira a autoridade {@code PERM_{CODIGO}}.
 */
public enum PermissaoOpenFinanceCatalogo implements PermissaoDefinida {

    OPEN_FINANCE_LISTAR("Listar Conexões Open Finance", "Permite visualizar a tela de conexões, acompanhar status e consultar contas vinculadas."),
    OPEN_FINANCE_CONECTAR("Conectar Novas Instituições", "Permite inicializar o widget Pluggy Connect, concluir novas conexões e mapear contas externas."),
    OPEN_FINANCE_SINCRONIZAR("Sincronizar Conexões Manualmente", "Permite disparar sincronizações manuais sob demanda para atualização de saldos e transações."),
    OPEN_FINANCE_DESCONECTAR("Desconectar Instituições", "Permite revogar consentimentos e desconectar instituições financeiras da conta."),
    OPEN_FINANCE_CONFIGURAR_CREDENCIAL("Configurar Credenciais BYOK", "Permite cadastrar, testar e atualizar as chaves de API do usuário no provedor.");

    public static final String MODULO = "Open Finance";

    private final String nome;
    private final String descricao;

    PermissaoOpenFinanceCatalogo(String nome, String descricao) {
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
