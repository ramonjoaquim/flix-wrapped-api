package com.ramonlimas.domain.dto;

import io.micronaut.serde.annotation.Serdeable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Serdeable
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserInfoResponseDTO {
    private String userId;
    private String name;
    private String picture;

}