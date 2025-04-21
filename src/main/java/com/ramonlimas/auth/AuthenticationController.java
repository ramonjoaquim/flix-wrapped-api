package com.ramonlimas.auth;


import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.ramonlimas.domain.dto.FlixWrappedResponse;
import com.ramonlimas.domain.dto.UserInfoResponseDTO;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.micronaut.context.annotation.Value;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Body;
import io.jsonwebtoken.security.Keys;
import jakarta.inject.Inject;

import java.nio.charset.StandardCharsets;

import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

@Controller("/flix-wrapped/api/auth")
public class AuthenticationController {

    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    @Value("${auth.clientId}")
    private String clientId;

    @Value("${auth.secretKey}")
    private String secretKey;

    @Inject
    private AuthenticationInfo authenticationInfo;

    @Post("/google")
    public HttpResponse<?> authenticateWithGoogle(@Body Map<String, String> body) {
        String token = body.get("token");

        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY)
                    .setAudience(Collections.singletonList(clientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(token);
            if (idToken != null) {
                GoogleIdToken.Payload payload = idToken.getPayload();
                String userId = payload.getSubject();
                String email = payload.getEmail();
                String name = (String) payload.get("name");
                String picture = (String) payload.get("picture");

                // Gerar o JWT para autenticação no backend
                String jwtToken = Jwts.builder()
                        .subject(userId)  // Armazena o userId no JWT
                        .claim("email", email)
                        .claim("name", name)
                        .claim("picture", picture)
                        .issuedAt(new Date())
                        .expiration(new Date(System.currentTimeMillis() + 3600000)) // Expira em 1 hora
                        .signWith(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)))
                        .compact();

                // Retorne o JWT para o frontend
                return HttpResponse.ok(Map.of(
                        "message", "Autenticação bem-sucedida",
                        "token", jwtToken // Retorna o JWT gerado
                ));
            } else {
                return HttpResponse.unauthorized().body(Map.of(
                        "message", "Token inválido"
                ));
            }
        } catch (Exception e) {
            return HttpResponse.serverError(Map.of(
                    "message", "Erro ao validar token",
                    "error", e.getMessage()
            ));
        }
    }

    @Get("/me")
    public HttpResponse<?> getUserInfo(HttpRequest<?> request) throws Exception {
        Optional<String> authHeader = request.getHeaders().get("Authorization", String.class);

        if (authHeader.isEmpty() || !authHeader.get().startsWith("Bearer ")) {
            return FlixWrappedResponse.unauthorized("Token ausente ou inválido");
        }

        String token = authHeader.get().replace("Bearer ", "");

        Claims claims = authenticationInfo.parseJwt(token);

        UserInfoResponseDTO userInfo = new UserInfoResponseDTO(
                claims.getSubject(),
                claims.get("name", String.class),
                claims.get("picture", String.class)
        );

        return HttpResponse.ok(userInfo);
    }
}
