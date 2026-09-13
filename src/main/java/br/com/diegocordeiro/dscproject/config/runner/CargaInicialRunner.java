package br.com.diegocordeiro.dscproject.config.runner;

import br.com.diegocordeiro.dscproject.enums.Genero;
import br.com.diegocordeiro.dscproject.model.perfil.Perfil;
import br.com.diegocordeiro.dscproject.model.perfil.PerfilPermissao;
import br.com.diegocordeiro.dscproject.model.perfil.Permissao;
import br.com.diegocordeiro.dscproject.model.usuario.Usuario;
import br.com.diegocordeiro.dscproject.repository.perfil.PerfilPermissaoRepository;
import br.com.diegocordeiro.dscproject.repository.perfil.PerfilRepository;
import br.com.diegocordeiro.dscproject.repository.perfil.PermissaoRepository;
import br.com.diegocordeiro.dscproject.repository.usuario.UsuarioRepository;
import br.com.diegocordeiro.dscproject.service.perfil.PermissaoCatalogoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Carga inicial idempotente do RBAC: perfis ADMIN/USER, sincronização do catálogo
 * de permissões com o código, vínculos do perfil ADMIN e — quando não há nenhum
 * usuário ADMIN ativo — o ADMIN inicial, lido de {@code app.admin.*}
 * (a senha nunca fica em código).
 */
@Component
public class CargaInicialRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(CargaInicialRunner.class);

    private final PerfilRepository perfilRepository;
    private final PermissaoRepository permissaoRepository;
    private final PerfilPermissaoRepository perfilPermissaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final PermissaoCatalogoService permissaoCatalogoService;
    private final PasswordEncoder passwordEncoder;

    private final boolean sincronizarCatalogoNaInicializacao;
    private final String adminLogin;
    private final String adminEmail;
    private final String adminSenha;
    private final String adminNome;
    private final boolean adminObrigatorio;

    public CargaInicialRunner(PerfilRepository perfilRepository,
                              PermissaoRepository permissaoRepository,
                              PerfilPermissaoRepository perfilPermissaoRepository,
                              UsuarioRepository usuarioRepository,
                              PermissaoCatalogoService permissaoCatalogoService,
                              PasswordEncoder passwordEncoder,
                              @Value("${rbac.sync-catalogo-na-inicializacao:true}") boolean sincronizarCatalogoNaInicializacao,
                              @Value("${app.admin.login:}") String adminLogin,
                              @Value("${app.admin.email:}") String adminEmail,
                              @Value("${app.admin.senha:}") String adminSenha,
                              @Value("${app.admin.nome:Administrador}") String adminNome,
                              @Value("${app.admin.obrigatorio:false}") boolean adminObrigatorio) {
        this.perfilRepository = perfilRepository;
        this.permissaoRepository = permissaoRepository;
        this.perfilPermissaoRepository = perfilPermissaoRepository;
        this.usuarioRepository = usuarioRepository;
        this.permissaoCatalogoService = permissaoCatalogoService;
        this.passwordEncoder = passwordEncoder;
        this.sincronizarCatalogoNaInicializacao = sincronizarCatalogoNaInicializacao;
        this.adminLogin = adminLogin;
        this.adminEmail = adminEmail;
        this.adminSenha = adminSenha;
        this.adminNome = adminNome;
        this.adminObrigatorio = adminObrigatorio;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Perfil admin = obterOuCriarPerfil("ADMIN", "Administrador",
            "Administrador do sistema. Recebe todas as permissões.", true);
        Perfil user = obterOuCriarPerfil("USER", "Usuário",
            "Usuário comum. É o perfil do auto-cadastro.", true);

        if (sincronizarCatalogoNaInicializacao) {
            permissaoCatalogoService.sincronizar();
        } else {
            log.info("Sincronização do catálogo de permissões na inicialização desativada (rbac.sync-catalogo-na-inicializacao=false).");
        }

        for (Permissao permissao : permissaoRepository.findAll()) {
            if (!permissao.isOrfa()) {
                vincular(admin, permissao);
                boolean isUserPerm = permissao.getCodigo().startsWith("CONTAS_")
                        || permissao.getCodigo().startsWith("CARTOES_")
                        || permissao.getCodigo().startsWith("RECEITAS_")
                        || permissao.getCodigo().startsWith("DESPESAS_")
                        || "DESPESA_RATEAR_MULTIUSUARIO".equals(permissao.getCodigo());
                if (isUserPerm && !permissao.isConcedivelPorPlano()) {
                    vincular(user, permissao);
                }
            }
        }

        criarAdminInicialSeNecessario(admin);
    }

    private Perfil obterOuCriarPerfil(String codigo, String nome, String descricao, boolean sistema) {
        return perfilRepository.findByCodigo(codigo).orElseGet(() -> {
            log.info("Carga inicial: criando perfil {}", codigo);
            return perfilRepository.save(new Perfil(codigo, nome, descricao, sistema));
        });
    }

    private void vincular(Perfil perfil, Permissao permissao) {
        if (!perfilPermissaoRepository.existsByPerfilAndPermissao(perfil, permissao)) {
            log.info("Carga inicial: vinculando {} -> {}", perfil.getCodigo(), permissao.getCodigo());
            perfilPermissaoRepository.save(new PerfilPermissao(perfil, permissao));
        }
    }

    private void criarAdminInicialSeNecessario(Perfil admin) {
        if (usuarioRepository.contarAdminsAtivos() > 0) {
            return;
        }
        if (adminLogin.isBlank() || adminEmail.isBlank() || adminSenha.isBlank()) {
            String situacao = "Nenhum usuário ADMIN ativo e app.admin.login/email/senha não configurados";
            if (adminObrigatorio) {
                throw new IllegalStateException(situacao
                    + ". Com app.admin.obrigatorio=true a aplicação não sobe sem um ADMIN.");
            }
            log.warn("{} — ADMIN inicial NÃO criado. Configure as três propriedades e reinicie.", situacao);
            return;
        }
        if (usuarioRepository.findByLoginOrEmail(adminLogin.trim(), adminEmail.trim()) != null) {
            String situacao = "Já existe usuário com o login ou e-mail de app.admin.* — ADMIN inicial não criado";
            if (adminObrigatorio) {
                throw new IllegalStateException(situacao
                    + ". Promova esse usuário ao perfil ADMIN ou ajuste app.admin.*.");
            }
            log.warn("{}. Promova esse usuário ao perfil ADMIN pela tela.", situacao);
            return;
        }
        Usuario usuario = new Usuario();
        usuario.setNome(adminNome);
        usuario.setGenero(Genero.OUTRO);
        usuario.setLogin(adminLogin.trim());
        usuario.setEmail(adminEmail.trim());
        usuario.setSenha(passwordEncoder.encode(adminSenha));
        usuario.setPerfil(admin);
        usuarioRepository.save(usuario);
        log.info("Carga inicial: usuário ADMIN '{}' criado.", adminLogin);
    }
}
