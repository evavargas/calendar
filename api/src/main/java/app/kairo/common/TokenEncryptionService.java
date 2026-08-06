package app.kairo.common;

import app.kairo.config.KairoProperties;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Service;

@Service
public class TokenEncryptionService {

  private static final String TRANSFORMATION = "AES/GCM/NoPadding";
  private static final int GCM_TAG_BITS = 128;
  private static final int IV_BYTES = 12;

  private final SecretKeySpec key;
  private final SecureRandom secureRandom = new SecureRandom();

  public TokenEncryptionService(KairoProperties properties) {
    this.key = new SecretKeySpec(deriveKey(properties.getTokenSecret()), "AES");
  }

  public String encrypt(String plaintext) {
    try {
      byte[] iv = new byte[IV_BYTES];
      secureRandom.nextBytes(iv);
      Cipher cipher = Cipher.getInstance(TRANSFORMATION);
      cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_BITS, iv));
      byte[] cipherBytes = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
      ByteBuffer buffer = ByteBuffer.allocate(iv.length + cipherBytes.length);
      buffer.put(iv);
      buffer.put(cipherBytes);
      return Base64.getEncoder().encodeToString(buffer.array());
    } catch (Exception ex) {
      throw new IllegalStateException("No se pudo cifrar el token", ex);
    }
  }

  public String decrypt(String payload) {
    try {
      byte[] decoded = Base64.getDecoder().decode(payload);
      ByteBuffer buffer = ByteBuffer.wrap(decoded);
      byte[] iv = new byte[IV_BYTES];
      buffer.get(iv);
      byte[] cipherBytes = new byte[buffer.remaining()];
      buffer.get(cipherBytes);
      Cipher cipher = Cipher.getInstance(TRANSFORMATION);
      cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_BITS, iv));
      return new String(cipher.doFinal(cipherBytes), StandardCharsets.UTF_8);
    } catch (Exception ex) {
      throw new IllegalStateException("No se pudo descifrar el token", ex);
    }
  }

  private static byte[] deriveKey(String secret) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      return digest.digest(secret.getBytes(StandardCharsets.UTF_8));
    } catch (Exception ex) {
      throw new IllegalStateException("No se pudo derivar la clave", ex);
    }
  }
}
