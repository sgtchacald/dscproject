package br.com.diegocordeiro.dscproject.config.runner;

import br.com.diegocordeiro.dscproject.enums.AplicaA;
import br.com.diegocordeiro.dscproject.model.categoria.Categoria;
import br.com.diegocordeiro.dscproject.model.instituicaofinanceira.OpfiProvedor;
import br.com.diegocordeiro.dscproject.repository.categoria.CategoriaRepository;
import br.com.diegocordeiro.dscproject.repository.instituicaofinanceira.OpfiProvedorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@Order(25)
public class CategoriaCargaInicialRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(CategoriaCargaInicialRunner.class);

    private final CategoriaRepository categoriaRepository;
    private final OpfiProvedorRepository opfiProvedorRepository;

    public CategoriaCargaInicialRunner(CategoriaRepository categoriaRepository, OpfiProvedorRepository opfiProvedorRepository) {
        this.categoriaRepository = categoriaRepository;
        this.opfiProvedorRepository = opfiProvedorRepository;
    }

    private record CategoriaInicial(String codigo, String nome, AplicaA aplicaA) {}

    private static final List<CategoriaInicial> CATEGORIAS_SISTEMA = List.of(
        new CategoriaInicial("SALARIO", "Salário", AplicaA.RECEITA),
        new CategoriaInicial("SALARIO_DECIMO_TERCEIRO", "13º Salário", AplicaA.RECEITA),
        new CategoriaInicial("EXTRA", "Renda Extra", AplicaA.RECEITA),
        new CategoriaInicial("FERIAS", "Férias", AplicaA.RECEITA),
        new CategoriaInicial("INVESTIMENTO", "Investimento", AplicaA.RECEITA),
        new CategoriaInicial("MORADIA", "Moradia", AplicaA.DESPESA),
        new CategoriaInicial("ALIMENTACAO", "Alimentação", AplicaA.DESPESA),
        new CategoriaInicial("LAZER", "Lazer", AplicaA.DESPESA),
        new CategoriaInicial("VESTUARIO", "Vestuário", AplicaA.DESPESA),
        new CategoriaInicial("TRANSPORTE", "Transporte", AplicaA.DESPESA),
        new CategoriaInicial("CARRO", "Carro", AplicaA.DESPESA),
        new CategoriaInicial("SAUDE", "Saúde", AplicaA.DESPESA),
        new CategoriaInicial("EDUCACAO", "Educação", AplicaA.DESPESA),
        new CategoriaInicial("SERVICOS", "Serviços", AplicaA.DESPESA),
        new CategoriaInicial("EMPRESTIMOS", "Empréstimos", AplicaA.DESPESA),
        new CategoriaInicial("CARTAO_DE_CREDITO", "Cartão de Crédito", AplicaA.DESPESA),
        new CategoriaInicial("TAXAS_EMPRESA", "Taxas PJ", AplicaA.DESPESA),
        new CategoriaInicial("EMPRESA", "Empresa", AplicaA.DESPESA),
        new CategoriaInicial("OUTRO", "Outro", AplicaA.AMBOS)
    );

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        carregarCategoriasSistema();
        carregarProvedoresPadrao();
    }

    private void carregarCategoriasSistema() {
        int inseridas = 0;
        for (CategoriaInicial ci : CATEGORIAS_SISTEMA) {
            if (categoriaRepository.findByCodigoAndDataExclusaoIsNull(ci.codigo()).isEmpty()) {
                Categoria c = new Categoria();
                c.setCodigo(ci.codigo());
                c.setNome(ci.nome());
                c.setAplicaA(ci.aplicaA());
                c.setAtivo(true);
                c.setSistema(true);
                categoriaRepository.save(c);
                inseridas++;
            }
        }
        if (inseridas > 0) {
            log.info("Carga inicial de categorias executada: {} categorias de sistema inseridas.", inseridas);
        }
    }

    private void carregarProvedoresPadrao() {
        if (opfiProvedorRepository.findByCodigo("PLUGGY").isEmpty()) {
            OpfiProvedor pluggy = new OpfiProvedor();
            pluggy.setCodigo("PLUGGY");
            pluggy.setNome("Pluggy");
            pluggy.setUrlBase("https://api.pluggy.ai");
            pluggy.setSuportaWebhook(false);
            pluggy.setAtivo(true);
            opfiProvedorRepository.save(pluggy);
            log.info("Carga inicial de provedores Open Finance executada: provedor PLUGGY inserido.");
        }
    }
}
