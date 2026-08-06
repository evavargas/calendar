package app.kairo.common;

import static org.assertj.core.api.Assertions.assertThat;

import app.kairo.config.KairoProperties;
import org.junit.jupiter.api.Test;

class TokenEncryptionServiceTest {

  @Test
  void roundTripsSecrets() {
    KairoProperties properties = new KairoProperties();
    properties.setTokenSecret("unit-test-secret-key-please-change");
    TokenEncryptionService service = new TokenEncryptionService(properties);

    String encrypted = service.encrypt("refresh-token-value");
    assertThat(encrypted).isNotEqualTo("refresh-token-value");
    assertThat(service.decrypt(encrypted)).isEqualTo("refresh-token-value");
  }
}
