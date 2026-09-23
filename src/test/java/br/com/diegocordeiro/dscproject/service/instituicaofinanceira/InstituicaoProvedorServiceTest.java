package br.com.diegocordeiro.dscproject.service.instituicaofinanceira;

import br.com.diegocordeiro.dscproject.dto.instituicaoprovedor.InstituicaoProvedorFormDTO;
import br.com.diegocordeiro.dscproject.dto.instituicaoprovedor.ProvedorOpcaoDTO;
import br.com.diegocordeiro.dscproject.model.instituicaofinanceira.InstituicaoFinanceira;
import br.com.diegocordeiro.dscproject.model.instituicaofinanceira.OpenFinanceInstituicaoProvedor;
import br.com.diegocordeiro.dscproject.model.instituicaofinanceira.OpenFinanceProvedor;
import br.com.diegocordeiro.dscproject.repository.instituicaofinanceira.InstituicaoFinanceiraRepository;
import br.com.diegocordeiro.dscproject.repository.instituicaofinanceira.OpenFinanceInstituicaoProvedorRepository;
import br.com.diegocordeiro.dscproject.repository.instituicaofinanceira.OpenFinanceProvedorRepository;
import br.com.diegocordeiro.dscproject.service.exceptions.RegraNegocioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InstituicaoProvedorServiceTest {

    @Mock
    private OpenFinanceInstituicaoProvedorRepository openFinanceInstituicaoProvedorRepository;

    @Mock
    private InstituicaoFinanceiraRepository instituicaoFinanceiraRepository;

    @Mock
    private OpenFinanceProvedorRepository openFinanceProvedorRepository;

    private InstituicaoProvedorService instituicaoProvedorService;

    @BeforeEach
    void setUp() {
        instituicaoProvedorService = new InstituicaoProvedorService(
                openFinanceInstituicaoProvedorRepository,
                instituicaoFinanceiraRepository,
                openFinanceProvedorRepository
        );
    }

    @Test
    @DisplayName("RN08 - Deve impedir cadastro de vínculo duplicado do par (instituicao, provedor)")
    void inserir_quandoParDuplicado_deveLancarExcecao() {
        when(openFinanceInstituicaoProvedorRepository.contarPorInstituicaoEProvedor(10L, 1L, null)).thenReturn(1L);

        InstituicaoProvedorFormDTO dto = new InstituicaoProvedorFormDTO();
        dto.setProvedorId(1L);
        dto.setInstituicaoId(10L);
        dto.setIdExterno("pluggy_nubank");

        RegraNegocioException ex = assertThrows(RegraNegocioException.class, () -> instituicaoProvedorService.inserir(dto));
        assertEquals("msg.instituicaoprovedor.instituicao.duplicada", ex.getMessage());
        assertEquals("instituicaoId", ex.getCampo());
        verify(openFinanceInstituicaoProvedorRepository, never()).save(any());
    }

    @Test
    @DisplayName("RN09 - Deve impedir cadastro de idExterno duplicado no mesmo provedor")
    void inserir_quandoIdProvedorDuplicado_deveLancarExcecao() {
        when(openFinanceInstituicaoProvedorRepository.contarPorInstituicaoEProvedor(10L, 1L, null)).thenReturn(0L);
        when(openFinanceInstituicaoProvedorRepository.contarPorProvedorEIdExterno(1L, "pluggy_nubank", null)).thenReturn(1L);

        InstituicaoProvedorFormDTO dto = new InstituicaoProvedorFormDTO();
        dto.setProvedorId(1L);
        dto.setInstituicaoId(10L);
        dto.setIdExterno("pluggy_nubank");

        RegraNegocioException ex = assertThrows(RegraNegocioException.class, () -> instituicaoProvedorService.inserir(dto));
        assertEquals("msg.instituicaoprovedor.idexterno.duplicado", ex.getMessage());
        assertEquals("idExterno", ex.getCampo());
        verify(openFinanceInstituicaoProvedorRepository, never()).save(any());
    }

    @Test
    @DisplayName("RN11 - Deve impedir vínculo se instituição inativa ou excluída")
    void inserir_quandoInstituicaoInativa_deveLancarExcecao() {
        when(openFinanceInstituicaoProvedorRepository.contarPorInstituicaoEProvedor(10L, 1L, null)).thenReturn(0L);
        when(openFinanceInstituicaoProvedorRepository.contarPorProvedorEIdExterno(1L, "pluggy_nubank", null)).thenReturn(0L);

        InstituicaoFinanceira inativa = new InstituicaoFinanceira();
        inativa.setId(10L);
        inativa.setAtivo(false);
        when(instituicaoFinanceiraRepository.findByIdAndDataExclusaoIsNull(10L)).thenReturn(Optional.of(inativa));

        InstituicaoProvedorFormDTO dto = new InstituicaoProvedorFormDTO();
        dto.setProvedorId(1L);
        dto.setInstituicaoId(10L);
        dto.setIdExterno("pluggy_nubank");

        RegraNegocioException ex = assertThrows(RegraNegocioException.class, () -> instituicaoProvedorService.inserir(dto));
        assertEquals("msg.instituicaoprovedor.instituicao.invalida", ex.getMessage());
        assertEquals("instituicaoId", ex.getCampo());
        verify(openFinanceInstituicaoProvedorRepository, never()).save(any());
    }

    @Test
    @DisplayName("RN11 - Deve impedir vínculo se provedor inativo ou excluído")
    void inserir_quandoProvedorInativo_deveLancarExcecao() {
        when(openFinanceInstituicaoProvedorRepository.contarPorInstituicaoEProvedor(10L, 1L, null)).thenReturn(0L);
        when(openFinanceInstituicaoProvedorRepository.contarPorProvedorEIdExterno(1L, "pluggy_nubank", null)).thenReturn(0L);

        InstituicaoFinanceira ativa = new InstituicaoFinanceira();
        ativa.setId(10L);
        ativa.setAtivo(true);
        when(instituicaoFinanceiraRepository.findByIdAndDataExclusaoIsNull(10L)).thenReturn(Optional.of(ativa));

        OpenFinanceProvedor inativo = new OpenFinanceProvedor();
        inativo.setId(1L);
        inativo.setAtivo(false);
        when(openFinanceProvedorRepository.findById(1L)).thenReturn(Optional.of(inativo));

        InstituicaoProvedorFormDTO dto = new InstituicaoProvedorFormDTO();
        dto.setProvedorId(1L);
        dto.setInstituicaoId(10L);
        dto.setIdExterno("pluggy_nubank");

        RegraNegocioException ex = assertThrows(RegraNegocioException.class, () -> instituicaoProvedorService.inserir(dto));
        assertEquals("msg.instituicaoprovedor.provedor.invalido", ex.getMessage());
        assertEquals("provedorId", ex.getCampo());
        verify(openFinanceInstituicaoProvedorRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve inserir vínculo válido com sucesso")
    void inserir_comDadosValidos_deveSalvarComSucesso() {
        when(openFinanceInstituicaoProvedorRepository.contarPorInstituicaoEProvedor(10L, 1L, null)).thenReturn(0L);
        when(openFinanceInstituicaoProvedorRepository.contarPorProvedorEIdExterno(1L, "pluggy_nubank", null)).thenReturn(0L);

        InstituicaoFinanceira ativa = new InstituicaoFinanceira();
        ativa.setId(10L);
        ativa.setNome("Nubank");
        ativa.setAtivo(true);
        when(instituicaoFinanceiraRepository.findByIdAndDataExclusaoIsNull(10L)).thenReturn(Optional.of(ativa));

        OpenFinanceProvedor provedorAtivo = new OpenFinanceProvedor();
        provedorAtivo.setId(1L);
        provedorAtivo.setNome("Pluggy");
        provedorAtivo.setAtivo(true);
        when(openFinanceProvedorRepository.findById(1L)).thenReturn(Optional.of(provedorAtivo));

        when(openFinanceInstituicaoProvedorRepository.save(any())).thenAnswer(inv -> {
            OpenFinanceInstituicaoProvedor oip = inv.getArgument(0);
            oip.setId(100L);
            return oip;
        });

        InstituicaoProvedorFormDTO dto = new InstituicaoProvedorFormDTO();
        dto.setProvedorId(1L);
        dto.setInstituicaoId(10L);
        dto.setIdExterno("pluggy_nubank");

        OpenFinanceInstituicaoProvedor salvo = instituicaoProvedorService.inserir(dto);
        assertNotNull(salvo);
        assertEquals("pluggy_nubank", salvo.getIdExterno());
        assertEquals(ativa, salvo.getInstituicao());
        assertEquals(provedorAtivo, salvo.getProvedor());
        verify(openFinanceInstituicaoProvedorRepository).save(any());
    }

    @Test
    @DisplayName("Exclusão lógica de vínculo preenche auditoria")
    void excluir_devePreencherDataEUsuarioExclusao() {
        OpenFinanceInstituicaoProvedor oip = new OpenFinanceInstituicaoProvedor();
        oip.setId(5L);
        when(openFinanceInstituicaoProvedorRepository.findByIdAndDataExclusaoIsNull(5L)).thenReturn(Optional.of(oip));

        instituicaoProvedorService.excluir(5L, "ADMIN");

        assertTrue(oip.isExcluido());
        assertEquals("ADMIN", oip.getExcluidoPor());
        assertNotNull(oip.getDataExclusao());
        verify(openFinanceInstituicaoProvedorRepository).save(oip);
    }
}
