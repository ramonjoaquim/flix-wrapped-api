package com.ramonlimas.domain.dto;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.serde.annotation.Serdeable;
import lombok.Data;
@Serdeable
@Data
public class FlixWrappedResponse<T> {
    private ApiResponseStatus status;
    private String message;
    private T data;

    // Construtor padrão
    public FlixWrappedResponse() {}

    // Construtor completo
    public FlixWrappedResponse(ApiResponseStatus status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public FlixWrappedResponse(ApiResponseStatus status, String message) {
        this.status = status;
        this.message = message;
        this.data = null;
    }

    public enum ApiResponseStatus {
        SUCCESS,
        ERROR
    }

    // Método estático para sucesso (200 OK)
    public static <T> HttpResponse<FlixWrappedResponse<T>> ok(String message, T data) {
        return HttpResponse.ok(new FlixWrappedResponse<>(ApiResponseStatus.SUCCESS, message, data));
    }

    // Método estático para sucesso (200 OK)
    public static <T> HttpResponse<FlixWrappedResponse<T>> ok(T data) {
        return HttpResponse.ok(new FlixWrappedResponse<>(ApiResponseStatus.SUCCESS, null, data));
    }

    // Método estático para erro no servidor (500)
    public static <T> HttpResponse<FlixWrappedResponse<T>> serverError(String message, T data) {
        return HttpResponse.serverError(new FlixWrappedResponse<>(ApiResponseStatus.ERROR, message, data));
    }

    // Versões sem `data` para respostas simples
    public static HttpResponse<FlixWrappedResponse<Object>> ok(String message) {
        return HttpResponse.ok(new FlixWrappedResponse<>(ApiResponseStatus.SUCCESS, message, null));
    }

    public static HttpResponse<FlixWrappedResponse<Object>> serverError(String message) {
        return HttpResponse.serverError(new FlixWrappedResponse<>(ApiResponseStatus.ERROR, message, null));
    }

    public static HttpResponse<FlixWrappedResponse<?>> notFound() {
        return HttpResponse.notFound();
    }

    public static HttpResponse<Object> unauthorized(String message) {
        return HttpResponse.unauthorized().body(new FlixWrappedResponse<>(ApiResponseStatus.ERROR, message, null));
    }

    public static HttpResponse<Object> unauthorized() {
        return HttpResponse.unauthorized().body(new FlixWrappedResponse<>(ApiResponseStatus.ERROR, null));
    }
}
