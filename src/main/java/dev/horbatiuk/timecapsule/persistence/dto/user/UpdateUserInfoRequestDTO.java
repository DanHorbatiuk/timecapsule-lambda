package dev.horbatiuk.timecapsule.persistence.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserInfoRequestDTO {
    private String name;
    private String oldPassword;
    private String newPassword;
}

