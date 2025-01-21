package com.example.api_gateway.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;

@Service
public class JwtUtils {

  private static final String TOKEN = "asjkdhfasncvaeioadfweqoweiqrksdfajfqwejdfasnfarurjewqwdmfnasmfnvcjkjansdf";

  private final Key key;

  public JwtUtils() {
    this.key = Keys.hmacShaKeyFor(TOKEN.getBytes(StandardCharsets.UTF_8));
  }

  public Claims getClaimsFromToken(String token) {
    return Jwts.parser()
      .verifyWith((SecretKey) this.key)
      .build()
      .parseSignedClaims(token)
      .getPayload();
  }

  public boolean isExpired(String token) {
    try {
      return getClaimsFromToken(token).getExpiration().toInstant().isBefore(Instant.now());
    } catch (Exception e) {
      System.out.println(e.getMessage());
      return true;
    }
  }

  public Integer getUserIdFromToken(String token) {
    try {
      return Integer.parseInt(getClaimsFromToken(token).getSubject());
    } catch (Exception e) {
      return null;
    }
  }
}
