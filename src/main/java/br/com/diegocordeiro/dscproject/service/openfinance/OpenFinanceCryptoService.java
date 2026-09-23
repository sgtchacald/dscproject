package br.com.diegocordeiro.dscproject.service.openfinance;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

@Slf4j
@Service
public class OpenFinanceCryptoService {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int TAG_LENGTH_BIT = 128;
    private static final int IV_LENGTH_BYTE = 12;

    private final SecretKeySpec secretKey;

    public OpenFinanceCryptoService(@Value("${app.openfinance.encryption-key:dscproject-openfinance-aes-key-dev}") String key) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] keyBytes = md.digest(key.getBytes(StandardCharsets.UTF_8));
            this.secretKey = new SecretKeySpec(keyBytes, "AES");
        } catch (Exception e) {
            throw new IllegalStateException("Erro ao inicializar chave criptografica para Open Finance", e);
        }
    }

    public String cifrar(String plainText) {
        if (plainText == null || plainText.isBlank()) {
            return plainText;
        }
        try {
            byte[] iv = new byte[IV_LENGTH_BYTE];
            SecureRandom random = new SecureRandom();
            random.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);

            byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + cipherText.length);
            byteBuffer.put(iv);
            byteBuffer.put(cipherText);

            return Base64.getEncoder().encodeToString(byteBuffer.array());
        } catch (Exception e) {
            log.error("Erro ao cifrar segredo Open Finance", e);
            throw new RuntimeException("Falha na criptografia de credencial Open Finance", e);
        }
    }

    public String encrypt(String plainText) {
        return cifrar(plainText);
    }

    public String decrypt(String cipherTextBase64) {
        return decifrar(cipherTextBase64);
    }

    public String decifrar(String cipherTextBase64) {
        if (cipherTextBase64 == null || cipherTextBase64.isBlank()) {
            return cipherTextBase64;
        }
        try {
            byte[] cipherBytes = Base64.getDecoder().decode(cipherTextBase64);

            if (cipherBytes.length < IV_LENGTH_BYTE) {
                throw new IllegalArgumentException("Texto cifrado invalido para decifragem Open Finance");
            }

            byte[] iv = new byte[IV_LENGTH_BYTE];
            byte[] cipherText = new byte[cipherBytes.length - IV_LENGTH_BYTE];

            ByteBuffer byteBuffer = ByteBuffer.wrap(cipherBytes);
            byteBuffer.get(iv);
            byteBuffer.get(cipherText);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);

            byte[] plainBytes = cipher.doFinal(cipherText);
            return new String(plainBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Erro ao decifrar segredo Open Finance", e);
            throw new RuntimeException("Falha na decifragem de credencial Open Finance", e);
        }
    }

    public String mascarar(String secret) {
        if (secret == null || secret.isBlank()) {
            return "";
        }
        return "********";
    }
}
