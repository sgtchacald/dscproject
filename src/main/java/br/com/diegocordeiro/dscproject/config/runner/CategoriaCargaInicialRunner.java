package br.com.diegocordeiro.dscproject.config.runner;

import br.com.diegocordeiro.dscproject.enums.AplicaA;
import br.com.diegocordeiro.dscproject.model.categoria.Categoria;
import br.com.diegocordeiro.dscproject.model.instituicaofinanceira.OpenFinanceProvedor;
import br.com.diegocordeiro.dscproject.repository.categoria.CategoriaRepository;
import br.com.diegocordeiro.dscproject.repository.instituicaofinanceira.OpenFinanceProvedorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
@Order(25)
public class CategoriaCargaInicialRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(CategoriaCargaInicialRunner.class);

    private final CategoriaRepository categoriaRepository;
    private final OpenFinanceProvedorRepository openFinanceProvedorRepository;

    public CategoriaCargaInicialRunner(CategoriaRepository categoriaRepository, OpenFinanceProvedorRepository openFinanceProvedorRepository) {
        this.categoriaRepository = categoriaRepository;
        this.openFinanceProvedorRepository = openFinanceProvedorRepository;
    }

    private record CategoriaInicial(String codigo, String nome, AplicaA aplicaA, String cor, String icone) {}

    private static final List<CategoriaInicial> CATEGORIAS_SISTEMA = List.of(
        new CategoriaInicial("SALARIO", "Salário", AplicaA.RECEITA, "#00c853", "money"),
        new CategoriaInicial("SALARIO_DECIMO_TERCEIRO", "13º Salário", AplicaA.RECEITA, "#ff9100", "gift"),
        new CategoriaInicial("EXTRA", "Renda Extra", AplicaA.RECEITA, "#64dd17", "hand-coins"),
        new CategoriaInicial("FERIAS", "Férias", AplicaA.RECEITA, "#ffea00", "sun"),
        new CategoriaInicial("INVESTIMENTO", "Investimento", AplicaA.RECEITA, "#ffc400", "trend-up"),
        new CategoriaInicial("MORADIA", "Moradia", AplicaA.DESPESA, "#a0522d", "home"),
        new CategoriaInicial("ALIMENTACAO", "Alimentação", AplicaA.DESPESA, "#ff3d00", "fork-knife"),
        new CategoriaInicial("LAZER", "Lazer", AplicaA.DESPESA, "#ff4081", "popcorn"),
        new CategoriaInicial("VESTUARIO", "Vestuário", AplicaA.DESPESA, "#d500f9", "t-shirt"),
        new CategoriaInicial("TRANSPORTE", "Transporte", AplicaA.DESPESA, "#00b0ff", "bus"),
        new CategoriaInicial("CARRO", "Carro", AplicaA.DESPESA, "#607d8b", "car"),
        new CategoriaInicial("SAUDE", "Saúde", AplicaA.DESPESA, "#d50000", "heartbeat"),
        new CategoriaInicial("EDUCACAO", "Educação", AplicaA.DESPESA, "#6200ea", "graduation-cap"),
        new CategoriaInicial("SERVICOS", "Serviços", AplicaA.DESPESA, "#00e5ff", "wrench"),
        new CategoriaInicial("EMPRESTIMOS", "Empréstimos", AplicaA.DESPESA, "#c51162", "bank"),
        new CategoriaInicial("CARTAO_DE_CREDITO", "Cartão de Crédito", AplicaA.DESPESA, "#212121", "credit-card"),
        new CategoriaInicial("TAXAS_EMPRESA", "Taxas PJ", AplicaA.DESPESA, "#00bfa5", "receipt"),
        new CategoriaInicial("EMPRESA", "Empresa", AplicaA.DESPESA, "#304ffe", "buildings"),
        new CategoriaInicial("OUTRO", "Outro", AplicaA.AMBOS, "#bdbdbd", "dots-three-circle")
    );

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        carregarCategoriasSistema();
        carregarProvedoresPadrao();
    }

    private void carregarCategoriasSistema() {
        int inseridas = 0;
        int completadas = 0;
        for (CategoriaInicial ci : CATEGORIAS_SISTEMA) {
            Optional<Categoria> existente = categoriaRepository.findByCodigoAndDataExclusaoIsNull(ci.codigo());
            if (existente.isEmpty()) {
                Categoria c = new Categoria();
                c.setCodigo(ci.codigo());
                c.setNome(ci.nome());
                c.setAplicaA(ci.aplicaA());
                c.setCor(ci.cor());
                c.setIcone(ci.icone());
                c.setAtivo(true);
                c.setSistema(true);
                categoriaRepository.save(c);
                inseridas++;
            } else if (completarCorEIcone(existente.get(), ci)) {
                completadas++;
            }
        }
        if (inseridas > 0) {
            log.info("Carga inicial de categorias executada: {} categorias de sistema inseridas.", inseridas);
        }
        if (completadas > 0) {
            log.info("Carga inicial de categorias: cor/ícone preenchidos em {} categorias de sistema.", completadas);
        }
    }

    // Bases criadas antes de as categorias de sistema terem cor e ícone; só
    // preenche o que está vazio para não sobrescrever escolha do usuário.
    private boolean completarCorEIcone(Categoria c, CategoriaInicial ci) {
        boolean alterou = false;
        if (c.getCor() == null || c.getCor().isBlank()) {
            c.setCor(ci.cor());
            alterou = true;
        }
        if (c.getIcone() == null || c.getIcone().isBlank()) {
            c.setIcone(ci.icone());
            alterou = true;
        }
        if (alterou) {
            categoriaRepository.save(c);
        }
        return alterou;
    }

    private void carregarProvedoresPadrao() {
        if (openFinanceProvedorRepository.findByCodigo("PLUGGY").isEmpty()) {
            OpenFinanceProvedor pluggy = new OpenFinanceProvedor();
            pluggy.setCodigo("PLUGGY");
            pluggy.setNome("Pluggy");
            pluggy.setUrlBase("https://api.pluggy.ai");
            pluggy.setSuportaWebhook(false);
            pluggy.setAtivo(true);
            openFinanceProvedorRepository.save(pluggy);
            log.info("Carga inicial de provedores Open Finance executada: provedor PLUGGY inserido.");
        }
    }
}
