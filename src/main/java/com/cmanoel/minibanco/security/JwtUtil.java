package com.cmanoel.minibanco.security;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    @Value("${jwt.refresh-expiration:1209600000}")
    private Long refreshExpiration;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    private String gerarToken(String email, Long tokenExpiration, String type) {
        return Jwts.builder()
            .setSubject(email)
            .addClaims(Map.of("type", type))
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + tokenExpiration))
            .signWith(getSigningKey(), SignatureAlgorithm.HS256)
            .compact();
    }

    public String gerarAccessToken(String email) {
        return gerarToken(email, expiration, "access");
    }

    public String gerarRefreshToken(String email) {
        return gerarToken(email, refreshExpiration, "refresh");
    }

    public String extrairEmail(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(getSigningKey())
            .build()
            .parseClaimsJws(token)
            .getBody()
            .getSubject();
    }

    public String extrairTipo(String token) {
        Object type = Jwts.parserBuilder()
            .setSigningKey(getSigningKey())
            .build()
            .parseClaimsJws(token)
            .getBody()
            .get("type");
        return type != null ? type.toString() : "";
    }

    public long extrairExpiracao(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(getSigningKey())
            .build()
            .parseClaimsJws(token)
            .getBody()
            .getExpiration()
            .getTime();
    }

    public boolean isAccessToken(String token) {
        return "access".equals(extrairTipo(token));
    }

    public boolean isRefreshToken(String token) {
        return "refresh".equals(extrairTipo(token));
    }
}
