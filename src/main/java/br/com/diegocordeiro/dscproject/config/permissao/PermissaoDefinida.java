package br.com.diegocordeiro.dscproject.config.permissao;

import br.com.diegocordeiro.dscproject.config.catalogo.ItemCatalogo;

/**
 * Entrada do catálogo de permissões definido no código. Cada enum de módulo
 * (Usuários, Perfis e Permissões, …) implementa esta interface; o sincronizador
 * reflete estas entradas na tabela {@code PERMISSOES}. Cada uma vira a authority
 * {@code PERM_{CODIGO}}.
 */
public interface PermissaoDefinida extends ItemCatalogo {

    String PREFIXO_AUTHORITY = "PERM_";

    default String getAuthority() {
        return PREFIXO_AUTHORITY + getCodigo();
    }

    default boolean isConcedivelPorPlano() {
        return false;
    }
}
