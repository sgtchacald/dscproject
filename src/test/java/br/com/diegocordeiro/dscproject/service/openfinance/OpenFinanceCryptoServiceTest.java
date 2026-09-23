package br.com.diegocordeiro.dscproject.service.openfinance;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OpenFinanceCryptoServiceTest {

    private final OpenFinanceCryptoService cryptoService = new OpenFinanceCryptoService("chave-secreta-para-testes-unitarios-256");

    @Test
    @DisplayName("Cifragem e decifragem de segredo produzem o texto plano original (AES-256-GCM)")
    void cifragemEDecifragem_sucesso() {
        String original = "minha-chave-super-secreta-pluggy-123";
        String cifrado = cryptoService.encrypt(original);

        assertThat(cifrado).isNotNull();
        assertThat(cifrado).isNotEqualTo(original);

        String decifrado = cryptoService.decrypt(cifrado);
        assertThat(decifrado).isEqualTo(original);
    }

    @Test
    @DisplayName("Cifrar o mesmo segredo duas vezes gera ciphertexts distintos devido ao IV aleatório de 12 bytes")
    void cifrar_duasVezes_geraCiphertextsDistintos() {
        String original = "mesmo-segredo-com-iv-distinto";
        String cifrado1 = cryptoService.encrypt(original);
        String cifrado2 = cryptoService.encrypt(original);

        assertThat(cifrado1).isNotEqualTo(cifrado2);

        assertThat(cryptoService.decrypt(cifrado1)).isEqualTo(original);
        assertThat(cryptoService.decrypt(cifrado2)).isEqualTo(original);
    }

    @Test
    @DisplayName("Decifrar com payload corrompido deve lançar exceção")
    void decifrar_payloadCorrompido_lancaExcecao() {
        String invalido = "dGV4dG9pbnZhbGlkbw=="; // base64 menor que 12 bytes ou corrompido
        assertThatThrownBy(() -> cryptoService.decrypt(invalido))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Mascarar deve retornar '********' para strings não vazias")
    void mascarar_retornaOitoAsteriscos() {
        assertThat(cryptoService.mascarar("segredo")).isEqualTo("********");
        assertThat(cryptoService.mascarar(null)).isEqualTo("");
        assertThat(cryptoService.mascarar("")).isEqualTo("");
    }
}
