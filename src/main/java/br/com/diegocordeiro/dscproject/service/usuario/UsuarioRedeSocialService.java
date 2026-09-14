package br.com.diegocordeiro.dscproject.service.usuario;

import br.com.diegocordeiro.dscproject.dto.usuario.UsuarioRedeSocialDTO;
import br.com.diegocordeiro.dscproject.dto.usuario.UsuarioRedeSocialFormDTO;
import br.com.diegocordeiro.dscproject.model.usuario.Usuario;
import br.com.diegocordeiro.dscproject.model.usuario.UsuarioRedeSocial;
import br.com.diegocordeiro.dscproject.repository.usuario.UsuarioRedeSocialRepository;
import br.com.diegocordeiro.dscproject.repository.usuario.UsuarioRepository;
import br.com.diegocordeiro.dscproject.service.exceptions.RegistroNaoEncontradoException;
import br.com.diegocordeiro.dscproject.service.exceptions.RegraNegocioException;
import br.com.diegocordeiro.dscproject.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioRedeSocialService {

    private final UsuarioRedeSocialRepository usuarioRedeSocialRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public List<UsuarioRedeSocialDTO> listarPorUsuario(Long usuarioId) {
        return usuarioRedeSocialRepository.findByUsuarioIdAndDataExclusaoIsNullOrderByTipoAsc(usuarioId)
                .stream()
                .map(UsuarioRedeSocialDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UsuarioRedeSocialDTO> listarAtivasPorUsuario(Long usuarioId) {
        return usuarioRedeSocialRepository.buscarAtivasPorUsuario(usuarioId)
                .stream()
                .map(UsuarioRedeSocialDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UsuarioRedeSocialDTO> listarAtivasPorLogin(String login) {
        if (login == null || SecurityUtils.AUTOR_SISTEMA.equals(login)) {
            return List.of();
        }
        Usuario usuario = usuarioRepository.findByLoginOrEmail(login, login);
        if (usuario == null) {
            return List.of();
        }
        return listarAtivasPorUsuario(usuario.getId());
    }

    @Transactional(readOnly = true)
    public UsuarioRedeSocialDTO buscarPorIdEUsuario(Long id, Long usuarioId) {
        UsuarioRedeSocial rede = usuarioRedeSocialRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(id, usuarioId)
                .orElseThrow(() -> new RegistroNaoEncontradoException("usuario.rede-social.nao-encontrada"));
        return UsuarioRedeSocialDTO.fromEntity(rede);
    }

    @Transactional
    public UsuarioRedeSocialDTO inserir(UsuarioRedeSocialFormDTO form, Long usuarioId, String operador) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RegistroNaoEncontradoException("usuario.nao.encontrado"));

        validarForm(form, usuarioId, null);

        UsuarioRedeSocial rede = UsuarioRedeSocial.builder()
                .usuario(usuario)
                .tipo(form.getTipo())
                .url(form.getUrl().trim())
                .identificador(form.getIdentificador() != null ? form.getIdentificador().trim() : null)
                .ativo(form.isAtivo())
                .build();
        rede.setCriadoPor(operador);

        UsuarioRedeSocial salvo = usuarioRedeSocialRepository.save(rede);
        return UsuarioRedeSocialDTO.fromEntity(salvo);
    }

    @Transactional
    public UsuarioRedeSocialDTO editar(Long id, UsuarioRedeSocialFormDTO form, Long usuarioId, String operador) {
        UsuarioRedeSocial rede = usuarioRedeSocialRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(id, usuarioId)
                .orElseThrow(() -> new RegistroNaoEncontradoException("usuario.rede-social.nao-encontrada"));

        validarForm(form, usuarioId, id);

        rede.setTipo(form.getTipo());
        rede.setUrl(form.getUrl().trim());
        rede.setIdentificador(form.getIdentificador() != null ? form.getIdentificador().trim() : null);
        rede.setAtivo(form.isAtivo());
        rede.setAlteradoPor(operador);

        UsuarioRedeSocial salvo = usuarioRedeSocialRepository.save(rede);
        return UsuarioRedeSocialDTO.fromEntity(salvo);
    }

    @Transactional
    public void excluir(Long id, Long usuarioId, String operador) {
        UsuarioRedeSocial rede = usuarioRedeSocialRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(id, usuarioId)
                .orElseThrow(() -> new RegistroNaoEncontradoException("usuario.rede-social.nao-encontrada"));

        rede.setDataExclusao(Instant.now());
        rede.setExcluidoPor(operador);
        usuarioRedeSocialRepository.save(rede);
    }

    private void validarForm(UsuarioRedeSocialFormDTO form, Long usuarioId, Long idEdicao) {
        if (form.getTipo() == null) {
            throw new RegraNegocioException("tipo", "usuario.rede-social.tipo.obrigatorio");
        }

        boolean duplicado = (idEdicao == null)
                ? usuarioRedeSocialRepository.existsByUsuarioIdAndTipoAndDataExclusaoIsNull(usuarioId, form.getTipo())
                : usuarioRedeSocialRepository.existsByUsuarioIdAndTipoAndIdNotAndDataExclusaoIsNull(usuarioId, form.getTipo(), idEdicao);

        if (duplicado) {
            throw new RegraNegocioException("tipo", "usuario.rede-social.tipo.duplicado");
        }

        if (form.getUrl() == null || form.getUrl().isBlank()) {
            throw new RegraNegocioException("url", "usuario.rede-social.url.obrigatoria");
        }

        if (!isUrlValida(form.getUrl())) {
            throw new RegraNegocioException("url", "usuario.rede-social.url.invalida");
        }
    }

    private boolean isUrlValida(String url) {
        String trimmed = url.trim();
        if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
            return false;
        }
        try {
            URI uri = new URI(trimmed);
            return uri.getHost() != null && !uri.getHost().isBlank();
        } catch (Exception e) {
            return false;
        }
    }
}
