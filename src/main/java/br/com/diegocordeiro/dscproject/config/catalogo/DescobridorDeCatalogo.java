package br.com.diegocordeiro.dscproject.config.catalogo;

import br.com.diegocordeiro.dscproject.config.permissao.PermissaoDefinida;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Varre um pacote e devolve todas as constantes dos {@code enum}s que implementam
 * um tipo de catálogo (ex.: {@link PermissaoDefinida}).
 * Substitui o agregador estático escrito à mão: adicionar um módulo é só criar o
 * enum dele no pacote — nada mais precisa ser editado.
 */
public final class DescobridorDeCatalogo {

    private DescobridorDeCatalogo() {
    }

    public static <T> List<T> noPacote(String pacote, Class<T> tipo) {
        var scanner = new ClassPathScanningCandidateComponentProvider(false) {
            @Override
            protected boolean isCandidateComponent(org.springframework.beans.factory.annotation.AnnotatedBeanDefinition beanDefinition) {
                return beanDefinition.getMetadata().isIndependent() && !beanDefinition.getMetadata().isAbstract();
            }
        };
        scanner.addIncludeFilter(new AssignableTypeFilter(tipo));

        return scanner.findCandidateComponents(pacote).stream()
            .map(bd -> carregar(bd.getBeanClassName()))
            .filter(Class::isEnum)
            .flatMap(enumClasse -> Arrays.stream(enumClasse.getEnumConstants()))
            .map(tipo::cast)
            .sorted(Comparator
                .comparing((T item) -> ((Enum<?>) item).getDeclaringClass().getName())
                .thenComparingInt(item -> ((Enum<?>) item).ordinal()))
            .toList();
    }

    private static Class<?> carregar(String nomeQualificado) {
        try {
            return Class.forName(nomeQualificado);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Enum de catálogo encontrado na varredura mas não carregável: " + nomeQualificado, e);
        }
    }
}
