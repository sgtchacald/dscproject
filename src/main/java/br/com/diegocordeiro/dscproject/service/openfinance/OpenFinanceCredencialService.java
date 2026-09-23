package br.com.diegocordeiro.dscproject.service.openfinance;

import br.com.diegocordeiro.dscproject.dto.openfinance.OpenFinanceCredencialDTO;
import br.com.diegocordeiro.dscproject.dto.openfinance.OpenFinanceCredencialExibicaoDTO;
import br.com.diegocordeiro.dscproject.model.instituicaofinanceira.OpenFinanceProvedor;
import br.com.diegocordeiro.dscproject.model.openfinance.OpenFinanceCredencial;
import br.com.diegocordeiro.dscproject.model.usuario.Usuario;
import br.com.diegocordeiro.dscproject.repository.instituicaofinanceira.OpenFinanceProvedorRepository;
import br.com.diegocordeiro.dscproject.repository.openfinance.OpenFinanceCredencialRepository;
import br.com.diegocordeiro.dscproject.repository.usuario.UsuarioRepository;
import br.com.diegocordeiro.dscproject.service.exceptions.RegistroNaoEncontradoException;
import br.com.diegocordeiro.dscproject.service.exceptions.RegraNegocioException;
import br.com.diegocordeiro.dscproject.service.openfinance.provider.OpenFinanceProviderFactory;
import br.com.diegocordeiro.dscproject.service.openfinance.provider.OpenFinanceProviderStrategy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class OpenFinanceCredencialService {

    private static final String MASCARA_SECRET = "********";

    private final OpenFinanceCredencialRepository credencialRepository;
    private final OpenFinanceProvedorRepository provedorRepository;
    private final UsuarioRepository usuarioRepository;
    private final OpenFinanceCryptoService cryptoService;
    private final OpenFinanceProviderFactory providerFactory;

    public OpenFinanceCredencialService(OpenFinanceCredencialRepository credencialRepository, OpenFinanceProvedorRepository provedorRepository, UsuarioRepository usuarioRepository, OpenFinanceCryptoService cryptoService, OpenFinanceProviderFactory providerFactory) {
        this.credencialRepository = credencialRepository;
        this.provedorRepository = provedorRepository;
        this.usuarioRepository = usuarioRepository;
        this.cryptoService = cryptoService;
        this.providerFactory = providerFactory;
    }

    @Transactional(readOnly = true)
    public OpenFinanceCredencialExibicaoDTO buscarCredencialExibicao(Long usuarioId, Long provedorId) {
        Optional<OpenFinanceCredencial> opt = credencialRepository.findByUsuarioIdAndProvedorIdAndDataExclusaoIsNull(usuarioId, provedorId);
        if (opt.isPresent()) {
            OpenFinanceCredencial cr = opt.get();
            return OpenFinanceCredencialExibicaoDTO.builder()
                    .id(cr.getId())
                    .provedorId(cr.getProvedor().getId())
                    .provedorNome(cr.getProvedor().getNome())
                    .provedorCodigo(cr.getProvedor().getCodigo())
                    .clientId(cr.getClientId())
                    .ambiente(cr.getAmbiente())
                    .ativo(cr.isAtivo())
                    .possuiSecret(cr.getClientSecret() != null && !cr.getClientSecret().isBlank())
                    .secretMascarado(MASCARA_SECRET)
                    .build();
        }

        OpenFinanceProvedor provedor = provedorRepository.findById(provedorId)
                .orElseThrow(() -> new RegistroNaoEncontradoException("openfinance.provedor.nao-encontrado"));

        return OpenFinanceCredencialExibicaoDTO.builder()
                .provedorId(provedor.getId())
                .provedorNome(provedor.getNome())
                .provedorCodigo(provedor.getCodigo())
                .possuiSecret(false)
                .ativo(false)
                .build();
    }

    @Transactional(readOnly = true)
    public boolean testarCredencial(OpenFinanceCredencialDTO dto) {
        OpenFinanceProvedor provedor = provedorRepository.findById(dto.getProvedorId())
                .orElseThrow(() -> new RegistroNaoEncontradoException("openfinance.provedor.nao-encontrado"));

        OpenFinanceProviderStrategy strategy = providerFactory.obterStrategy(provedor.getCodigo());
        return strategy.validarCredenciais(dto.getClientId(), dto.getClientSecret(), dto.getAmbiente());
    }

    @Transactional
    public OpenFinanceCredencial salvarCredencial(Long usuarioId, OpenFinanceCredencialDTO dto) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RegistroNaoEncontradoException("usuario.nao-encontrado"));

        OpenFinanceProvedor provedor = provedorRepository.findById(dto.getProvedorId())
                .orElseThrow(() -> new RegistroNaoEncontradoException("openfinance.provedor.nao-encontrado"));

        Optional<OpenFinanceCredencial> optExistente = credencialRepository
                .findByUsuarioIdAndProvedorIdAndDataExclusaoIsNull(usuarioId, provedor.getId());

        String secretCifrado;
        if (dto.getClientSecret() != null && !dto.getClientSecret().isBlank()) {
            OpenFinanceProviderStrategy strategy = providerFactory.obterStrategy(provedor.getCodigo());
            boolean valida = strategy.validarCredenciais(dto.getClientId(), dto.getClientSecret(), dto.getAmbiente());
            if (!valida) {
                throw new RegraNegocioException("openfinance.credencial.invalida");
            }
            secretCifrado = cryptoService.encrypt(dto.getClientSecret());
        } else if (optExistente.isPresent()) {
            secretCifrado = optExistente.get().getClientSecret();
        } else {
            throw new RegraNegocioException("openfinance.credencial.secret-obrigatorio");
        }

        OpenFinanceCredencial credencial = optExistente.orElseGet(OpenFinanceCredencial::new);
        credencial.setUsuario(usuario);
        credencial.setProvedor(provedor);
        credencial.setClientId(dto.getClientId());
        credencial.setClientSecret(secretCifrado);
        credencial.setAmbiente(dto.getAmbiente());
        credencial.setAtivo(true);

        return credencialRepository.save(credencial);
    }

    @Transactional(readOnly = true)
    public OpenFinanceCredencial obterCredencialAtiva(Long usuarioId, Long provedorId) {
        return credencialRepository.findByUsuarioIdAndProvedorIdAndDataExclusaoIsNull(usuarioId, provedorId)
                .filter(OpenFinanceCredencial::isAtivo)
                .orElseThrow(() -> new RegraNegocioException("openfinance.credencial.nao-configurada"));
    }

    @Transactional(readOnly = true)
    public OpenFinanceCredencial obterCredencialAtivaPorProvedorCodigo(Long usuarioId, String provedorCodigo) {
        OpenFinanceProvedor provedor = provedorRepository.findByCodigo(provedorCodigo)
                .orElseThrow(() -> new RegistroNaoEncontradoException("openfinance.provedor.nao-encontrado"));

        return obterCredencialAtiva(usuarioId, provedor.getId());
    }

    public String obterClientSecretDecifrado(OpenFinanceCredencial credencial) {
        return cryptoService.decrypt(credencial.getClientSecret());
    }
}
