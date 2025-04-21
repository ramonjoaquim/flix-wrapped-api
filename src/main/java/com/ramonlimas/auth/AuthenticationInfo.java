package com.ramonlimas.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.SignatureException;
import io.micronaut.context.annotation.Value;
import jakarta.inject.Singleton;

import java.nio.charset.StandardCharsets;
import java.security.Key;

@Singleton
public class AuthenticationInfo {

    @Value("${auth.secretKey}")
    private String secretKey;

    public Claims parseJwt(String token) throws Exception {
        try {
            Key key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));

            return Jwts.parser()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (SignatureException e) {
            throw new Exception("Assinatura inválida", e);
        } catch (ExpiredJwtException e) {
            throw new Exception("Token expirado", e);
        } catch (Exception e) {
            throw new Exception("Erro ao processar o token", e);
        }
    }
}

