package br.com.diegocordeiro.dscproject.service.usuario;

import br.com.diegocordeiro.dscproject.service.comum.EmailService;
import br.com.diegocordeiro.dscproject.model.usuario.Usuario;
import br.com.diegocordeiro.dscproject.model.usuario.UsuarioRecuperacaoSenha;
import br.com.diegocordeiro.dscproject.repository.usuario.UsuarioRecuperacaoSenhaRepository;
import br.com.diegocordeiro.dscproject.repository.usuario.UsuarioRepository;
import br.com.diegocordeiro.dscproject.service.exceptions.TokenRecuperacaoException;
import br.com.diegocordeiro.dscproject.util.TokenUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

/**
 * Fluxo de recuperação de senha por token.
 * O token nunca é persistido em claro; grava-se apenas o hash SHA-256.
 */
@Service
public class RecuperacaoSenhaService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioRecuperacaoSenhaRepository recuperacaoRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    private final int validadeMinutos;
    private final int maxTentativas;
    private final int maxSolicitacoesJanela;
    private final int janelaSolicitacoesMinutos;
    private final String baseUrl;

    public RecuperacaoSenhaService(UsuarioRepository usuarioRepository,
                                   UsuarioRecuperacaoSenhaRepository recuperacaoRepository,
                                   PasswordEncoder passwordEncoder,
                                   EmailService emailService,
                                   @Value("${usu.recup-senha.validade-min:30}") int validadeMinutos,
                                   @Value("${usu.recup-senha.max-tentativas:5}") int maxTentativas,
                                   @Value("${usu.recup-senha.max-solicitacoes:3}") int maxSolicitacoesJanela,
                                   @Value("${usu.recup-senha.janela-min:15}") int janelaSolicitacoesMinutos,
                                   @Value("${app.base-url:http://localhost:8080}") String baseUrl) {
        this.usuarioRepository = usuarioRepository;
        this.recuperacaoRepository = recuperacaoRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.validadeMinutos = validadeMinutos;
        this.maxTentativas = maxTentativas;
        this.maxSolicitacoesJanela = maxSolicitacoesJanela;
        this.janelaSolicitacoesMinutos = janelaSolicitacoesMinutos;
        this.baseUrl = baseUrl;
    }

    /** Sempre silencioso para o chamador: exista a conta ou não, a resposta é a mesma. */
    @Transactional
    public void solicitar(String email) {
        Optional<Usuario> encontrado = usuarioRepository.findByEmailAndDataExclusaoIsNull(email.trim());
        if (encontrado.isEmpty()) {
            return;   // e-mail não confere: nada a fazer, sem vazar a informação
        }
        Usuario usuario = encontrado.get();

        Instant limiteJanela = Instant.now().minus(janelaSolicitacoesMinutos, ChronoUnit.MINUTES);
        long recentes = recuperacaoRepository.countByUsuarioIdAndDataCriacaoAfter(usuario.getId(), limiteJanela);
        if (recentes >= maxSolicitacoesJanela) {
            return;   // limite de solicitações na janela atingido
        }

        List<UsuarioRecuperacaoSenha> pendentes =
            recuperacaoRepository.findByUsuarioIdAndUtilizadoFalseAndDataExclusaoIsNull(usuario.getId());
        pendentes.forEach(pendente -> pendente.setUtilizado(true));
        recuperacaoRepository.saveAll(pendentes);

        String token = TokenUtils.gerarToken();
        UsuarioRecuperacaoSenha registro = new UsuarioRecuperacaoSenha();
        registro.setUsuario(usuario);
        registro.setTokenHash(TokenUtils.hash(token));
        registro.setExpiraEm(Instant.now().plus(validadeMinutos, ChronoUnit.MINUTES));
        recuperacaoRepository.save(registro);

        String link = baseUrl + "/usuarios/recuperar-senha?token=" + token;
        emailService.enviarLinkRecuperacaoSenha(usuario.getEmail(), usuario.getNome(), link);
    }

    /** Valida o token e troca a senha. */
    @Transactional(noRollbackFor = TokenRecuperacaoException.class)
    public void confirmar(String token, String novaSenha) {
        UsuarioRecuperacaoSenha registro = recuperacaoRepository
            .findByTokenHashAndDataExclusaoIsNull(TokenUtils.hash(token))
            .orElseThrow(TokenRecuperacaoException::invalido);

        if (registro.isUtilizado()) {
            throw TokenRecuperacaoException.invalido();
        }

        registro.registrarTentativa();
        recuperacaoRepository.save(registro);

        if (registro.getTentativas() > maxTentativas) {
            throw TokenRecuperacaoException.tentativasExcedidas();
        }
        if (registro.expirado(Instant.now())) {
            throw TokenRecuperacaoException.expirado();
        }

        Usuario usuario = registro.getUsuario();
        usuario.setSenha(passwordEncoder.encode(novaSenha));
        usuarioRepository.save(usuario);

        registro.setUtilizado(true);   // token de uso único
        recuperacaoRepository.save(registro);
    }
}
