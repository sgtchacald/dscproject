package br.com.diegocordeiro.dscproject.config.runner;

import br.com.diegocordeiro.dscproject.enums.TipoInstituicaoFinanceira;
import br.com.diegocordeiro.dscproject.model.instituicaofinanceira.InstituicaoFinanceira;
import br.com.diegocordeiro.dscproject.repository.instituicaofinanceira.InstituicaoFinanceiraRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@Order(26)
public class InstituicaoFinanceiraCargaInicialRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(InstituicaoFinanceiraCargaInicialRunner.class);

    private final InstituicaoFinanceiraRepository instituicaoFinanceiraRepository;

    public InstituicaoFinanceiraCargaInicialRunner(InstituicaoFinanceiraRepository instituicaoFinanceiraRepository) {
        this.instituicaoFinanceiraRepository = instituicaoFinanceiraRepository;
    }

    private record InstituicaoInicial(String nome, String codigo, TipoInstituicaoFinanceira tipo) {}

    private static final List<InstituicaoInicial> INSTITUICOES_BASE = List.of(
        new InstituicaoInicial("Banco do Brasil", "001", TipoInstituicaoFinanceira.BANCO),
        new InstituicaoInicial("Caixa Econômica Federal", "104", TipoInstituicaoFinanceira.BANCO),
        new InstituicaoInicial("Bradesco", "237", TipoInstituicaoFinanceira.BANCO),
        new InstituicaoInicial("Itaú Unibanco", "341", TipoInstituicaoFinanceira.BANCO),
        new InstituicaoInicial("Santander", "033", TipoInstituicaoFinanceira.BANCO),
        new InstituicaoInicial("Nubank", "260", TipoInstituicaoFinanceira.BANCO),
        new InstituicaoInicial("Banco Inter", "077", TipoInstituicaoFinanceira.BANCO),
        new InstituicaoInicial("XP Investimentos", "102", TipoInstituicaoFinanceira.CORRETORA),
        new InstituicaoInicial("BTG Pactual", "208", TipoInstituicaoFinanceira.CORRETORA),
        new InstituicaoInicial("Rico Investimentos", "386", TipoInstituicaoFinanceira.CORRETORA),
        new InstituicaoInicial("Clear Corretora", "003", TipoInstituicaoFinanceira.CORRETORA)
    );

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        int inseridas = 0;
        for (InstituicaoInicial ini : INSTITUICOES_BASE) {
            if (instituicaoFinanceiraRepository.findByNomeIgnoreCaseAndDataExclusaoIsNull(ini.nome()).isEmpty()) {
                InstituicaoFinanceira infi = new InstituicaoFinanceira();
                infi.setNome(ini.nome());
                infi.setCodigo(ini.codigo());
                infi.setTipo(ini.tipo());
                infi.setAtivo(true);
                infi.setSistema(true);
                instituicaoFinanceiraRepository.save(infi);
                inseridas++;
            }
        }
        if (inseridas > 0) {
            log.info("Carga inicial de instituições financeiras executada: {} instituições de sistema inseridas.", inseridas);
        }
    }
}
