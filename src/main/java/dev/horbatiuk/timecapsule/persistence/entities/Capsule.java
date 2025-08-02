package dev.horbatiuk.timecapsule.persistence.entities;

import dev.horbatiuk.timecapsule.persistence.entities.enums.CapsuleStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "capsule")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Capsule {

    @Id
    @GeneratedValue(generator = "UUID")
    private UUID id;

    @Column(unique = true, nullable = false)
    private String title;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private CapsuleStatus status = CapsuleStatus.INACTIVE;

    @Column(nullable = false)
    private Timestamp createdAt;

    @Column(nullable = false)
    private Timestamp openAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "app_user_id", referencedColumnName = "id")
    private User appUser;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "capsule")
    private List<Attachment> attachments;

}
