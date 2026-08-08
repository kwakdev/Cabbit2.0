package ca.cabbit.fieldservice.security;

import ca.cabbit.fieldservice.AppProperties;
import ca.cabbit.fieldservice.domain.AppUser;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class TokenService {
    private final SecretKey key;
    private final AppProperties properties;
    public TokenService(AppProperties properties) {
        this.properties = properties;
        String secret = Objects.requireNonNull(properties.jwtSecret(), "APP_JWT_SECRET is required");
        if (secret.length() < 32) throw new IllegalStateException("APP_JWT_SECRET must contain at least 32 characters");
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
    public String accessToken(AppUser user) {
        Instant now = Instant.now();
        return Jwts.builder().subject(user.id.toString()).claim("companyId", user.companyId.toString())
                .claim("email", user.email).claim("name", user.displayName).claim("role", user.role.name())
                .issuedAt(Date.from(now)).expiration(Date.from(now.plus(properties.accessTokenMinutes(), ChronoUnit.MINUTES)))
                .signWith(key).compact();
    }
    public UserPrincipal parse(String token) {
        Claims c = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
        return new UserPrincipal(UUID.fromString(c.getSubject()), UUID.fromString(c.get("companyId", String.class)),
                c.get("email", String.class), c.get("name", String.class), ca.cabbit.fieldservice.domain.Role.valueOf(c.get("role", String.class)));
    }
    public long accessExpiresInSeconds() { return properties.accessTokenMinutes() * 60; }
}
