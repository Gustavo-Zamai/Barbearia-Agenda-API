package com.gsz.agenda.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

@Component
@Slf4j
public class JwtTokenProvider {

    @Value("${jwt.secret:chave-super-secreta-para-jwt-deve-ter-pelo-menos-32-caracteres-para-seguranca}")
    private String jwtSecret;

    @Value("${jwt.expiration:86400000}")
    private Long jwtExpiration;

    private SecretKey secretKey;

    @PostConstruct
    protected void init() {
        // Garantir que a chave tenha pelo menos 32 caracteres
        if (jwtSecret.length() < 32) {
            log.warn("JWT secret é muito curto (menos de 32 caracteres). Recomenda-se usar uma chave mais segura.");
        }
        this.secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Gerar token JWT
     */
    public String gerarToken(Authentication authentication) {
        String username = authentication.getName();
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        return Jwts.builder()
                .setSubject(username)
                .claim("roles", authorities)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    /**
     * Obter username do token
     */
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.getSubject();
    }

    /**
     * Obter roles do token
     */
    public Collection<SimpleGrantedAuthority> getRolesFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        String roles = claims.get("roles", String.class);
        
        if (roles == null || roles.isEmpty()) {
            return Arrays.asList(new SimpleGrantedAuthority("ROLE_CLIENTE"));
        }

        return Arrays.stream(roles.split(","))
                .map(role -> new SimpleGrantedAuthority(role.trim()))
                .collect(Collectors.toList());
    }

    /**
     * Validar token
     */
    public boolean validarToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.warn("Token JWT inválido: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.warn("Token JWT expirado: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("Token JWT não suportado: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("Token JWT vazio: {}", e.getMessage());
        }
        return false;
    }

    /**
     * Obter expiração do token
     */
    public Long getExpiracaoToken() {
        return jwtExpiration;
    }

    /**
     * Criar Authentication a partir do token
     */
    public Authentication getAuthentication(String token) {
        String username = getUsernameFromToken(token);
        Collection<SimpleGrantedAuthority> authorities = getRolesFromToken(token);

        UserDetails userDetails = User.builder()
                .username(username)
                .password("")
                .authorities(authorities)
                .build();

        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }
}