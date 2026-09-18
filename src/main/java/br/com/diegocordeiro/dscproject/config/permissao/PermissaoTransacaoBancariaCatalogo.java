package br.com.diegocordeiro.dscproject.config.permissao;

public enum PermissaoTransacaoBancariaCatalogo implements PermissaoDefinida {

    TRANSACOES_LISTAR("Listar transações bancárias", "Abrir a tela Transações Bancárias, listar e filtrar as próprias movimentações bancárias. Controla a visibilidade do menu 'Transações Bancárias'."),
    TRANSACOES_INSERIR("Cadastrar transações bancárias", "Cadastrar novas transações bancárias manuais e duplicar lançamentos existentes."),
    TRANSACOES_EDITAR("Editar transações bancárias", "Editar dados de transações bancárias manuais existentes."),
    TRANSACOES_EXCLUIR("Excluir transações bancárias", "Excluir logicamente transações bancárias manuais do próprio usuário."),
    TRANSACOES_IMPORTAR("Importar extratos bancários OFX", "Importar extratos bancários em formato OFX para as contas do próprio usuário.");

    public static final String MODULO = "Transações Bancárias";

    private final String nome;
    private final String descricao;
    private final boolean concedivelPorPlano;

    PermissaoTransacaoBancariaCatalogo(String nome, String descricao) {
        this(nome, descricao, false);
    }

    PermissaoTransacaoBancariaCatalogo(String nome, String descricao, boolean concedivelPorPlano) {
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
