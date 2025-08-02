package dev.horbatiuk.timecapsule.persistence.entities;

import dev.horbatiuk.timecapsule.persistence.entities.enums.AttachmentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "attachments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attachment {

    @Id
    @GeneratedValue(generator = "UUID")
    private UUID id;

    @Column(nullable = false)
    private String filename;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String fileKey;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private AttachmentStatus status = AttachmentStatus.NOT_IN_USE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "capsule_id")
    private Capsule capsule;

}
