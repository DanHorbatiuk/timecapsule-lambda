package dev.horbatiuk.timecapsule.controllers.user;

import dev.horbatiuk.timecapsule.exception.AppException;
import dev.horbatiuk.timecapsule.persistence.dto.capsule.CapsuleCreateDTO;
import dev.horbatiuk.timecapsule.persistence.dto.capsule.CapsuleResponseDTO;
import dev.horbatiuk.timecapsule.persistence.entities.enums.CapsuleStatus;
import dev.horbatiuk.timecapsule.security.CustomUserDetails;
import dev.horbatiuk.timecapsule.service.CapsuleService;
import dev.horbatiuk.timecapsule.service.aws.EventBridgeScheduledService;
import dev.horbatiuk.timecapsule.service.aws.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserCapsuleController {

    @Value("${app.user-max-capsules}")
    private int userMaxCapsules;

    @Value("${app.premium-user-max-capsules}")
    private int premiumUserMaxCapsules;

    private final S3Service s3Service;
    private final CapsuleService capsuleService;
    private final EventBridgeScheduledService eventBridgeScheduledService;

    @GetMapping("/capsules")
    public ResponseEntity<List<CapsuleResponseDTO>> getCapsulesByUser(
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        List<CapsuleResponseDTO> capsuleResponseDTOList =
                capsuleService.findCapsulesByEmail(user.getEmail());
        if (capsuleResponseDTOList.isEmpty()) {
            return ResponseEntity.ok(new ArrayList<>());
        }
        return ResponseEntity.ok(capsuleResponseDTOList);
    }

    @PostMapping("/capsules/add")
    public ResponseEntity<Void> addNewCapsuleByUser(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody CapsuleCreateDTO dto
    ) {
        try {
            int maxCapsules = userDetails.isPremiumUser() ? premiumUserMaxCapsules : userMaxCapsules;
            boolean allowedToAddNewCapsule = capsuleService.findCapsulesByEmail(userDetails.getEmail()).size() < maxCapsules;

            if (!userDetails.isVerified()) {
                throw new AppException("User is not verified", HttpStatus.FORBIDDEN);
            }

            if (!allowedToAddNewCapsule) {
                throw new AppException("Limit reached, max: " + maxCapsules + " capsules", HttpStatus.FORBIDDEN);
            }

            capsuleService.addNewCapsule(userDetails.getEmail(), dto);
            return ResponseEntity.ok().build();

        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            throw new AppException("An error occurred while adding new capsule", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PatchMapping("/capsules/{capsuleId}")
    public ResponseEntity<Void> changeCapsuleStatusToActive(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID capsuleId
    ) {
        if (!capsuleService.userHasAccess(capsuleId, userDetails.getEmail())) {
            throw new AppException("You do not have access to this capsule", HttpStatus.FORBIDDEN);
        }
        try {
            capsuleService.setCapsuleStatus(capsuleId, CapsuleStatus.ACTIVE);
            CapsuleResponseDTO capsuleResponseDTO = capsuleService.findCapsuleById(capsuleId);
            System.out.printf(capsuleResponseDTO.getOpenAt().toInstant().toString());

            s3Service.uploadCapsuleData(capsuleResponseDTO);
            eventBridgeScheduledService.scheduleLambdaTrigger(
                    capsuleId,
                    capsuleResponseDTO.getOpenAt().toInstant()
            );
        } catch (Exception e) {
            System.out.printf(e.getMessage());
            capsuleService.setCapsuleStatus(capsuleId, CapsuleStatus.INACTIVE);
            throw new AppException("Failed to activate capsule: " + capsuleId, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return ResponseEntity.ok().build();
    }

}
