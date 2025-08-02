package dev.horbatiuk.timecapsule.persistence.dto.capsule;

import dev.horbatiuk.timecapsule.persistence.entities.Attachment;
import dev.horbatiuk.timecapsule.persistence.entities.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CapsuleCreateDTO {
    private String title;
    private String description;
    private Timestamp openAt;
}

