package br.com.diegocordeiro.dscproject.service.openfinance;

import br.com.diegocordeiro.dscproject.dto.openfinance.pluggy.PluggyAccountDTO;
import br.com.diegocordeiro.dscproject.dto.openfinance.pluggy.PluggyTransactionDTO;
import br.com.diegocordeiro.dscproject.enums.AmbienteCredencial;
import br.com.diegocordeiro.dscproject.enums.StatusConexao;
import br.com.diegocordeiro.dscproject.enums.StatusSincronizacao;
import br.com.diegocordeiro.dscproject.enums.TipoContaExterna;
import br.com.diegocordeiro.dscproject.enums.TipoSincronizacao;
import br.com.diegocordeiro.dscproject.model.instituicaofinanceira.OpenFinanceProvedor;
import br.com.diegocordeiro.dscproject.model.openfinance.OpenFinanceConexao;
import br.com.diegocordeiro.dscproject.model.openfinance.OpenFinanceContaExterna;
import br.com.diegocordeiro.dscproject.model.openfinance.OpenFinanceCredencial;
import br.com.diegocordeiro.dscproject.model.openfinance.OpenFinanceSincronizacao;
import br.com.diegocordeiro.dscproject.model.openfinance.OpenFinanceTransacao;
import br.com.diegocordeiro.dscproject.model.usuario.Usuario;
import br.com.diegocordeiro.dscproject.repository.openfinance.OpenFinanceConexaoRepository;
import br.com.diegocordeiro.dscproject.repository.openfinance.OpenFinanceContaExternaRepository;
import br.com.diegocordeiro.dscproject.repository.openfinance.OpenFinanceFaturaRepository;
import br.com.diegocordeiro.dscproject.repository.openfinance.OpenFinanceSincronizacaoRepository;
import br.com.diegocordeiro.dscproject.repository.openfinance.OpenFinanceTransacaoRepository;
import br.com.diegocordeiro.dscproject.service.exceptions.RegraNegocioException;
import br.com.diegocordeiro.dscproject.service.openfinance.provider.OpenFinanceProviderFactory;
import br.com.diegocordeiro.dscproject.service.openfinance.provider.OpenFinanceProviderStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OpenFinanceSincronizacaoServiceTest {

    @Mock
    private OpenFinanceConexaoRepository conexaoRepository;
    @Mock
    private OpenFinanceContaExternaRepository contaExternaRepository;
    @Mock
    private OpenFinanceTransacaoRepository transacaoRepository;
    @Mock
    private OpenFinanceFaturaRepository faturaRepository;
    @Mock
    private OpenFinanceSincronizacaoRepository sincronizacaoRepository;
    @Mock
    private OpenFinanceCredencialService credencialService;
    @Mock
    private OpenFinanceProviderFactory providerFactory;
    @Mock
    private OpenFinanceProviderStrategy providerStrategy;

    private OpenFinanceSincronizacaoService service;

    @BeforeEach
    void setUp() {
        service = new OpenFinanceSincronizacaoService(
                conexaoRepository,
                contaExternaRepository,
                transacaoRepository,
                faturaRepository,
                sincronizacaoRepository,
                credencialService,
                providerFactory
        );
    }

    @Test
    @DisplayName("Sincronização manual com menos de 15 min da anterior lança RegraNegocioException por rate limit")
    void sincronizarConexao_rateLimitAtivo_lancaExcecao() {
        Long conexaoId = 1L;
        Long usuarioId = 10L;

        Usuario u = new Usuario();
        u.setId(usuarioId);

        OpenFinanceConexao conexao = new OpenFinanceConexao();
        conexao.setId(conexaoId);
        conexao.setUsuario(u);
        conexao.setUltimaSincronizacaoEm(LocalDateTime.now().minusMinutes(5)); // há apenas 5 minutos

        when(conexaoRepository.findById(conexaoId)).thenReturn(Optional.of(conexao));

        assertThatThrownBy(() -> service.sincronizarConexao(conexaoId, TipoSincronizacao.MANUAL, usuarioId))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("openfinance.sync.rate-limit");
    }

    @Test
    @DisplayName("Sincronização com sucesso ingere contas e transações nas tabelas de staging")
    void sincronizarConexao_sucesso_ingereDadosEmStaging() {
        Long conexaoId = 1L;
        Long usuarioId = 10L;

        Usuario u = new Usuario();
        u.setId(usuarioId);

        OpenFinanceProvedor prov = new OpenFinanceProvedor();
        prov.setCodigo("PLUGGY");

        OpenFinanceCredencial cred = new OpenFinanceCredencial();
        cred.setClientId("cid");
        cred.setAmbiente(AmbienteCredencial.PRODUCTION);

        OpenFinanceConexao conexao = new OpenFinanceConexao();
        conexao.setId(conexaoId);
        conexao.setUsuario(u);
        conexao.setIdExterno("item-ext-1");
        conexao.setProvedor(prov);
        conexao.setCredencial(cred);
        conexao.setUltimaSincronizacaoEm(null); // primeira sincronização

        PluggyAccountDTO acc = new PluggyAccountDTO();
        acc.setId("acc-1");
        acc.setName("Conta Corrente");
        acc.setType("BANK");
        acc.setBalance(new BigDecimal("1500.00"));

        PluggyTransactionDTO tx = new PluggyTransactionDTO();
        tx.setId("tx-1");
        tx.setDescription("Padaria");
        tx.setAmount(new BigDecimal("25.50"));
        tx.setType("DEBIT");
        tx.setDate(LocalDate.now());

        OpenFinanceContaExterna contaExterna = new OpenFinanceContaExterna();
        contaExterna.setId(100L);
        contaExterna.setIdExterno("acc-1");
        contaExterna.setTipo(TipoContaExterna.BANK);

        when(conexaoRepository.findById(conexaoId)).thenReturn(Optional.of(conexao));
        when(sincronizacaoRepository.save(any(OpenFinanceSincronizacao.class))).thenAnswer(inv -> inv.getArgument(0));
        when(credencialService.obterClientSecretDecifrado(cred)).thenReturn("secret");
        when(providerFactory.obterStrategy("PLUGGY")).thenReturn(providerStrategy);
        when(providerStrategy.buscarContas("cid", "secret", AmbienteCredencial.PRODUCTION, "item-ext-1"))
                .thenReturn(List.of(acc));
        when(contaExternaRepository.findByIdExterno("acc-1")).thenReturn(Optional.of(contaExterna));
        when(contaExternaRepository.findByConexaoIdAndDataExclusaoIsNull(conexaoId)).thenReturn(List.of(contaExterna));
        when(providerStrategy.buscarTransacoes(eq("cid"), eq("secret"), eq(AmbienteCredencial.PRODUCTION), eq("acc-1"), any(), any()))
                .thenReturn(List.of(tx));
        when(transacaoRepository.existsByIdExterno("tx-1")).thenReturn(false);

        service.sincronizarConexao(conexaoId, TipoSincronizacao.MANUAL, usuarioId);

        assertThat(conexao.getStatus()).isEqualTo(StatusConexao.ATUALIZADO);
        assertThat(conexao.getUltimaSincronizacaoEm()).isNotNull();

        verify(contaExternaRepository).save(any(OpenFinanceContaExterna.class));
        verify(transacaoRepository).save(any(OpenFinanceTransacao.class));
        verify(conexaoRepository, times(2)).save(conexao);
    }
}
