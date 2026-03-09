package com.marymar.app.configuration.Security;

import com.marymar.app.persistence.Entity.Persona;
import com.marymar.app.persistence.Entity.Rol;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration:86400000}")
    private long expirationMs;

    // =========================================
    // 🔎 EXTRACCIÓN
    // =========================================

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Rol extractRole(String token) {
        String roleString = extractClaim(token, claims -> (String) claims.get("role"));
        return Rol.valueOf(roleString);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        final Claims claims = extractAllClaims(token);
        return resolver.apply(claims);
    }

    // =========================================
    // 🔐 GENERACIÓN
    // =========================================

    public String generateToken(Persona persona) {

        Map<String, Object> claims = new HashMap<>();
        claims.put("role", persona.getRol().name());

        return buildToken(claims, persona.getEmail());
    }

    private String buildToken(Map<String, Object> extraClaims, String username) {

        long now = System.currentTimeMillis();

        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(username)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + expirationMs))
                .signWith(getSignInKey())
                .compact();
    }

    // =========================================
    // ✅ VALIDACIÓN
    // =========================================

    public boolean isTokenValid(String token, UserDetails userDetails) {

        final String username = extractUsername(token);

        return username.equals(userDetails.getUsername())
                && !isTokenExpired(token);
    }

        public boolean isTokenExpired(String token) {
            try {
                return extractExpiration(token).before(new Date());
            } catch (ExpiredJwtException e) {
                return true;
            }
        }
    // =========================================
    // 🔧 INTERNOS
    // =========================================

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSignInKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public void validateToken(String token) {
        extractAllClaims(token);
    }
}

