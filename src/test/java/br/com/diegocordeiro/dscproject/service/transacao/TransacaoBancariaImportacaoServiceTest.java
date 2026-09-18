package br.com.diegocordeiro.dscproject.service.transacao;

import br.com.diegocordeiro.dscproject.dto.transacao.ResultadoImportacaoOFXDTO;
import br.com.diegocordeiro.dscproject.enums.NaturezaMovimento;
import br.com.diegocordeiro.dscproject.enums.OrigemLancamento;
import br.com.diegocordeiro.dscproject.model.conta.Conta;
import br.com.diegocordeiro.dscproject.model.transacao.TransacaoBancaria;
import br.com.diegocordeiro.dscproject.model.usuario.Usuario;
import br.com.diegocordeiro.dscproject.repository.conta.ContaRepository;
import br.com.diegocordeiro.dscproject.repository.transacao.TransacaoBancariaRepository;
import br.com.diegocordeiro.dscproject.service.exceptions.RegraNegocioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransacaoBancariaImportacaoServiceTest {

    @Mock
    private TransacaoBancariaRepository transacaoBancariaRepository;

    @Mock
    private ContaRepository contaRepository;

    private TransacaoBancariaImportacaoService importacaoService;

    @BeforeEach
    void setUp() {
        importacaoService = new TransacaoBancariaImportacaoService(transacaoBancariaRepository, contaRepository);
    }

    private Conta contaAtiva(Long id, Long usuarioId) {
        Conta c = new Conta();
        c.setId(id);
        c.setDescricao("Conta Corrente Nubank");
        c.setAtivo(true);
        Usuario u = new Usuario();
        u.setId(usuarioId);
        c.setUsuario(u);
        return c;
    }

    private String gerarOfxBancario() {
        return "OFXHEADER:100\n"
            + "DATA:OFXSGML\n"
            + "VERSION:102\n"
            + "SECURITY:NONE\n"
            + "ENCODING:USASCII\n"
            + "CHARSET:1252\n"
            + "COMPRESSION:NONE\n"
            + "OLDFILEUID:NONE\n"
            + "NEWFILEUID:NONE\n\n"
            + "<OFX>\n"
            + "<BANKMSGSRSV1>\n"
            + "<STMTTRNRS>\n"
            + "<TRNUID>1\n"
            + "<STATUS><CODE>0<SEVERITY>INFO</STATUS>\n"
            + "<STMTRS>\n"
            + "<CURDEF>BRL\n"
            + "<BANKACCTFROM><BANKID>0260<ACCTID>123456</ACCTID><ACCTTYPE>CHECKING</BANKACCTFROM>\n"
            + "<BANKTRANLIST>\n"
            + "<DTSTART>20260901000000\n"
            + "<DTEND>20260930000000\n"
            + "<STMTTRN>\n"
            + "<TRNTYPE>CREDIT\n"
            + "<DTPOSTED>20260910120000\n"
            + "<TRNAMT>1500.00\n"
            + "<FITID>FITID-CREDITO-01\n"
            + "<MEMO>Pix Recebido\n"
            + "</STMTTRN>\n"
            + "<STMTTRN>\n"
            + "<TRNTYPE>DEBIT\n"
            + "<DTPOSTED>20260912120000\n"
            + "<TRNAMT>-200.00\n"
            + "<FITID>FITID-DEBITO-02\n"
            + "<MEMO>Pagamento Boleto\n"
            + "</STMTTRN>\n"
            + "</BANKTRANLIST>\n"
            + "</STMTRS>\n"
            + "</STMTTRNRS>\n"
            + "</BANKMSGSRSV1>\n"
            + "</OFX>\n";
    }

    @Test
    @DisplayName("Deve importar extrato bancario OFX com sucesso deduplicando registros")
    void deveImportarOfxComSucesso() {
        Long contaId = 1L;
        Long usuarioId = 1L;

        when(contaRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(contaId, usuarioId))
            .thenReturn(Optional.of(contaAtiva(contaId, usuarioId)));

        // Primeiro FITID é novo, segundo já existe
        when(transacaoBancariaRepository.existsByIdExternoAndContaIdAndDataExclusaoIsNull("FITID-CREDITO-01", contaId))
            .thenReturn(false);
        when(transacaoBancariaRepository.existsByIdExternoAndContaIdAndDataExclusaoIsNull("FITID-DEBITO-02", contaId))
            .thenReturn(true);

        MockMultipartFile arquivo = new MockMultipartFile(
            "arquivo", "extrato.ofx", "application/x-ofx",
            gerarOfxBancario().getBytes(StandardCharsets.UTF_8)
        );

        ResultadoImportacaoOFXDTO res = importacaoService.importarOFX(arquivo, contaId, usuarioId);

        assertNotNull(res);
        assertEquals(2, res.getTotalLidos());
        assertEquals(1, res.getTotalImportados());
        assertEquals(1, res.getTotalIgnorados());

        ArgumentCaptor<TransacaoBancaria> captor = ArgumentCaptor.forClass(TransacaoBancaria.class);
        verify(transacaoBancariaRepository, times(1)).save(captor.capture());

        TransacaoBancaria salva = captor.getValue();
        assertEquals("Pix Recebido", salva.getDescricao());
        assertEquals(new BigDecimal("1500.0"), salva.getValor());
        assertEquals(NaturezaMovimento.CREDITO, salva.getNatureza());
        assertEquals(OrigemLancamento.IMPORTACAO, salva.getOrigem());
        assertEquals("FITID-CREDITO-01", salva.getIdExterno());
    }

    @Test
    @DisplayName("Deve lancar excecao ao tentar importar arquivo vazio")
    void deveLancarExcecaoArquivoVazio() {
        MockMultipartFile arquivo = new MockMultipartFile("arquivo", "vazio.ofx", "application/x-ofx", new byte[0]);
        assertThrows(RegraNegocioException.class, () ->
            importacaoService.importarOFX(arquivo, 1L, 1L));
    }
}
