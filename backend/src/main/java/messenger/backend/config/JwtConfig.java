package messenger.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import java.util.Base64;
import javax.crypto.spec.SecretKeySpec;

@Configuration
public class JwtConfig {
    private final String secretKey;

    public JwtConfig(@Value("${jwt.secret}") String secretKey) {
        this.secretKey = secretKey;
    }

    private SecretKeySpec secretKeySpec() {
        byte[] decodedKey = Base64.getDecoder().decode(secretKey);
        return new SecretKeySpec(decodedKey, "HmacSHA256");
    }

    @Bean
    public JwtEncoder jwtEncoder() {
        return NimbusJwtEncoder.withSecretKey(secretKeySpec()).build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withSecretKey(secretKeySpec()).build();
    }
}
