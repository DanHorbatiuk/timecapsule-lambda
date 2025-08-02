package dev.horbatiuk.timecapsule.persistence.dto.security;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthenticationRequestDTO {
    private String email;
    private String password;
}