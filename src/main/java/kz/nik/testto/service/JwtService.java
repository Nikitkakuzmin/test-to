package kz.nik.testto.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;


import java.util.Base64;
import java.util.Date;


@Service
@Slf4j
public class JwtService {

    private final byte[] secretKey;

    public JwtService(@Value("${jwt.secret}") String secret) {
        this.secretKey = Base64.getDecoder().decode(secret);
    }

    public String generateToken(UserDetails userDetails) {
        log.info("Generating JWT token for user: {}", userDetails.getUsername());
        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
                .signWith(Keys.hmacShaKeyFor(secretKey), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        log.info("Validating token for user: {}", userDetails.getUsername());
        String username = extractUsername(token);

        if (!username.equals(userDetails.getUsername())) {
            log.warn("Token validation failed: username mismatch");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Недействительный токен");
        }

        if (isTokenExpired(token)) {
            log.warn("Token has expired");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Срок действия токена истек");
        }

        log.info("Token validation successful");
        return true;
    }

    public String extractUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(secretKey))
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return Jwts.parserBuilder().setSigningKey(secretKey).build()
                .parseClaimsJws(token).getBody().getExpiration();
    }
}
