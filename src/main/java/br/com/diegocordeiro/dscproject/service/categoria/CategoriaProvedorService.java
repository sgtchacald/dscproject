package br.com.diegocordeiro.dscproject.service.categoria;

import br.com.diegocordeiro.dscproject.dto.categoriaprovedor.CategoriaProvedorFormDTO;
import br.com.diegocordeiro.dscproject.dto.categoriaprovedor.CategoriaProvedorGridDTO;
import br.com.diegocordeiro.dscproject.model.categoria.Categoria;
import br.com.diegocordeiro.dscproject.model.categoria.CategoriaProvedor;
import br.com.diegocordeiro.dscproject.model.instituicaofinanceira.OpfiProvedor;
import br.com.diegocordeiro.dscproject.repository.categoria.CategoriaProvedorRepository;
import br.com.diegocordeiro.dscproject.repository.categoria.CategoriaRepository;
import br.com.diegocordeiro.dscproject.repository.instituicaofinanceira.OpfiProvedorRepository;
import br.com.diegocordeiro.dscproject.service.exceptions.RegistroNaoEncontradoException;
import br.com.diegocordeiro.dscproject.service.exceptions.RegraNegocioException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class CategoriaProvedorService {

    private final CategoriaProvedorRepository categoriaProvedorRepository;
    private final CategoriaRepository categoriaRepository;
    private final OpfiProvedorRepository opfiProvedorRepository;

    public CategoriaProvedorService(CategoriaProvedorRepository categoriaProvedorRepository, CategoriaRepository categoriaRepository, OpfiProvedorRepository opfiProvedorRepository) {
        this.categoriaProvedorRepository = categoriaProvedorRepository;
        this.categoriaRepository = categoriaRepository;
        this.opfiProvedorRepository = opfiProvedorRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoriaProvedorGridDTO> listar(Long provedorId) {
        return categoriaProvedorRepository.listarPorProvedor(provedorId).stream()
            .map(cp -> CategoriaProvedorGridDTO.builder()
                .id(cp.getId())
                .rotuloExterno(cp.getRotuloExterno())
                .provedorId(cp.getProvedor().getId())
                .provedorNome(cp.getProvedor().getNome())
                .categoriaId(cp.getCategoria().getId())
                .categoriaNome(cp.getCategoria().getNome())
                .build())
            .toList();
    }

    @Transactional(readOnly = true)
    public CategoriaProvedor buscarPorId(Long id) {
        return categoriaProvedorRepository.findById(id)
            .orElseThrow(() -> new RegistroNaoEncontradoException("categoriaprovedor.nao-encontrado"));
    }

    @Transactional
    public CategoriaProvedor inserir(CategoriaProvedorFormDTO dto) {
        String rotulo = dto.getRotuloExterno().trim();
        validarUnicidade(dto.getProvedorId(), rotulo, null);

        Categoria categoria = validarECarregarCategoriaAtiva(dto.getCategoriaId());
        OpfiProvedor provedor = validarECarregarProvedorAtivo(dto.getProvedorId());

        CategoriaProvedor vinculo = new CategoriaProvedor();
        vinculo.setRotuloExterno(rotulo);
        vinculo.setCategoria(categoria);
        vinculo.setProvedor(provedor);

        return categoriaProvedorRepository.save(vinculo);
    }

    @Transactional
    public CategoriaProvedor editar(Long id, CategoriaProvedorFormDTO dto) {
        CategoriaProvedor vinculo = buscarPorId(id);
        String rotulo = dto.getRotuloExterno().trim();
        validarUnicidade(dto.getProvedorId(), rotulo, id);

        Categoria categoria = validarECarregarCategoriaAtiva(dto.getCategoriaId());
        OpfiProvedor provedor = validarECarregarProvedorAtivo(dto.getProvedorId());

        vinculo.setRotuloExterno(rotulo);
        vinculo.setCategoria(categoria);
        vinculo.setProvedor(provedor);

        return categoriaProvedorRepository.save(vinculo);
    }

    @Transactional
    public void excluir(Long id, String usuarioLogado) {
        CategoriaProvedor vinculo = buscarPorId(id);
        vinculo.setDataExclusao(Instant.now());
        vinculo.setExcluidoPor(usuarioLogado);
        categoriaProvedorRepository.save(vinculo);
    }

    private void validarUnicidade(Long provedorId, String rotulo, Long idAtual) {
        if (categoriaProvedorRepository.contarPorProvedorERotulo(provedorId, rotulo, idAtual) > 0) {
            throw new RegraNegocioException("rotuloExterno", "categoriaprovedor.rotulo.duplicado");
        }
    }

    private Categoria validarECarregarCategoriaAtiva(Long categoriaId) {
        Categoria categoria = categoriaRepository.findById(categoriaId)
            .orElseThrow(() -> new RegraNegocioException("categoriaId", "categoriaprovedor.categoria.invalida"));

        if (!categoria.isAtivo() || categoria.isExcluido()) {
            throw new RegraNegocioException("categoriaId", "categoriaprovedor.categoria.invalida");
        }
        return categoria;
    }

    private OpfiProvedor validarECarregarProvedorAtivo(Long provedorId) {
        OpfiProvedor provedor = opfiProvedorRepository.findById(provedorId)
            .orElseThrow(() -> new RegraNegocioException("provedorId", "categoriaprovedor.provedor.invalido"));

        if (!provedor.isAtivo() || provedor.isExcluido()) {
            throw new RegraNegocioException("provedorId", "categoriaprovedor.provedor.invalido");
        }
        return provedor;
    }
}
