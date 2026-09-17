package br.com.diegocordeiro.dscproject.service.transacao;

import br.com.diegocordeiro.dscproject.dto.transacao.ResultadoImportacaoOFXDTO;
import br.com.diegocordeiro.dscproject.enums.NaturezaMovimento;
import br.com.diegocordeiro.dscproject.enums.OrigemLancamento;
import br.com.diegocordeiro.dscproject.model.conta.Conta;
import br.com.diegocordeiro.dscproject.model.transacao.TransacaoBancaria;
import br.com.diegocordeiro.dscproject.repository.conta.ContaRepository;
import br.com.diegocordeiro.dscproject.repository.transacao.TransacaoBancariaRepository;
import br.com.diegocordeiro.dscproject.service.exceptions.RegraNegocioException;
import com.webcohesion.ofx4j.domain.data.MessageSetType;
import com.webcohesion.ofx4j.domain.data.ResponseEnvelope;
import com.webcohesion.ofx4j.domain.data.banking.BankStatementResponseTransaction;
import com.webcohesion.ofx4j.domain.data.banking.BankingResponseMessageSet;
import com.webcohesion.ofx4j.domain.data.common.Transaction;
import com.webcohesion.ofx4j.io.AggregateUnmarshaller;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class TransacaoBancariaImportacaoService {

    private final TransacaoBancariaRepository transacaoBancariaRepository;
    private final ContaRepository contaRepository;

    public TransacaoBancariaImportacaoService(TransacaoBancariaRepository transacaoBancariaRepository, ContaRepository contaRepository) {
        this.transacaoBancariaRepository = transacaoBancariaRepository;
        this.contaRepository = contaRepository;
    }

    @Transactional
    public ResultadoImportacaoOFXDTO importarOFX(MultipartFile arquivo, Long contaId, Long usuarioId) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new RegraNegocioException("arquivo", "msg.transacao.importar.arquivo-obrigatorio");
        }

        Conta conta = contaRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(contaId, usuarioId)
            .filter(Conta::isAtivo)
            .orElseThrow(() -> new RegraNegocioException("contaId", "msg.transacao.conta.invalida"));

        List<Transaction> transacoesLidas = lerTransacoesOfx(arquivo);
        int totalLidos = transacoesLidas.size();
        int totalImportados = 0;
        int totalIgnorados = 0;

        for (Transaction t : transacoesLidas) {
            String fitid = (t.getId() != null && !t.getId().isBlank()) ? t.getId().trim() : null;

            if (fitid != null && transacaoBancariaRepository.existsByIdExternoAndContaIdAndDataExclusaoIsNull(fitid, conta.getId())) {
                totalIgnorados++;
                continue;
            }

            double valorBruto = t.getAmount();
            BigDecimal valor = BigDecimal.valueOf(valorBruto).abs();
            if (valor.compareTo(BigDecimal.ZERO) == 0) {
                totalIgnorados++;
                continue;
            }

            NaturezaMovimento natureza = (valorBruto >= 0) ? NaturezaMovimento.CREDITO : NaturezaMovimento.DEBITO;

            LocalDate data = null;
            if (t.getDatePosted() != null) {
                Date dt = t.getDatePosted();
                data = dt.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            } else {
                data = LocalDate.now();
            }

            YearMonth competencia = YearMonth.from(data);
            String desc = (t.getMemo() != null && !t.getMemo().isBlank())
                ? t.getMemo().trim()
                : ((t.getName() != null && !t.getName().isBlank()) ? t.getName().trim() : "Transação Bancária");

            TransacaoBancaria transacao = new TransacaoBancaria();
            transacao.setDescricao(desc);
            transacao.setValor(valor);
            transacao.setNatureza(natureza);
            transacao.setDataLancamento(data);
            transacao.setCompetencia(competencia);
            transacao.setConta(conta);
            transacao.setOrigem(OrigemLancamento.IMPORTACAO);
            transacao.setIdExterno(fitid);
            transacao.setPagamentoFatura(false);

            transacaoBancariaRepository.save(transacao);
            totalImportados++;
        }

        return ResultadoImportacaoOFXDTO.builder()
            .totalLidos(totalLidos)
            .totalImportados(totalImportados)
            .totalIgnorados(totalIgnorados)
            .build();
    }

    private List<Transaction> lerTransacoesOfx(MultipartFile arquivo) {
        try {
            InputStreamReader reader = new InputStreamReader(arquivo.getInputStream(), StandardCharsets.UTF_8);
            AggregateUnmarshaller a = new AggregateUnmarshaller(ResponseEnvelope.class);
            ResponseEnvelope re = (ResponseEnvelope) a.unmarshal(reader);

            List<Transaction> resultado = new ArrayList<>();
            BankingResponseMessageSet bankSet = (BankingResponseMessageSet) re.getMessageSet(MessageSetType.banking);
            if (bankSet != null && bankSet.getStatementResponses() != null) {
                for (BankStatementResponseTransaction resp : bankSet.getStatementResponses()) {
                    if (resp.getMessage() != null && resp.getMessage().getTransactionList() != null) {
                        List<Transaction> trans = resp.getMessage().getTransactionList().getTransactions();
                        if (trans != null) {
                            resultado.addAll(trans);
                        }
                    }
                }
            }
            return resultado;
        } catch (Exception e) {
            throw new RegraNegocioException("arquivo", "msg.transacao.importar.formato-invalido");
        }
    }
}
