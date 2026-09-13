package br.com.diegocordeiro.dscproject.config.permissao;

public enum PermissaoDespesaCatalogo implements PermissaoDefinida {

    DESPESAS_LISTAR("Listar minhas despesas", "Abrir a tela Despesas, listar e filtrar as próprias despesas. Controla a visibilidade do menu 'Despesas'."),
    DESPESAS_INSERIR("Cadastrar despesas", "Cadastrar novas despesas à vista, parceladas, recorrentes e duplicar despesas existentes."),
    DESPESAS_EDITAR("Editar despesas", "Editar dados cadastrais de despesas existentes e atualizar valor inline no grid."),
    DESPESAS_EXCLUIR("Excluir despesas", "Excluir logicamente despesas manuais do próprio usuário (individuais, parceladas ou séries recorrentes)."),
    DESPESAS_PAGAR("Registrar pagamento de despesas", "Registrar e reverter o pagamento de despesas em aberto (individual ou em lote)."),
    DESPESAS_IMPORTAR("Importar faturas de cartão", "Importar faturas e extratos de cartão de crédito (Excel Itaú, Bradesco, C6 Bank e arquivos OFX)."),
    DESPESA_RATEAR_MULTIUSUARIO("Ratear despesas entre usuários", "Dividir despesas entre múltiplos usuários com gestão de fatias e acerto.");

    public static final String MODULO = "Despesas";

    private final String nome;
    private final String descricao;
    private final boolean concedivelPorPlano;

    PermissaoDespesaCatalogo(String nome, String descricao) {
        this(nome, descricao, false);
    }

    PermissaoDespesaCatalogo(String nome, String descricao, boolean concedivelPorPlano) {
        this.nome = nome;
        this.descricao = descricao;
        this.concedivelPorPlano = concedivelPorPlano;
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

    @Override
    public boolean isConcedivelPorPlano() {
        return concedivelPorPlano;
    }
}
