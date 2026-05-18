package com.hostelfinder.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Service
public class JwtTokenService {
  private final Key key;
  private final long expMinutes;

  public JwtTokenService(
    @Value("${app.jwt.secret}") String secret,
    @Value("${app.jwt.expMinutes}") long expMinutes
  ) {
    // HS256 requires a sufficiently long secret; for college demo keep it simple.
    this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    this.expMinutes = expMinutes;
  }

  public String createAccessToken(Long userId, String role) {
    Instant now = Instant.now();
    Instant exp = now.plusSeconds(expMinutes * 60);

    return Jwts.builder()
      .subject(String.valueOf(userId))
      .claims(Map.of("role", role))
      .issuedAt(Date.from(now))
      .expiration(Date.from(exp))
      .signWith(key)
      .compact();
  }

  public JwtUser parse(String token) {
    var claims = Jwts.parser()
      .verifyWith((javax.crypto.SecretKey) key)
      .build()
      .parseSignedClaims(token)
      .getPayload();

    Long userId = Long.valueOf(claims.getSubject());
    String role = claims.get("role", String.class);
    return new JwtUser(userId, role);
  }
}

