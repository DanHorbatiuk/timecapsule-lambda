package dev.horbatiuk.timecapsule.persistence.dto.capsule;

import dev.horbatiuk.timecapsule.persistence.entities.enums.CapsuleStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CapsuleListItemDTO {
    private UUID id;
    private String title;
    private CapsuleStatus status;
    private Timestamp openAt;
}
