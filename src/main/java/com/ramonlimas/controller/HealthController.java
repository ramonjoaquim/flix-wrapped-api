package com.ramonlimas.controller;

import com.ramonlimas.domain.dto.FlixWrappedResponse;
import com.ramonlimas.infrastructure.persistence.HistoryRepository;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;

@Controller("/flix-wrapped/api/health")
@Tag(name = "Health", description = "Endpoints para verificação de saúde da API")
public class HealthController {

    @Inject
    HistoryRepository repository;

    @Get(value = "/", consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Verificar saúde da API",
            description = "Este endpoint verifica se a API está ativa e se consegue se conectar ao MongoDB.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "API ativa e MongoDB conectado",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = FlixWrappedResponse.class))),
                    @ApiResponse(responseCode = "500", description = "Erro ao conectar ao MongoDB",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = FlixWrappedResponse.class)))
            }
    )
    public HttpResponse<FlixWrappedResponse<Object>> check() {
        try {
            repository.count();
            return FlixWrappedResponse.ok("API ativa e MongoDB conectado");
        } catch (Exception e) {
            return FlixWrappedResponse.serverError("API ativa, mas erro ao conectar no MongoDB: " + e.getMessage());
        }
    }
}
