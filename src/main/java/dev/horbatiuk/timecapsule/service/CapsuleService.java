package dev.horbatiuk.timecapsule.service;

import dev.horbatiuk.timecapsule.exception.AppException;
import dev.horbatiuk.timecapsule.persistence.CapsuleRepository;
import dev.horbatiuk.timecapsule.persistence.UserRepository;
import dev.horbatiuk.timecapsule.persistence.dto.capsule.CapsuleCreateDTO;
import dev.horbatiuk.timecapsule.persistence.dto.capsule.CapsuleResponseDTO;
import dev.horbatiuk.timecapsule.persistence.entities.Capsule;
import dev.horbatiuk.timecapsule.persistence.entities.User;
import dev.horbatiuk.timecapsule.persistence.entities.enums.CapsuleStatus;
import dev.horbatiuk.timecapsule.persistence.mapper.CapsuleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CapsuleService {

    private final CapsuleRepository capsuleRepository;
    private final UserRepository userRepository;
    private final CapsuleMapper capsuleMapper;

    @Transactional
    public List<CapsuleResponseDTO> findCapsulesByEmail(String email) {
        User user = findUser(email);
        return capsuleRepository.findAllByAppUser(user).stream()
                .map(capsuleMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void addNewCapsule(String email, CapsuleCreateDTO dto) {
        User user = findUser(email);
        Capsule capsule = capsuleMapper.toEntity(dto);
        capsule.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
        capsule.setAppUser(user);
        capsuleRepository.save(capsule);
    }

    private User findUser(String email) {
        return userRepository.findUserByEmail(email)
                .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND));
    }

    @Transactional
    public List<CapsuleResponseDTO> findCapsulesByUserId(UUID userId) {
        return capsuleRepository.findAllByAppUser_Id(userId).stream()
                .map(capsuleMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    public CapsuleResponseDTO findCapsuleById(UUID capsuleId) {
        Capsule capsule = findCapsuleEntityById(capsuleId);
        return capsuleMapper.toResponseDTO(capsule);
    }

    public void deleteCapsuleById(UUID capsuleId) {
        Capsule capsule = findCapsuleEntityById(capsuleId);
        capsuleRepository.delete(capsule);
    }

    public void updateStatus(UUID capsuleId, CapsuleStatus newStatus) {
        Capsule capsule = findCapsuleEntityById(capsuleId);
        capsule.setStatus(newStatus);
        capsuleRepository.save(capsule);
    }

    public void setCapsuleStatus(UUID capsuleId, CapsuleStatus newStatus) {
        Capsule capsule = findCapsuleEntityById(capsuleId);
        capsule.setStatus(newStatus);
        capsuleRepository.save(capsule);
    }

    protected Capsule findCapsuleEntityById(UUID capsuleId) {
        return capsuleRepository.findById(capsuleId)
                .orElseThrow(() -> new AppException("Capsule not found: " + capsuleId, HttpStatus.NOT_FOUND));
    }

    public boolean userHasAccess(UUID capsuleId, String email) {
        CapsuleResponseDTO capsule = findCapsuleById(capsuleId);
        if (!capsule.getEmail().equals(email)) {
            throw new AppException("User (" + email + ") does not have access to this capsule (" + capsuleId + ")", HttpStatus.FORBIDDEN);
        }
        return true;
    }
}
