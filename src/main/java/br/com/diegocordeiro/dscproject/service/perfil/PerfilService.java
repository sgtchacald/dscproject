package br.com.diegocordeiro.dscproject.service.perfil;

import br.com.diegocordeiro.dscproject.dto.perfil.PerfilEdicaoDTO;
import br.com.diegocordeiro.dscproject.dto.perfil.PerfilFormDTO;
import br.com.diegocordeiro.dscproject.dto.perfil.PerfilResumoDTO;
import br.com.diegocordeiro.dscproject.model.perfil.Perfil;
import br.com.diegocordeiro.dscproject.model.perfil.PerfilPermissao;
import br.com.diegocordeiro.dscproject.model.perfil.Permissao;
import br.com.diegocordeiro.dscproject.model.usuario.Usuario;
import br.com.diegocordeiro.dscproject.repository.perfil.PerfilPermissaoRepository;
import br.com.diegocordeiro.dscproject.repository.perfil.PerfilRepository;
import br.com.diegocordeiro.dscproject.repository.perfil.PermissaoRepository;
import br.com.diegocordeiro.dscproject.repository.usuario.UsuarioRepository;
import br.com.diegocordeiro.dscproject.service.exceptions.RegistroNaoEncontradoException;
import br.com.diegocordeiro.dscproject.service.exceptions.RegraNegocioException;
import br.com.diegocordeiro.dscproject.util.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class PerfilService {

    /** Permissão que gere perfis — alvo das travas anti-lockout. */
    static final String PERFIS_VINCULAR_PERMISSAO = "PERFIS_VINCULAR_PERMISSAO";
    /** Permissão que reatribui o perfil de um usuário — a segunda trava anti-lockout. */
    static final String USUARIOS_EDITAR = "USUARIOS_EDITAR";

    private final PerfilRepository perfilRepository;
    private final PerfilPermissaoRepository perfilPermissaoRepository;
    private final PermissaoRepository permissaoRepository;
    private final UsuarioRepository usuarioRepository;

    public PerfilService(PerfilRepository perfilRepository, PerfilPermissaoRepository perfilPermissaoRepository, PermissaoRepository permissaoRepository, UsuarioRepository usuarioRepository) {
        this.perfilRepository = perfilRepository;
        this.perfilPermissaoRepository = perfilPermissaoRepository;
        this.permissaoRepository = permissaoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public List<PerfilResumoDTO> listar() {
        return perfilRepository.listarResumo();
    }

    @Transactional(readOnly = true)
    public PerfilEdicaoDTO buscarParaEdicao(Long id) {
        Perfil perfil = buscarPorId(id);
        return new PerfilEdicaoDTO(perfil, perfilPermissaoRepository.buscarCodigosPermissaoAtivos(id));
    }

    /** {@code idAtual} é nulo na criação e traz o próprio id na edição. */
    @Transactional(readOnly = true)
    public boolean verificarCodigoDuplicado(String codigo, Long idAtual) {
        if (codigo == null || codigo.isBlank()) {
            return false;
        }
        return perfilRepository.contarPorCodigo(normalizarCodigo(codigo), idAtual) > 0;
    }

    @Transactional
    public Perfil inserir(PerfilFormDTO dto) {
        Perfil perfil = new Perfil();
        perfil.setCodigo(normalizarCodigo(dto.getCodigo()));
        perfil.setNome(dto.getNome());
        perfil.setDescricao(dto.getDescricao());
        perfil.setSistema(false);
        perfilRepository.save(perfil);

        substituirVinculos(perfil, codigosDesejados(dto.getPermissoes()), new HashSet<>());
        validarAntiLockoutGlobal();
        return perfil;
    }

    @Transactional
    public Perfil editar(Long id, PerfilFormDTO dto, String loginUsuarioLogado) {
        Perfil perfil = buscarPorId(id);

        if (!perfil.isSistema()) {                       // código de perfil de sistema não muda
            perfil.setCodigo(normalizarCodigo(dto.getCodigo()));
        }
        perfil.setNome(dto.getNome());
        perfil.setDescricao(dto.getDescricao());

        Set<String> desejados = codigosDesejados(dto.getPermissoes());
        Set<String> atuais = new HashSet<>(perfilPermissaoRepository.buscarCodigosPermissaoAtivos(id));

        if (ehPerfilDoUsuarioLogado(loginUsuarioLogado, perfil)
                && atuais.contains(PERFIS_VINCULAR_PERMISSAO) && !desejados.contains(PERFIS_VINCULAR_PERMISSAO)) {
            throw new RegraNegocioException("perfil.antilockout.proprio");
        }

        substituirVinculos(perfil, desejados, atuais);
        validarAntiLockoutGlobal();
        return perfil;
    }

    @Transactional
    public void excluir(Long id) {
        Perfil perfil = buscarPorId(id);
        if (perfil.isSistema()) {
            throw new RegraNegocioException("perfil.exclusao.sistema");
        }
        if (usuarioRepository.existsByPerfil(perfil)) {
            throw new RegraNegocioException("perfil.exclusao.com.usuarios");
        }
        if (perfil.isExcluido()) {
            return;
        }
        Instant agora = Instant.now();
        String autor = SecurityUtils.loginAtual();
        perfil.setDataExclusao(agora);
        perfil.setExcluidoPor(autor);
        for (PerfilPermissao vinculo : perfilPermissaoRepository.findByPerfilAndDataExclusaoIsNull(perfil)) {
            vinculo.setDataExclusao(agora);
            vinculo.setExcluidoPor(autor);
        }
    }

    private void substituirVinculos(Perfil perfil, Set<String> desejados, Set<String> atuais) {
        Instant agora = Instant.now();
        String autor = SecurityUtils.loginAtual();

        for (PerfilPermissao vinculo : perfilPermissaoRepository.findByPerfilAndDataExclusaoIsNull(perfil)) {
            if (!desejados.contains(vinculo.getPermissao().getCodigo())) {
                vinculo.setDataExclusao(agora);
                vinculo.setExcluidoPor(autor);
            }
        }

        for (String codigo : desejados) {
            if (atuais.contains(codigo)) {
                continue;
            }
            Permissao permissao = permissaoRepository.findByCodigo(codigo)
                .orElseThrow(() -> new RegraNegocioException("perfil.permissao.invalida"));
            if (permissao.isOrfa()) {                    // permissão órfã não pode ser vinculada de novo
                throw new RegraNegocioException("perfil.permissao.orfa");
            }
            PerfilPermissao vinculo = perfilPermissaoRepository.findByPerfilAndPermissao(perfil, permissao)
                .orElseGet(() -> new PerfilPermissao(perfil, permissao));
            vinculo.setDataExclusao(null);
            vinculo.setExcluidoPor(null);
            perfilPermissaoRepository.save(vinculo);
        }
    }

    /** Nenhuma gravação pode deixar o sistema sem um perfil, com usuário ativo, capaz de gerir perfis ou usuários. */
    private void validarAntiLockoutGlobal() {
        perfilPermissaoRepository.flush();
        for (String codigo : List.of(PERFIS_VINCULAR_PERMISSAO, USUARIOS_EDITAR)) {
            if (perfilRepository.contarPerfisComUsuarioAtivoConcedendo(codigo) == 0) {
                throw new RegraNegocioException("perfil.antilockout.global");
            }
        }
    }

    private boolean ehPerfilDoUsuarioLogado(String login, Perfil perfil) {
        if (login == null || login.isBlank() || perfil.getId() == null) {
            return false;
        }
        Usuario usuario = usuarioRepository.findByLoginOrEmail(login, login);
        return usuario != null && usuario.getPerfil() != null
            && perfil.getId().equals(usuario.getPerfil().getId());
    }

    private Set<String> codigosDesejados(Collection<String> brutos) {
        Set<String> codigos = new LinkedHashSet<>();
        if (brutos != null) {
            for (String bruto : brutos) {
                if (bruto != null && !bruto.isBlank()) {
                    codigos.add(normalizarCodigo(bruto));
                }
            }
        }
        return codigos;
    }

    private String normalizarCodigo(String valor) {
        return valor == null ? null : valor.trim().toUpperCase();
    }

    private Perfil buscarPorId(Long id) {
        return perfilRepository.findById(id)
            .orElseThrow(() -> new RegistroNaoEncontradoException("perfil.nao.encontrado"));
    }
}
