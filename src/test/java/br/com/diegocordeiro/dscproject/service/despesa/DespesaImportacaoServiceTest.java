package br.com.diegocordeiro.dscproject.service.despesa;

import br.com.diegocordeiro.dscproject.enums.OrigemLancamento;
import br.com.diegocordeiro.dscproject.enums.StatusPagamento;
import br.com.diegocordeiro.dscproject.model.cartao.CartaoCredito;
import br.com.diegocordeiro.dscproject.model.despesa.Despesa;
import br.com.diegocordeiro.dscproject.repository.cartao.CartaoCreditoRepository;
import br.com.diegocordeiro.dscproject.repository.categoria.CategoriaRepository;
import br.com.diegocordeiro.dscproject.repository.despesa.DespesaRepository;
import br.com.diegocordeiro.dscproject.service.exceptions.RegraNegocioException;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DespesaImportacaoServiceTest {

    @Mock
    private DespesaRepository despesaRepository;

    @Mock
    private CartaoCreditoRepository cartaoCreditoRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    private DespesaImportacaoService service;

    @BeforeEach
    void setUp() {
        service = new DespesaImportacaoService(despesaRepository, cartaoCreditoRepository, categoriaRepository);
    }

    private CartaoCredito mockCartao(Long id, Long usuarioId) {
        CartaoCredito c = new CartaoCredito();
        c.setId(id);
        c.setDescricao("Nubank Ultravioleta");
        c.setAtivo(true);
        return c;
    }

    @Test
    @DisplayName("RN28 - Arquivo vazio lança RegraNegocioException")
    void importar_arquivoVazio_deveLancarExcecao() {
        MockMultipartFile vazio = new MockMultipartFile("arquivo", "teste.xls", "application/vnd.ms-excel", new byte[0]);
        assertThrows(RegraNegocioException.class,
                () -> service.importarFatura(vazio, 1L, "2026-09", "ITAU", null, null, 1L, "autor"));
    }

    @Test
    @DisplayName("RN28 - Cartão não encontrado lança RegraNegocioException")
    void importar_cartaoInexistente_deveLancarExcecao() {
        MockMultipartFile arquivo = new MockMultipartFile("arquivo", "teste.xls", "application/vnd.ms-excel", "conteudo".getBytes());
        when(cartaoCreditoRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(99L, 1L)).thenReturn(Optional.empty());

        assertThrows(RegraNegocioException.class,
                () -> service.importarFatura(arquivo, 99L, "2026-09", "ITAU", null, null, 1L, "autor"));
    }

    @Test
    @DisplayName("RN28 - Importar Excel Itaú com sucesso")
    void importar_excelItau_comSucesso() throws Exception {
        CartaoCredito cc = mockCartao(1L, 1L);
        when(cartaoCreditoRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(1L, 1L)).thenReturn(Optional.of(cc));

        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet();
        HSSFRow h = sheet.createRow(0);
        h.createCell(0).setCellValue("data");
        h.createCell(1).setCellValue("lançamento");
        h.createCell(3).setCellValue("valor");

        HSSFRow r1 = sheet.createRow(1);
        r1.createCell(0).setCellValue("05/09/2026");
        r1.createCell(1).setCellValue("Uber");
        r1.createCell(3).setCellValue(25.50);

        HSSFRow rPagto = sheet.createRow(2);
        rPagto.createCell(0).setCellValue("10/09/2026");
        rPagto.createCell(1).setCellValue("PAGAMENTO EFETUADO");
        rPagto.createCell(3).setCellValue(500.00);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        wb.write(baos);
        wb.close();

        MockMultipartFile arquivo = new MockMultipartFile("arquivo", "fatura-itau.xls", "application/vnd.ms-excel", baos.toByteArray());

        int total = service.importarFatura(arquivo, 1L, "2026-09", "ITAU", "2026-09-15", null, 1L, "autor");

        assertEquals(1, total);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Despesa>> captor = ArgumentCaptor.forClass(List.class);
        verify(despesaRepository).saveAll(captor.capture());

        List<Despesa> salvas = captor.getValue();
        assertEquals(1, salvas.size());
        Despesa d = salvas.get(0);
        assertEquals("Uber", d.getNome());
        assertEquals(new BigDecimal("25.5"), d.getValor());
        assertEquals(LocalDate.of(2026, 9, 5), d.getDataLancamento());
        assertEquals(LocalDate.of(2026, 9, 15), d.getDataVencimento());
        assertEquals(YearMonth.of(2026, 9), d.getCompetencia());
        assertEquals(OrigemLancamento.IMPORTACAO, d.getOrigem());
        assertEquals(StatusPagamento.NAO, d.getStatusPagamento());
    }

    @Test
    @DisplayName("RN28 - Importar CSV C6 Bank com sucesso")
    void importar_csvC6Bank_comSucesso() {
        CartaoCredito cc = mockCartao(1L, 1L);
        when(cartaoCreditoRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(1L, 1L)).thenReturn(Optional.of(cc));

        String csv = "data de compra;cartao;final;categoria;descrição;moeda;valor;cotacao;valor (em R$)\n"
                + "02/09/2026;Titular;1234;Alimentacao;Restaurante;BRL;80,00;1,00;80,00\n"
                + "03/09/2026;Titular;1234;Pagamento;PAGAMENTO EFETUADO;BRL;100,00;1,00;100,00\n";

        MockMultipartFile arquivo = new MockMultipartFile("arquivo", "fatura.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8));

        int total = service.importarFatura(arquivo, 1L, "2026-09", "C6BANK", null, null, 1L, "autor");

        assertEquals(1, total);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Despesa>> captor = ArgumentCaptor.forClass(List.class);
        verify(despesaRepository).saveAll(captor.capture());

        Despesa d = captor.getValue().get(0);
        assertEquals("Restaurante", d.getNome());
        assertEquals(new BigDecimal("80.00"), d.getValor());
        assertEquals(LocalDate.of(2026, 9, 2), d.getDataLancamento());
    }

    @Test
    @DisplayName("RN28 - Importar OFX com sucesso")
    void importar_ofx_comSucesso() {
        CartaoCredito cc = mockCartao(1L, 1L);
        when(cartaoCreditoRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(1L, 1L)).thenReturn(Optional.of(cc));

        String ofx = "OFXHEADER:100\n"
                + "DATA:OFXSGML\n"
                + "VERSION:102\n"
                + "SECURITY:NONE\n"
                + "ENCODING:USASCII\n"
                + "CHARSET:1252\n"
                + "COMPRESSION:NONE\n"
                + "OLDFILEUID:NONE\n"
                + "NEWFILEUID:NONE\n\n"
                + "<OFX>\n"
                + "<CREDITCARDMSGSRSV1>\n"
                + "<CCSTMTTRNRS>\n"
                + "<TRNUID>1\n"
                + "<STATUS><CODE>0<SEVERITY>INFO</STATUS>\n"
                + "<CCSTMTRS>\n"
                + "<CURDEF>BRL\n"
                + "<CCACCTFROM><ACCTID>1234</CCACCTFROM>\n"
                + "<BANKTRANLIST>\n"
                + "<DTSTART>20260901000000\n"
                + "<DTEND>20260930000000\n"
                + "<STMTTRN>\n"
                + "<TRNTYPE>DEBIT\n"
                + "<DTPOSTED>20260905120000\n"
                + "<TRNAMT>-45.00\n"
                + "<FITID>TRANS1\n"
                + "<MEMO>Farmacia\n"
                + "</STMTTRN>\n"
                + "</BANKTRANLIST>\n"
                + "</CCSTMTRS>\n"
                + "</CCSTMTTRNRS>\n"
                + "</CREDITCARDMSGSRSV1>\n"
                + "</OFX>\n";

        MockMultipartFile arquivo = new MockMultipartFile("arquivo", "extrato.ofx", "application/x-ofx", ofx.getBytes(StandardCharsets.UTF_8));

        int total = service.importarFatura(arquivo, 1L, "2026-09", "OFX", null, null, 1L, "autor");

        assertEquals(1, total);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Despesa>> captor = ArgumentCaptor.forClass(List.class);
        verify(despesaRepository).saveAll(captor.capture());

        Despesa d = captor.getValue().get(0);
        assertEquals("Farmacia", d.getNome());
        assertEquals(new BigDecimal("45.0"), d.getValor());
    }
}
