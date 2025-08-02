package dev.horbatiuk.timecapsule.controllers.admin;

import dev.horbatiuk.timecapsule.persistence.dto.capsule.CapsuleResponseDTO;
import dev.horbatiuk.timecapsule.persistence.entities.enums.CapsuleStatus;
import dev.horbatiuk.timecapsule.service.CapsuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminCapsuleController {

    private final CapsuleService capsuleService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/capsules/{userId}")
    public ResponseEntity<List<CapsuleResponseDTO>> getUserCapsule(
            @PathVariable UUID userId
    ) {
        return ResponseEntity.ok(capsuleService.findCapsulesByUserId(userId));
    }


    /*
        TODO: ДОДАТИ СКАСУВАННЯ ПОДІЇ
         ІНВЕРСІЯ UserCapsuleController.changeCapsuleStatusToActive
         СКАСУВАТИ ПОДІЮ
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/capsules/{capsuleId}")
    public ResponseEntity<CapsuleResponseDTO> updateCapsule(
        @PathVariable UUID capsuleId,
        @RequestParam boolean setStatus
    ) {
        CapsuleResponseDTO capsule = capsuleService.findCapsuleById(capsuleId);
        CapsuleStatus neededStatus = setStatus ? CapsuleStatus.ACTIVE : CapsuleStatus.INACTIVE;
        capsuleService.updateStatus(capsule.getId(), neededStatus);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/capsules/{capsuleId}")
    public ResponseEntity<CapsuleResponseDTO> deleteCapsule(
            @PathVariable UUID capsuleId
    ) {
        capsuleService.deleteCapsuleById(capsuleId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

}
