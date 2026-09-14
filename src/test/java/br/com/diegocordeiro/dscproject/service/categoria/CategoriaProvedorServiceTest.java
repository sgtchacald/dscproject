package br.com.diegocordeiro.dscproject.service.categoria;

import br.com.diegocordeiro.dscproject.dto.categoriaprovedor.CategoriaProvedorFormDTO;
import br.com.diegocordeiro.dscproject.dto.categoriaprovedor.CategoriaProvedorGridDTO;
import br.com.diegocordeiro.dscproject.model.categoria.Categoria;
import br.com.diegocordeiro.dscproject.model.categoria.CategoriaProvedor;
import br.com.diegocordeiro.dscproject.model.instituicaofinanceira.OpfiProvedor;
import br.com.diegocordeiro.dscproject.repository.categoria.CategoriaProvedorRepository;
import br.com.diegocordeiro.dscproject.repository.categoria.CategoriaRepository;
import br.com.diegocordeiro.dscproject.repository.instituicaofinanceira.OpfiProvedorRepository;
import br.com.diegocordeiro.dscproject.service.exceptions.RegraNegocioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoriaProvedorServiceTest {

    @Mock
    private CategoriaProvedorRepository categoriaProvedorRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private OpfiProvedorRepository opfiProvedorRepository;

    private CategoriaProvedorService categoriaProvedorService;

    @BeforeEach
    void setUp() {
        categoriaProvedorService = new CategoriaProvedorService(
            categoriaProvedorRepository, categoriaRepository, opfiProvedorRepository);
    }

    @Test
    @DisplayName("RN07 / C6 - Deve impedir cadastro de dois vínculos com mesmo rótulo no mesmo provedor")
    void inserir_quandoRotuloDuplicadoNoMesmoProvedor_deveLancarExcecao() {
        when(categoriaProvedorRepository.contarPorProvedorERotulo(1L, "Food and drinks", null)).thenReturn(1L);

        CategoriaProvedorFormDTO dto = new CategoriaProvedorFormDTO();
        dto.setProvedorId(1L);
        dto.setRotuloExterno("Food and drinks");
        dto.setCategoriaId(10L);

        RegraNegocioException ex = assertThrows(RegraNegocioException.class, () -> categoriaProvedorService.inserir(dto));
        assertEquals("categoriaprovedor.rotulo.duplicado", ex.getMessage());
        assertEquals("rotuloExterno", ex.getCampo());
        verify(categoriaProvedorRepository, never()).save(any());
    }

    @Test
    @DisplayName("RN07 - Permite mesmo rótulo externo em provedores diferentes")
    void inserir_quandoMesmoRotuloEmProvedorDiferente_deveSalvar() {
        when(categoriaProvedorRepository.contarPorProvedorERotulo(2L, "Food and drinks", null)).thenReturn(0L);

        Categoria categoria = new Categoria();
        categoria.setId(10L);
        categoria.setAtivo(true);
        when(categoriaRepository.findById(10L)).thenReturn(Optional.of(categoria));

        OpfiProvedor provedor = new OpfiProvedor();
        provedor.setId(2L);
        provedor.setAtivo(true);
        when(opfiProvedorRepository.findById(2L)).thenReturn(Optional.of(provedor));

        when(categoriaProvedorRepository.save(any())).thenAnswer(inv -> {
            CategoriaProvedor cp = inv.getArgument(0);
            cp.setId(99L);
            return cp;
        });

        CategoriaProvedorFormDTO dto = new CategoriaProvedorFormDTO();
        dto.setProvedorId(2L);
        dto.setRotuloExterno("Food and drinks");
        dto.setCategoriaId(10L);

        CategoriaProvedor salvo = categoriaProvedorService.inserir(dto);
        assertNotNull(salvo);
        assertEquals(99L, salvo.getId());
        assertEquals("Food and drinks", salvo.getRotuloExterno());
        verify(categoriaProvedorRepository).save(any());
    }

    @Test
    @DisplayName("RN10 - Vínculo só aceita categoria ativa e não excluída")
    void inserir_quandoCategoriaInativa_deveLancarExcecao() {
        when(categoriaProvedorRepository.contarPorProvedorERotulo(1L, "Salário", null)).thenReturn(0L);

        Categoria inativa = new Categoria();
        inativa.setId(15L);
        inativa.setAtivo(false);
        when(categoriaRepository.findById(15L)).thenReturn(Optional.of(inativa));

        CategoriaProvedorFormDTO dto = new CategoriaProvedorFormDTO();
        dto.setProvedorId(1L);
        dto.setRotuloExterno("Salário");
        dto.setCategoriaId(15L);

        RegraNegocioException ex = assertThrows(RegraNegocioException.class, () -> categoriaProvedorService.inserir(dto));
        assertEquals("categoriaprovedor.categoria.invalida", ex.getMessage());
        assertEquals("categoriaId", ex.getCampo());
        verify(categoriaProvedorRepository, never()).save(any());
    }

    @Test
    @DisplayName("RN08 - Exclusão lógica de vínculo preenche auditoria")
    void excluir_devePreencherDataEUsuarioExclusao() {
        CategoriaProvedor cp = new CategoriaProvedor();
        cp.setId(5L);
        when(categoriaProvedorRepository.findById(5L)).thenReturn(Optional.of(cp));

        categoriaProvedorService.excluir(5L, "ADMIN");

        assertTrue(cp.isExcluido());
        assertEquals("ADMIN", cp.getExcluidoPor());
        assertNotNull(cp.getDataExclusao());
        verify(categoriaProvedorRepository).save(cp);
    }
}
