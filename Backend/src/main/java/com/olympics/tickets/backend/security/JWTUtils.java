package com.olympics.tickets.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import com.olympics.tickets.backend.entity.OurUsers;

import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

@Component
public class JWTUtils {

    private static final String SECRET_KEY = "votre_cle_secrete_tres_securisee_au_moins_32_caracteres";
    private static final long ACCESS_TOKEN_EXPIRATION = 86400000; // 24 heures
    private static final long REFRESH_TOKEN_EXPIRATION = 604800000; // 7 jours

    // Méthode pour extraire le username (email) du token
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Méthode générique pour extraire une claim
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // Génération de token avec type
    public String generateToken(Map<String, Object> claims, OurUsers user, String tokenType) {
        long expiration = tokenType.equalsIgnoreCase("REFRESH")
                ? REFRESH_TOKEN_EXPIRATION
                : ACCESS_TOKEN_EXPIRATION;

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getEmail())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Version simplifiée sans type de token
    public String generateToken(Map<String, Object> claims, OurUsers user) {
        return generateToken(claims, user, "ACCESS");
    }

    // Validation du token
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}