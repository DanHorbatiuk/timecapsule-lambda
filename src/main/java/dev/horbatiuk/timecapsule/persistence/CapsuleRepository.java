package dev.horbatiuk.timecapsule.persistence;

import dev.horbatiuk.timecapsule.persistence.entities.Capsule;
import dev.horbatiuk.timecapsule.persistence.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CapsuleRepository extends JpaRepository<Capsule, UUID> {
    List<Capsule> findAllByAppUser(User user);
    List<Capsule> findAllByAppUser_Id(UUID appUserId);
    Capsule findCapsuleById(UUID id);
}
