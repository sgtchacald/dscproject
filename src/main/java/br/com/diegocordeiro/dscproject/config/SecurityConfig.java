package br.com.diegocordeiro.dscproject.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    /** Rotas públicas — auto-cadastro, recuperação de senha, checagem em tempo real, estáticos. */
    private static final String[] PUBLICO_GET = {
        "/login",
        "/error",
        "/usuarios/existe",
        "/usuarios/cadastrar-site",
        "/usuarios/recuperar-senha",
        "/webjars/**", "/css/**", "/js/**", "/image/**",
        // Healthcheck do container (sem detalhes). O proxy bloqueia /actuator/** para a internet.
        "/actuator/health", "/actuator/health/**"
    };
    private static final String[] PUBLICO_POST = {
        "/usuarios/cadastrar-site",
        "/usuarios/recuperar-senha/solicitar",
        "/usuarios/recuperar-senha/confirmar",
        "/api/open-finance/webhook/**"
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, @Value("${app.remember-me.key}") String rememberMeKey) throws Exception {
        http
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/open-finance/webhook/**")
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.GET, PUBLICO_GET).permitAll()
                .requestMatchers(HttpMethod.POST, PUBLICO_POST).permitAll()

                // CRUD administrativo — cada operação exige a sua permissão
                .requestMatchers(HttpMethod.GET, "/usuarios/listar", "/usuarios/listar-dados")
                    .hasAuthority("PERM_USUARIOS_LISTAR")
                .requestMatchers(HttpMethod.GET, "/usuarios/buscar/**")
                    .hasAuthority("PERM_USUARIOS_EDITAR")
                .requestMatchers(HttpMethod.POST, "/usuarios/inserir")
                    .hasAuthority("PERM_USUARIOS_INSERIR")
                .requestMatchers(HttpMethod.PUT, "/usuarios/*/senha")
                    .hasAuthority("PERM_USUARIOS_EDITAR")
                .requestMatchers(HttpMethod.PUT, "/usuarios/editar/**")
                    .hasAuthority("PERM_USUARIOS_EDITAR")
                .requestMatchers(HttpMethod.DELETE, "/usuarios/excluir/**")
                    .hasAuthority("PERM_USUARIOS_EXCLUIR")
                .requestMatchers(HttpMethod.GET, "/usuarios/historico/**")
                    .hasAuthority("PERM_USUARIOS_VER_HISTORICO")

                // Perfis e Permissões — cada operação exige a sua permissão
                .requestMatchers(HttpMethod.GET, "/perfis/listar", "/perfis/listar-dados", "/permissoes/listar-dados")
                    .hasAuthority("PERM_PERFIS_LISTAR")
                .requestMatchers(HttpMethod.GET, "/perfis/buscar/**")
                    .hasAuthority("PERM_PERFIS_EDITAR")
                .requestMatchers(HttpMethod.POST, "/perfis/inserir")
                    .hasAuthority("PERM_PERFIS_INSERIR")
                .requestMatchers(HttpMethod.PUT, "/perfis/editar/**")
                    .hasAuthority("PERM_PERFIS_EDITAR")
                .requestMatchers(HttpMethod.DELETE, "/perfis/excluir/**")
                    .hasAuthority("PERM_PERFIS_EXCLUIR")
                .requestMatchers(HttpMethod.POST, "/permissoes/sincronizar-catalogo")
                    .hasAuthority("PERM_PERFIS_SINCRONIZAR_CATALOGO")

                // Parâmetros Globais — cada operação exige a sua permissão
                .requestMatchers(HttpMethod.GET, "/parametros/listar", "/parametros/listar-dados", "/parametros/historico/**")
                    .hasAuthority("PERM_PARAMETROS_LISTAR")
                .requestMatchers(HttpMethod.GET, "/parametros/buscar/**")
                    .hasAuthority("PERM_PARAMETROS_EDITAR")
                .requestMatchers(HttpMethod.PUT, "/parametros/editar/**", "/parametros/restaurar-padrao/**")
                    .hasAuthority("PERM_PARAMETROS_EDITAR")

                // Categorias — cada operação exige a sua permissão
                .requestMatchers(HttpMethod.GET, "/categorias/opcoes")
                    .authenticated()
                .requestMatchers(HttpMethod.GET, "/categorias/listar")
                    .hasAuthority("PERM_CATEGORIAS_LISTAR")
                .requestMatchers(HttpMethod.POST, "/categorias/inserir")
                    .hasAuthority("PERM_CATEGORIAS_INSERIR")
                .requestMatchers(HttpMethod.PUT, "/categorias/editar/**", "/categorias/desativar/**")
                    .hasAuthority("PERM_CATEGORIAS_EDITAR")
                .requestMatchers(HttpMethod.DELETE, "/categorias/excluir/**")
                    .hasAuthority("PERM_CATEGORIAS_EXCLUIR")

                // Categorias por Provedor — cada operação exige a sua permissão
                .requestMatchers(HttpMethod.GET, "/categorias-provedor/listar")
                    .hasAuthority("PERM_CATEGORIAS_PROVEDOR_LISTAR")
                .requestMatchers(HttpMethod.POST, "/categorias-provedor/inserir")
                    .hasAuthority("PERM_CATEGORIAS_PROVEDOR_INSERIR")
                .requestMatchers(HttpMethod.PUT, "/categorias-provedor/editar/**")
                    .hasAuthority("PERM_CATEGORIAS_PROVEDOR_EDITAR")
                .requestMatchers(HttpMethod.DELETE, "/categorias-provedor/excluir/**")
                    .hasAuthority("PERM_CATEGORIAS_PROVEDOR_EXCLUIR")

                // Instituições Financeiras — cada operação exige a sua permissão
                .requestMatchers(HttpMethod.GET, "/instituicoes-financeiras/opcoes")
                    .authenticated()
                .requestMatchers(HttpMethod.GET, "/instituicoes-financeiras/listar", "/instituicoes-financeiras/listar-dados")
                    .hasAuthority("PERM_INSTITUICOES_LISTAR")
                .requestMatchers(HttpMethod.GET, "/instituicoes-financeiras/buscar/**")
                    .hasAuthority("PERM_INSTITUICOES_EDITAR")
                .requestMatchers(HttpMethod.POST, "/instituicoes-financeiras/inserir")
                    .hasAuthority("PERM_INSTITUICOES_INSERIR")
                .requestMatchers(HttpMethod.PUT, "/instituicoes-financeiras/editar/**", "/instituicoes-financeiras/desativar/**")
                    .hasAuthority("PERM_INSTITUICOES_EDITAR")
                .requestMatchers(HttpMethod.DELETE, "/instituicoes-financeiras/excluir/**")
                    .hasAuthority("PERM_INSTITUICOES_EXCLUIR")

                // Instituições por Provedor — cada operação exige a sua permissão
                .requestMatchers(HttpMethod.GET, "/instituicoes-provedor/listar", "/instituicoes-provedor/listar-dados")
                    .hasAuthority("PERM_INSTITUICOES_PROVEDOR_LISTAR")
                .requestMatchers(HttpMethod.GET, "/instituicoes-provedor/buscar/**")
                    .hasAuthority("PERM_INSTITUICOES_PROVEDOR_EDITAR")
                .requestMatchers(HttpMethod.GET, "/instituicoes-provedor/provedores-opcoes")
                    .hasAnyAuthority("PERM_INSTITUICOES_PROVEDOR_LISTAR", "PERM_INSTITUICOES_PROVEDOR_INSERIR", "PERM_INSTITUICOES_PROVEDOR_EDITAR")
                .requestMatchers(HttpMethod.POST, "/instituicoes-provedor/inserir")
                    .hasAuthority("PERM_INSTITUICOES_PROVEDOR_INSERIR")
                .requestMatchers(HttpMethod.PUT, "/instituicoes-provedor/editar/**")
                    .hasAuthority("PERM_INSTITUICOES_PROVEDOR_EDITAR")
                .requestMatchers(HttpMethod.DELETE, "/instituicoes-provedor/excluir/**")
                    .hasAuthority("PERM_INSTITUICOES_PROVEDOR_EXCLUIR")

                // Contas do Usuário (Minhas Contas) — cada operação exige a sua permissão
                .requestMatchers(HttpMethod.GET, "/contas/opcoes")
                    .authenticated()
                .requestMatchers(HttpMethod.GET, "/contas/listar", "/contas/listar-dados")
                    .hasAuthority("PERM_CONTAS_LISTAR")
                .requestMatchers(HttpMethod.GET, "/contas/buscar/**")
                    .hasAuthority("PERM_CONTAS_EDITAR")
                .requestMatchers(HttpMethod.POST, "/contas/inserir")
                    .hasAuthority("PERM_CONTAS_INSERIR")
                .requestMatchers(HttpMethod.PUT, "/contas/editar/**")
                    .hasAuthority("PERM_CONTAS_EDITAR")
                .requestMatchers(HttpMethod.PUT, "/contas/desativar/**")
                    .hasAnyAuthority("PERM_CONTAS_DESATIVAR", "PERM_CONTAS_ATIVAR", "PERM_CONTAS_EDITAR")
                .requestMatchers(HttpMethod.PUT, "/contas/ajustar-saldo/**")
                    .hasAuthority("PERM_CONTAS_AJUSTAR_SALDO")
                .requestMatchers(HttpMethod.DELETE, "/contas/excluir/**")
                    .hasAuthority("PERM_CONTAS_EXCLUIR")

                // Cartões de Crédito (Meus Cartões) — cada operação exige a sua permissão
                .requestMatchers(HttpMethod.GET, "/cartoes/opcoes")
                    .authenticated()
                .requestMatchers(HttpMethod.GET, "/cartoes/listar", "/cartoes/listar-dados")
                    .hasAuthority("PERM_CARTOES_LISTAR")
                .requestMatchers(HttpMethod.GET, "/cartoes/buscar/**")
                    .hasAuthority("PERM_CARTOES_EDITAR")
                .requestMatchers(HttpMethod.POST, "/cartoes/inserir")
                    .hasAuthority("PERM_CARTOES_INSERIR")
                .requestMatchers(HttpMethod.PUT, "/cartoes/editar/**")
                    .hasAuthority("PERM_CARTOES_EDITAR")
                .requestMatchers(HttpMethod.DELETE, "/cartoes/excluir/**")
                    .hasAuthority("PERM_CARTOES_EXCLUIR")

                // Receitas (Finanças > Receitas) — cada operação exige a sua permissão
                .requestMatchers(HttpMethod.GET, "/receitas/listar", "/receitas/listar-dados")
                    .hasAuthority("PERM_RECEITAS_LISTAR")
                .requestMatchers(HttpMethod.GET, "/receitas/buscar/**")
                    .hasAuthority("PERM_RECEITAS_EDITAR")
                .requestMatchers(HttpMethod.POST, "/receitas/inserir", "/receitas/duplicar")
                    .hasAuthority("PERM_RECEITAS_INSERIR")
                .requestMatchers(HttpMethod.PUT, "/receitas/editar/**")
                    .hasAuthority("PERM_RECEITAS_EDITAR")
                .requestMatchers(HttpMethod.PUT, "/receitas/marcar-recebida/**")
                    .hasAuthority("PERM_RECEITAS_REGISTRAR_RECEBIMENTO")
                .requestMatchers(HttpMethod.DELETE, "/receitas/excluir/**")
                    .hasAuthority("PERM_RECEITAS_EXCLUIR")

                // Despesas (Finanças > Despesas) — cada operação exige a sua permissão
                .requestMatchers(HttpMethod.GET, "/despesas/listar", "/despesas/listar-dados")
                    .hasAuthority("PERM_DESPESAS_LISTAR")
                .requestMatchers(HttpMethod.GET, "/despesas/buscar/**")
                    .hasAuthority("PERM_DESPESAS_EDITAR")
                .requestMatchers(HttpMethod.POST, "/despesas/inserir", "/despesas/duplicar")
                    .hasAuthority("PERM_DESPESAS_INSERIR")
                .requestMatchers(HttpMethod.PUT, "/despesas/editar/**")
                    .hasAuthority("PERM_DESPESAS_EDITAR")
                .requestMatchers(HttpMethod.PATCH, "/despesas/*/valor", "/despesas/*/competencia", "/despesas/*/categoria", "/despesas/*/forma-pagamento", "/despesas/*/nome")
                    .hasAuthority("PERM_DESPESAS_EDITAR")
                .requestMatchers(HttpMethod.POST, "/despesas/registrar-pagamento-lote")
                    .hasAuthority("PERM_DESPESAS_PAGAR")
                .requestMatchers(HttpMethod.PUT, "/despesas/registrar-pagamento/**")
                    .hasAuthority("PERM_DESPESAS_PAGAR")
                .requestMatchers(HttpMethod.POST, "/despesas/importar-extrato")
                    .hasAuthority("PERM_DESPESAS_IMPORTAR")
                .requestMatchers(HttpMethod.DELETE, "/despesas/excluir/**")
                    .hasAuthority("PERM_DESPESAS_EXCLUIR")
                .requestMatchers(HttpMethod.PUT, "/despesas/*/rateio-acerto")
                    .hasAuthority("PERM_DESPESA_RATEAR_MULTIUSUARIO")
                .requestMatchers(HttpMethod.GET, "/despesas/usuarios-rateio", "/despesas/contatos-rateio")
                    .hasAuthority("PERM_DESPESA_RATEAR_MULTIUSUARIO")
                .requestMatchers(HttpMethod.POST, "/despesas/contatos-rapido")
                    .hasAuthority("PERM_DESPESA_RATEAR_MULTIUSUARIO")

                // Transações Bancárias (Finanças > Transações Bancárias)
                .requestMatchers(HttpMethod.GET, "/transacoes-bancarias/listar", "/transacoes-bancarias/listar-dados")
                    .hasAuthority("PERM_TRANSACOES_LISTAR")
                .requestMatchers(HttpMethod.GET, "/transacoes-bancarias/buscar/**")
                    .hasAuthority("PERM_TRANSACOES_EDITAR")
                .requestMatchers(HttpMethod.POST, "/transacoes-bancarias/inserir", "/transacoes-bancarias/duplicar/**")
                    .hasAuthority("PERM_TRANSACOES_INSERIR")
                .requestMatchers(HttpMethod.PUT, "/transacoes-bancarias/editar/**")
                    .hasAuthority("PERM_TRANSACOES_EDITAR")
                .requestMatchers(HttpMethod.POST, "/transacoes-bancarias/importar-ofx")
                    .hasAuthority("PERM_TRANSACOES_IMPORTAR")
                .requestMatchers(HttpMethod.DELETE, "/transacoes-bancarias/excluir/**")
                    .hasAuthority("PERM_TRANSACOES_EXCLUIR")

                // Faturas de Cartão (Finanças > Faturas de Cartão)
                .requestMatchers(HttpMethod.GET, "/faturas-cartao/listar", "/faturas-cartao/dados", "/faturas-cartao/*/compras")
                    .hasAuthority("PERM_FATURAS_LISTAR")
                .requestMatchers(HttpMethod.POST, "/faturas-cartao/*/fechar")
                    .hasAuthority("PERM_FATURAS_FECHAR")
                .requestMatchers(HttpMethod.POST, "/faturas-cartao/*/reabrir")
                    .hasAuthority("PERM_FATURAS_REABRIR")
                .requestMatchers(HttpMethod.POST, "/faturas-cartao/*/pagar")
                    .hasAuthority("PERM_FATURAS_PAGAR")
                .requestMatchers(HttpMethod.PUT, "/faturas-cartao/*/encargos")
                    .hasAuthority("PERM_FATURAS_EDITAR")

                // Open Finance (Finanças > Conexões Open Finance)
                .requestMatchers(HttpMethod.GET, "/open-finance/conexoes", "/open-finance/dados", "/open-finance/provedores/ativos", "/open-finance/conexoes/*/historico", "/open-finance/conexoes/*/contas")
                    .hasAuthority("PERM_OPEN_FINANCE_LISTAR")
                .requestMatchers(HttpMethod.POST, "/open-finance/connect-token", "/open-finance/conexoes/callback")
                    .hasAuthority("PERM_OPEN_FINANCE_CONECTAR")
                .requestMatchers(HttpMethod.PUT, "/open-finance/contas-externas/*/vincular")
                    .hasAuthority("PERM_OPEN_FINANCE_CONECTAR")
                .requestMatchers(HttpMethod.POST, "/open-finance/conexoes/*/sincronizar")
                    .hasAuthority("PERM_OPEN_FINANCE_SINCRONIZAR")
                .requestMatchers(HttpMethod.DELETE, "/open-finance/conexoes/*")
                    .hasAuthority("PERM_OPEN_FINANCE_DESCONECTAR")
                .requestMatchers(HttpMethod.GET, "/open-finance/credenciais")
                    .hasAuthority("PERM_OPEN_FINANCE_CONFIGURAR_CREDENCIAL")
                .requestMatchers(HttpMethod.POST, "/open-finance/credenciais", "/open-finance/credenciais/testar")
                    .hasAuthority("PERM_OPEN_FINANCE_CONFIGURAR_CREDENCIAL")

                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/", true)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .rememberMe(rm -> rm
                .key(rememberMeKey)
                .rememberMeParameter("lembrar")
                .tokenValiditySeconds(14 * 24 * 60 * 60)
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID", "remember-me")
                .permitAll()
            );
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
