package com.ramonlimas.controller;

import com.ramonlimas.auth.AuthenticationInfo;
import com.ramonlimas.domain.dto.FlixWrappedResponse;
import com.ramonlimas.domain.dto.HistoryDTO;
import com.ramonlimas.domain.dto.HistoryProcessedDTO;
import com.ramonlimas.domain.dto.PaginatedResponse;
import com.ramonlimas.domain.model.HistoryProcessedEntity;
import com.ramonlimas.infrastructure.persistence.HistoryProcessedRepository;
import com.ramonlimas.infrastructure.persistence.HistoryRepository;
import com.ramonlimas.processor.RawHistoryProcessor;
import com.ramonlimas.service.history.HistoryProcessedService;
import io.jsonwebtoken.Claims;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.model.Sort;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Optional;


@Controller("/flix-wrapped/api/user-history")
@Tag(name = "User History", description = "Endpoints para acessar o histórico do usuário")
public class UserHistoryController {

    @Inject
    private HistoryRepository repository;

    @Inject
    private HistoryProcessedRepository historyProcessedRepository;

    @Inject
    private HistoryProcessedService historyProcessedService;

    @Inject
    private RawHistoryProcessor rawHistoryProcessor;

    @Inject
    private AuthenticationInfo authenticationInfo;

    @Get("raw")
    @Operation(
            summary = "Obter histórico bruto do usuário",
            description = "Retorna o histórico bruto de visualizações com base no email do usuário.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Histórico bruto retornado com sucesso",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = PaginatedResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Erro nos parâmetros fornecidos",
                            content = @Content(mediaType = "application/json")
                    )
            }
    )
    public PaginatedResponse<HistoryDTO> getRawHistory(@QueryValue String userId,
                                                       @QueryValue(defaultValue = "0") int offset,
                                                       @QueryValue(defaultValue = "10") int limit,
                                                       @QueryValue(defaultValue = "date") String sort,
                                                       @QueryValue(defaultValue = "DESC") String direction) {
        // Validação dos campos de ordenação permitidos
        String sortField = switch (sort) {
            case "date", "title" -> sort;
            default -> "date"; // fallback
        };

        Sort.Order.Direction sortDirection = "ASC".equalsIgnoreCase(direction)
                ? Sort.Order.Direction.ASC
                : Sort.Order.Direction.DESC;

        Sort.Order order = new Sort.Order(sortField, sortDirection, true);
        Pageable pageable = Pageable.from(offset / limit, limit, Sort.of(order));


        List<HistoryDTO> items = repository.findByUserId(userId, pageable)
                .stream()
                .map(e -> new HistoryDTO(e.getTitle(), e.getDate()))
                .toList();

        long total = repository.countByUserId(userId);
        return new PaginatedResponse<>(items, total, offset, limit);
    }

    @Get("processed/{userId}")
    @Operation(
            summary = "Obter histórico processado do usuário",
            description = "Retorna o histórico processado de visualizações com base no userId do usuário.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Histórico processado retornado com sucesso",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = PaginatedResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "userId não encontrado ou sem histórico processado",
                            content = @Content(mediaType = "application/json")
                    )
            }
    )
    public PaginatedResponse<HistoryProcessedDTO> getProcessedHistory(@PathVariable String userId,
                                                                      @QueryValue(defaultValue = "0") int offset,
                                                                      @QueryValue(defaultValue = "10") int limit,
                                                                      @QueryValue(defaultValue = "") String filterTitle) {
        // Consultar MongoDB com filtro e ordenação
        List<HistoryProcessedDTO> items = historyProcessedService.findByUserIdAndTitleRegex(userId, filterTitle, offset, limit)
                .stream()
                .map(HistoryProcessedEntity::getData) // Extrai o DTO
                .toList();

        long totalItems = historyProcessedService.countByUserIdAndTitleRegex(userId, filterTitle);

        // Retornar resultados paginados
        return new PaginatedResponse<>(items, totalItems, offset, limit);
    }

    @Delete
    public HttpResponse<?> deleteDataUser(HttpRequest<?> request) {
        Optional<String> authHeader = request.getHeaders().get("Authorization", String.class);

        if (authHeader.isEmpty() || !authHeader.get().startsWith("Bearer ")) {
            return HttpResponse.unauthorized();
        }

        String token = authHeader.get().replace("Bearer ", "");

        try {
            Claims claims = authenticationInfo.parseJwt(token);

            String userId = claims.getSubject();
            repository.deleteByUserId(userId);
            historyProcessedRepository.deleteByUserId(userId);

            return FlixWrappedResponse.ok("Dados removidos com sucesso");
        } catch (Exception e) {
            return FlixWrappedResponse.unauthorized();
        }
    }

}
