package br.com.diegocordeiro.dscproject.service.usuario;

import br.com.diegocordeiro.dscproject.dto.minhaconta.MinhaContaDTO;
import br.com.diegocordeiro.dscproject.dto.usuario.PerfilOpcaoDTO;
import br.com.diegocordeiro.dscproject.dto.usuario.RevisaoUsuarioDTO;
import br.com.diegocordeiro.dscproject.dto.usuario.UsuarioDTO;
import br.com.diegocordeiro.dscproject.dto.usuario.UsuarioEdicaoDTO;
import br.com.diegocordeiro.dscproject.dto.usuario.UsuarioListaDTO;
import br.com.diegocordeiro.dscproject.model.perfil.Perfil;
import br.com.diegocordeiro.dscproject.model.usuario.Usuario;
import br.com.diegocordeiro.dscproject.repository.perfil.PerfilRepository;
import br.com.diegocordeiro.dscproject.repository.usuario.UsuarioRepository;
import br.com.diegocordeiro.dscproject.service.exceptions.RegistroNaoEncontradoException;
import br.com.diegocordeiro.dscproject.service.exceptions.RegraNegocioException;
import br.com.diegocordeiro.dscproject.util.SecurityUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class UsuarioService {

    private static final String PERFIL_ADMIN = "ADMIN";
    private static final String PERFIL_USER = "USER";

    private final UsuarioRepository usuarioRepository;
    private final PerfilRepository perfilRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PerfilRepository perfilRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.perfilRepository = perfilRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UsuarioListaDTO> listarParaGrid() {
        return usuarioRepository.listarParaGrid().stream()
            .map(UsuarioListaDTO::new)
            .toList();
    }

    @Transactional(readOnly = true)
    public UsuarioEdicaoDTO buscarParaEdicao(Long id) {
        return new UsuarioEdicaoDTO(buscarPorId(id));
    }

    /** Perfis para os selects das telas de usuário, ordenados por nome. */
    @Transactional(readOnly = true)
    public List<PerfilOpcaoDTO> listarPerfis() {
        return perfilRepository.findAllByOrderByNomeAsc().stream()
            .map(perfil -> new PerfilOpcaoDTO(perfil.getCodigo(), perfil.getNome()))
            .toList();
    }

    /** {@code idAtual} é nulo na criação e traz o próprio id na edição. */
    @Transactional(readOnly = true)
    public boolean verificarSeExiste(String valor, Long idAtual) {
        if (valor == null || valor.isBlank()) {
            return false;
        }
        return usuarioRepository.contarPorLoginOuEmail(valor.trim(), idAtual) > 0;
    }

    /** Cadastro administrativo. */
    @Transactional
    public Usuario inserir(UsuarioDTO dto) {
        Usuario usuario = new Usuario();
        aplicarDados(usuario, dto);
        usuario.setSenha(passwordEncoder.encode(dto.getSenha()));
        usuario.setPerfil(resolverPerfil(dto.getPerfilCodigo()));  // quem chega aqui já passou pela autorização do endpoint
        return usuarioRepository.save(usuario);
    }

    /** Auto-cadastro público: sempre perfil USER. */
    @Transactional
    public Usuario autoCadastrar(UsuarioDTO dto) {
        Usuario usuario = new Usuario();
        aplicarDados(usuario, dto);
        usuario.setSenha(passwordEncoder.encode(dto.getSenha()));
        usuario.setPerfil(resolverPerfil(PERFIL_USER));
        return usuarioRepository.save(usuario);
    }

    /** Edição administrativa. */
    @Transactional
    public Usuario editar(Long id, UsuarioDTO dto) {
        Usuario usuario = buscarPorId(id);
        Perfil novoPerfil = resolverPerfil(dto.getPerfilCodigo());

        boolean eraAdmin = PERFIL_ADMIN.equals(usuario.getPerfil().getCodigo());
        boolean seraAdmin = PERFIL_ADMIN.equals(novoPerfil.getCodigo());
        if (eraAdmin && !seraAdmin && usuarioRepository.contarAdminsAtivos() <= 1) {   // não deixa o sistema sem ADMIN ativo
            throw new RegraNegocioException("usuario.ultimo.admin");
        }

        aplicarDados(usuario, dto);
        usuario.setPerfil(novoPerfil);
        return usuarioRepository.save(usuario);
    }

    /** Ação administrativa dedicada: troca só a senha, cifrando com BCrypt. Nenhum outro campo é tocado. */
    @Transactional
    public Usuario alterarSenha(Long id, String novaSenha) {
        Usuario usuario = buscarPorId(id);
        usuario.setSenha(passwordEncoder.encode(novaSenha));
        return usuarioRepository.save(usuario);
    }

    /**
     * Atualização self-service: o usuário edita o próprio registro, identificado pelo login autenticado.
     * Persiste sempre os dados cadastrais; a senha só quando informada. O perfil não muda.
     */
    @Transactional
    public Usuario atualizarPropriaConta(String login, MinhaContaDTO dto) {
        Usuario usuario = buscarPorLogin(login);
        usuario.setNome(dto.getNome());
        usuario.setGenero(dto.getGenero());
        usuario.setNascimento(dto.getNascimento());
        usuario.setEmail(dto.getEmail() != null ? dto.getEmail().trim() : null);
        usuario.setLogin(dto.getLogin() != null ? dto.getLogin().trim() : null);
        if (dto.senhaInformada()) {
            usuario.setSenha(passwordEncoder.encode(dto.getSenha()));
        }
        return usuarioRepository.save(usuario);
    }

    @Transactional(readOnly = true)
    public Usuario buscarPorLogin(String login) {
        Usuario usuario = usuarioRepository.findByLoginOrEmail(login, login);
        if (usuario == null) {
            throw new RegistroNaoEncontradoException("usuario.nao.encontrado");
        }
        return usuario;
    }

    /** Exclusão lógica, com as travas de não excluir a si mesmo nem o último ADMIN. */
    @Transactional
    public void excluir(Long id, String loginUsuarioLogado) {
        Usuario usuario = buscarPorId(id);

        if (loginUsuarioLogado != null
                && (loginUsuarioLogado.equals(usuario.getLogin())
                    || loginUsuarioLogado.equals(usuario.getEmail()))) {   // ninguém exclui a própria conta
            throw new RegraNegocioException("usuario.exclusao.proprio");
        }
        if (PERFIL_ADMIN.equals(usuario.getPerfil().getCodigo())
                && usuarioRepository.contarAdminsAtivos() <= 1) {   // não deixa o sistema sem ADMIN ativo
            throw new RegraNegocioException("usuario.ultimo.admin");
        }
        if (usuario.isExcluido()) {
            return;
        }
        usuario.setDataExclusao(Instant.now());
        usuario.setExcluidoPor(SecurityUtils.loginAtual());
        usuarioRepository.save(usuario);
    }

    /** Histórico via Envers; o DTO nunca traz a senha. */
    @Transactional(readOnly = true)
    public Page<RevisaoUsuarioDTO> buscarHistorico(Long id, Pageable pageable) {
        buscarPorId(id);
        return usuarioRepository.findRevisions(id, pageable).map(RevisaoUsuarioDTO::new);
    }

    private void aplicarDados(Usuario usuario, UsuarioDTO dto) {
        usuario.setNome(dto.getNome());
        usuario.setGenero(dto.getGenero());
        usuario.setNascimento(dto.getNascimento());
        usuario.setEmail(dto.getEmail() != null ? dto.getEmail().trim() : null);
        usuario.setLogin(dto.getLogin() != null ? dto.getLogin().trim() : null);
    }

    private Perfil resolverPerfil(String codigo) {
        String alvo = (codigo == null || codigo.isBlank()) ? PERFIL_USER : codigo;
        return perfilRepository.findByCodigo(alvo)
            .orElseThrow(() -> new RegraNegocioException("perfilCodigo", "usuario.perfil.invalido"));
    }

    private Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id)
            .orElseThrow(() -> new RegistroNaoEncontradoException("usuario.nao.encontrado"));
    }
}
