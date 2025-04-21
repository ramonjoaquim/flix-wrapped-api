package com.ramonlimas;

import io.micronaut.runtime.Micronaut;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@OpenAPIDefinition(
        info = @Info(
                title = "flix-wrapped-service",
                version = "1.0",
                description = "Flix Wrapped"))
public class Application {

    public static void main(String[] args) {
        Micronaut.run(Application.class, args);
        log.info("Rapidoc Running: {}", "http://localhost:8080/rapidoc");
    }
}