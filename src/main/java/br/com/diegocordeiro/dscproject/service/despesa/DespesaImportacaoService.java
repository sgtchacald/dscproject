package br.com.diegocordeiro.dscproject.service.despesa;

import br.com.diegocordeiro.dscproject.enums.MeioPagamento;
import br.com.diegocordeiro.dscproject.enums.OrigemLancamento;
import br.com.diegocordeiro.dscproject.enums.StatusPagamento;
import br.com.diegocordeiro.dscproject.model.cartao.CartaoCredito;
import br.com.diegocordeiro.dscproject.model.categoria.Categoria;
import br.com.diegocordeiro.dscproject.model.despesa.Despesa;
import br.com.diegocordeiro.dscproject.repository.cartao.CartaoCreditoRepository;
import br.com.diegocordeiro.dscproject.repository.categoria.CategoriaRepository;
import br.com.diegocordeiro.dscproject.repository.despesa.DespesaRepository;
import br.com.diegocordeiro.dscproject.service.exceptions.RegraNegocioException;
import com.webcohesion.ofx4j.domain.data.MessageSetType;
import com.webcohesion.ofx4j.domain.data.ResponseEnvelope;
import com.webcohesion.ofx4j.domain.data.banking.BankStatementResponseTransaction;
import com.webcohesion.ofx4j.domain.data.banking.BankingResponseMessageSet;
import com.webcohesion.ofx4j.domain.data.common.Transaction;
import com.webcohesion.ofx4j.domain.data.creditcard.CreditCardResponseMessageSet;
import com.webcohesion.ofx4j.domain.data.creditcard.CreditCardStatementResponseTransaction;
import com.webcohesion.ofx4j.io.AggregateUnmarshaller;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class DespesaImportacaoService {

    private final DespesaRepository despesaRepository;
    private final CartaoCreditoRepository cartaoCreditoRepository;
    private final CategoriaRepository categoriaRepository;

    public DespesaImportacaoService(DespesaRepository despesaRepository, CartaoCreditoRepository cartaoCreditoRepository, CategoriaRepository categoriaRepository) {
        this.despesaRepository = despesaRepository;
        this.cartaoCreditoRepository = cartaoCreditoRepository;
        this.categoriaRepository = categoriaRepository;
    }

    private record ItemFatura(LocalDate data, String descricao, BigDecimal valor) {}

    @Transactional
    public int importarFatura(
            MultipartFile arquivo,
            Long cartaoId,
            String competencia,
            String formato,
            String dtVencimento,
            Long categoriaIdPadrao,
            Long usuarioId,
            String loginAutor) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new RegraNegocioException("msg.despesa.importacao.erro");
        }
        if (cartaoId == null) {
            throw new RegraNegocioException("cartaoId", "msg.despesa.cartao.invalido");
        }
        CartaoCredito cartao = cartaoCreditoRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(cartaoId, usuarioId)
                .orElseThrow(() -> new RegraNegocioException("cartaoId", "msg.despesa.cartao.invalido"));

        YearMonth ym;
        try {
            ym = YearMonth.parse(competencia);
        } catch (Exception e) {
            throw new RegraNegocioException("competencia", "msg.despesa.importacao.erro");
        }

        LocalDate dataVencimento = null;
        if (dtVencimento != null && !dtVencimento.isBlank()) {
            try {
                dataVencimento = LocalDate.parse(dtVencimento);
            } catch (Exception ignored) {
            }
        }

        Categoria categoria = null;
        if (categoriaIdPadrao != null) {
            categoria = categoriaRepository.findByIdAndDataExclusaoIsNull(categoriaIdPadrao).orElse(null);
        }

        List<ItemFatura> itens = lerItensFatura(arquivo, formato, ym.getYear());
        if (itens.isEmpty()) {
            throw new RegraNegocioException("msg.despesa.importacao.nenhum-registro");
        }

        List<Despesa> despesas = new ArrayList<>();
        for (ItemFatura item : itens) {
            Despesa d = new Despesa();
            d.setNome(item.descricao());
            d.setDescricao(item.descricao());
            d.setValor(item.valor().abs());
            d.setDataLancamento(item.data() != null ? item.data() : ym.atDay(1));
            d.setDataVencimento(dataVencimento);
            d.setCompetencia(ym);
            d.setCartao(cartao);
            d.setConta(null);
            d.setCategoria(categoria);
            d.setMeioPagamento(MeioPagamento.CREDITO);
            d.setStatusPagamento(StatusPagamento.NAO_SE_APLICA);
            d.setDataPagamento(null);
            d.setOrigem(OrigemLancamento.IMPORTACAO);
            d.setParcelada(false);
            d.setNroParcela(null);
            d.setQtdParcelas(null);
            d.setParcelaPai(null);
            d.setValorTotalCompra(null);
            d.setRecorrente(false);
            d.setRecorrentePai(null);
            d.setCriadoPor(loginAutor);
            d.setAlteradoPor(loginAutor);
            despesas.add(d);
        }

        despesaRepository.saveAll(despesas);
        return despesas.size();
    }

    private List<ItemFatura> lerItensFatura(MultipartFile arquivo, String formato, int anoCompetencia) {
        String fmt = formato != null ? formato.trim().toUpperCase() : "";
        try {
            return switch (fmt) {
                case "ITAU" -> lerExcelItau(arquivo.getInputStream());
                case "BRADESCO" -> lerExcelBradesco(arquivo.getInputStream(), anoCompetencia);
                case "C6BANK" -> lerC6Bank(arquivo, anoCompetencia);
                case "OFX" -> lerOfx(arquivo.getInputStream());
                default -> throw new RegraNegocioException("msg.despesa.importacao.erro");
            };
        } catch (RegraNegocioException e) {
            throw e;
        } catch (Exception e) {
            throw new RegraNegocioException("msg.despesa.importacao.erro");
        }
    }

    private List<ItemFatura> lerExcelItau(InputStream is) throws Exception {
        List<ItemFatura> itens = new ArrayList<>();
        try (Workbook workbook = WorkbookFactory.create(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) return itens;

            for (int i = 0; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                Cell cellData = row.getCell(1);
                Cell cellDesc = row.getCell(2);
                Cell cellValor = row.getCell(4);

                if (cellData == null || cellDesc == null || cellValor == null) continue;

                String textoDesc = obterTextoCelula(cellDesc);
                if (textoDesc.isBlank()) continue;
                if ("data".equalsIgnoreCase(obterTextoCelula(cellData)) || "lançamento".equalsIgnoreCase(textoDesc)) continue;
                if (isPagamentoFatura(textoDesc)) continue;

                LocalDate data = parseData(cellData, 0);
                BigDecimal valor = parseValor(cellValor);

                if (data != null && valor != null && valor.compareTo(BigDecimal.ZERO) != 0) {
                    itens.add(new ItemFatura(data, textoDesc, valor));
                }
            }
        }
        return itens;
    }

    private List<ItemFatura> lerExcelBradesco(InputStream is, int anoPadrao) throws Exception {
        List<ItemFatura> itens = new ArrayList<>();
        try (Workbook workbook = WorkbookFactory.create(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) return itens;

            for (int i = 0; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                Cell cellData = row.getCell(0);
                Cell cellDesc = row.getCell(1);
                Cell cellValor = row.getCell(4);

                if (cellData == null || cellDesc == null || cellValor == null) continue;

                String textoDesc = obterTextoCelula(cellDesc);
                if (textoDesc.isBlank()) continue;
                if ("data".equalsIgnoreCase(obterTextoCelula(cellData)) || "histórico".equalsIgnoreCase(textoDesc)) continue;
                if (isPagamentoFatura(textoDesc)) continue;

                LocalDate data = parseData(cellData, anoPadrao);
                BigDecimal valor = parseValor(cellValor);

                if (data != null && valor != null && valor.compareTo(BigDecimal.ZERO) != 0) {
                    itens.add(new ItemFatura(data, textoDesc, valor));
                }
            }
        }
        return itens;
    }

    private List<ItemFatura> lerC6Bank(MultipartFile arquivo, int anoPadrao) throws Exception {
        String nomeArquivo = arquivo.getOriginalFilename() != null ? arquivo.getOriginalFilename().toLowerCase() : "";
        if (nomeArquivo.endsWith(".csv")) {
            return lerCsvC6Bank(arquivo.getInputStream());
        }

        List<ItemFatura> itens = new ArrayList<>();
        try (Workbook workbook = WorkbookFactory.create(arquivo.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) return itens;

            for (int i = 0; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                Cell cellData = row.getCell(0);
                Cell cellDesc = row.getCell(4);
                Cell cellValor = row.getCell(8);

                if (cellData == null || cellDesc == null || cellValor == null) continue;

                String textoDesc = obterTextoCelula(cellDesc);
                if (textoDesc.isBlank()) continue;
                if ("data de compra".equalsIgnoreCase(obterTextoCelula(cellData)) || "descrição".equalsIgnoreCase(textoDesc)) continue;
                if (isPagamentoFatura(textoDesc)) continue;

                LocalDate data = parseData(cellData, anoPadrao);
                BigDecimal valor = parseValor(cellValor);

                if (data != null && valor != null && valor.compareTo(BigDecimal.ZERO) != 0) {
                    itens.add(new ItemFatura(data, textoDesc, valor));
                }
            }
        }
        return itens;
    }

    private List<ItemFatura> lerCsvC6Bank(InputStream is) throws Exception {
        List<ItemFatura> itens = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String linha;
            while ((linha = reader.readLine()) != null) {
                if (linha.isBlank()) continue;
                String sep = linha.contains(";") ? ";" : ",";
                String[] partes = linha.split(sep);
                if (partes.length < 3) continue;

                String p0 = partes[0].trim().replace("\"", "");
                if ("data de compra".equalsIgnoreCase(p0) || "data".equalsIgnoreCase(p0)) continue;

                String pDesc = partes.length > 4 ? partes[4].trim().replace("\"", "") : partes[1].trim().replace("\"", "");
                String pValor = partes.length > 8 ? partes[8].trim().replace("\"", "") : partes[partes.length - 1].trim().replace("\"", "");

                if (isPagamentoFatura(pDesc)) continue;

                LocalDate data = parseDataTexto(p0, 0);
                BigDecimal valor = parseValorTexto(pValor);

                if (data != null && valor != null && valor.compareTo(BigDecimal.ZERO) != 0) {
                    itens.add(new ItemFatura(data, pDesc, valor));
                }
            }
        }
        return itens;
    }

    private List<ItemFatura> lerOfx(InputStream is) throws Exception {
        List<ItemFatura> itens = new ArrayList<>();
        InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
        AggregateUnmarshaller a = new AggregateUnmarshaller(ResponseEnvelope.class);
        ResponseEnvelope re = (ResponseEnvelope) a.unmarshal(reader);

        CreditCardResponseMessageSet ccSet = (CreditCardResponseMessageSet) re.getMessageSet(MessageSetType.creditcard);
        if (ccSet != null && ccSet.getStatementResponses() != null) {
            for (CreditCardStatementResponseTransaction resp : ccSet.getStatementResponses()) {
                if (resp.getMessage() != null && resp.getMessage().getTransactionList() != null) {
                    List<Transaction> trans = resp.getMessage().getTransactionList().getTransactions();
                    if (trans != null) {
                        for (Transaction t : trans) {
                            adicionarTransacaoOfx(itens, t);
                        }
                    }
                }
            }
        }

        if (itens.isEmpty()) {
            BankingResponseMessageSet bankSet = (BankingResponseMessageSet) re.getMessageSet(MessageSetType.banking);
            if (bankSet != null && bankSet.getStatementResponses() != null) {
                for (BankStatementResponseTransaction resp : bankSet.getStatementResponses()) {
                    if (resp.getMessage() != null && resp.getMessage().getTransactionList() != null) {
                        List<Transaction> trans = resp.getMessage().getTransactionList().getTransactions();
                        if (trans != null) {
                            for (Transaction t : trans) {
                                adicionarTransacaoOfx(itens, t);
                            }
                        }
                    }
                }
            }
        }

        return itens;
    }

    private void adicionarTransacaoOfx(List<ItemFatura> itens, Transaction t) {
        String desc = t.getMemo() != null ? t.getMemo().trim() : (t.getName() != null ? t.getName().trim() : "Transação");
        if (isPagamentoFatura(desc)) return;

        BigDecimal valor = BigDecimal.valueOf(t.getAmount()).abs();
        LocalDate data = null;
        if (t.getDatePosted() != null) {
            Date dt = t.getDatePosted();
            data = dt.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }

        if (data != null && valor.compareTo(BigDecimal.ZERO) != 0) {
            itens.add(new ItemFatura(data, desc, valor));
        }
    }

    private boolean isPagamentoFatura(String desc) {
        if (desc == null) return false;
        String s = desc.toUpperCase();
        return s.contains("PAGAMENTO RECEBIDO") || s.contains("PAGAMENTO EFETUADO") || s.contains("PAGTO EFETUADO");
    }

    private String obterTextoCelula(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf(cell.getNumericCellValue()).trim();
            default -> cell.toString().trim();
        };
    }

    private LocalDate parseData(Cell cell, int anoPadrao) {
        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return cell.getLocalDateTimeCellValue().toLocalDate();
        }
        return parseDataTexto(obterTextoCelula(cell), anoPadrao);
    }

    private LocalDate parseDataTexto(String texto, int anoPadrao) {
        if (texto == null || texto.isBlank()) return null;
        String limpo = texto.trim();
        if (limpo.matches("\\d{2}/\\d{2}") && anoPadrao > 0) {
            limpo = limpo + "/" + anoPadrao;
        }
        DateTimeFormatter[] formatters = new DateTimeFormatter[]{
                DateTimeFormatter.ofPattern("dd/MM/yyyy"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd"),
                DateTimeFormatter.ofPattern("dd/MM/yy")
        };
        for (DateTimeFormatter fmt : formatters) {
            try {
                return LocalDate.parse(limpo, fmt);
            } catch (DateTimeParseException ignored) {
            }
        }
        return null;
    }

    private BigDecimal parseValor(Cell cell) {
        if (cell.getCellType() == CellType.NUMERIC) {
            return BigDecimal.valueOf(cell.getNumericCellValue()).abs();
        }
        return parseValorTexto(obterTextoCelula(cell));
    }

    private BigDecimal parseValorTexto(String texto) {
        if (texto == null || texto.isBlank()) return null;
        try {
            String limpo = texto.replace("R$", "").replace(" ", "").trim();
            if (limpo.contains(",") && limpo.contains(".")) {
                limpo = limpo.replace(".", "").replace(",", ".");
            } else if (limpo.contains(",")) {
                limpo = limpo.replace(",", ".");
            }
            return new BigDecimal(limpo).abs();
        } catch (Exception e) {
            return null;
        }
    }
}
