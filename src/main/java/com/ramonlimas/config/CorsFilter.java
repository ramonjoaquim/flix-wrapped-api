package com.ramonlimas.config;

import com.ramonlimas.auth.AuthenticationInfo;
import com.ramonlimas.domain.dto.FlixWrappedResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.micronaut.context.annotation.Value;
import io.micronaut.http.HttpMethod;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Filter;
import io.micronaut.http.filter.HttpServerFilter;
import io.micronaut.http.filter.ServerFilterChain;
import io.reactivex.Flowable;
import jakarta.inject.Inject;
import org.reactivestreams.Publisher;

import java.util.Optional;

@Filter("/**")
public class CorsFilter implements HttpServerFilter {

    @Value("${frontend.url}")
    private String frontendUrl;

    @Inject
    private AuthenticationInfo authenticationInfo;

    @Override
    public Publisher<MutableHttpResponse<?>> doFilter(io.micronaut.http.HttpRequest<?> request, ServerFilterChain chain) {
        // Se for preflight CORS (OPTIONS), devolve 200 OK com headers
        if (request.getMethod().equals(HttpMethod.OPTIONS)) {
            MutableHttpResponse<?> response = HttpResponse.status(HttpStatus.OK);
            addCorsHeaders(response);
            return Flowable.just(response);
        }

        // Rotas públicas
        if (request.getPath().endsWith("/flix-wrapped/api/auth/google")) {
            return Flowable.fromPublisher(chain.proceed(request))
                    .map(response -> {
                        addCorsHeaders(response);
                        return response;
                    });
        }

        Optional<String> authHeader = request.getHeaders().get("Authorization", String.class);

        if (authHeader.isEmpty()) {
            return Flowable.just(FlixWrappedResponse.unauthorized("Token de autenticação ausente.").toMutableResponse());
        }

        String token = authHeader.get().replaceFirst("^Bearer ", "");

        try {
            Claims claims = authenticationInfo.parseJwt(token);
            // Se quiser, você pode armazenar os claims na request:
            // request.setAttribute("claims", claims);
            // Para as outras requisições, segue fluxo normal e depois adiciona os headers
            return Flowable.fromPublisher(chain.proceed(request))
                    .map(response -> {
                        addCorsHeaders(response);
                        return response;
                    });
        } catch (ExpiredJwtException e) {
            return Flowable.just(FlixWrappedResponse.unauthorized("Token expirado. Faça login novamente.").toMutableResponse());
        } catch (Exception e) {
            return Flowable.just(FlixWrappedResponse.unauthorized("Token inválido ou erro ao processar o token.").toMutableResponse());
        }
    }

    private void addCorsHeaders(MutableHttpResponse<?> response) {
        response.getHeaders().add("Access-Control-Allow-Origin", frontendUrl);
        response.getHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.getHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization, Accept");
    }
}
