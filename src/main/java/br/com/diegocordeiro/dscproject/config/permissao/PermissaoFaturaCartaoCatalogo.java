package br.com.diegocordeiro.dscproject.config.permissao;

public enum PermissaoFaturaCartaoCatalogo implements PermissaoDefinida {

    FATURAS_LISTAR("Listar faturas de cartão", "Abrir a tela Faturas de Cartão, listar faturas e consultar detalhes de compras do ciclo. Controla a visibilidade do menu 'Faturas de Cartão'."),
    FATURAS_FECHAR("Fechar fatura de cartão", "Realizar o fechamento formal de faturas em aberto e congelar valores com encargos."),
    FATURAS_REABRIR("Reabrir fatura de cartão", "Reabrir faturas fechadas sem pagamento para inclusão ou retificação de compras."),
    FATURAS_PAGAR("Registrar pagamento de fatura", "Registrar a quitação total ou parcial de faturas fechadas debitando em conta bancária."),
    FATURAS_EDITAR("Editar encargos da fatura", "Ajustar encargos e valores mínimos de faturas fechadas.");

    public static final String MODULO = "Faturas de Cartão";

    private final String nome;
    private final String descricao;
    private final boolean concedivelPorPlano;

    PermissaoFaturaCartaoCatalogo(String nome, String descricao) {
        this(nome, descricao, false);
    }

    PermissaoFaturaCartaoCatalogo(String nome, String descricao, boolean concedivelPorPlano) {
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
