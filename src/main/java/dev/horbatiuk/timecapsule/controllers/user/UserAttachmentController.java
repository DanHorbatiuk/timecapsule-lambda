package dev.horbatiuk.timecapsule.controllers.user;

import dev.horbatiuk.timecapsule.exception.AppException;
import dev.horbatiuk.timecapsule.persistence.dto.attachment.AttachmentCreateDTO;
import dev.horbatiuk.timecapsule.persistence.dto.attachment.AttachmentResponseDTO;
import dev.horbatiuk.timecapsule.security.CustomUserDetails;
import dev.horbatiuk.timecapsule.service.AttachmentService;
import dev.horbatiuk.timecapsule.service.CapsuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.unit.DataSize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserAttachmentController {

    @Value("${app.user-max-attachments-per-capsule}")
    private int userMaxAttachmentsPerCapsule;

    @Value("${app.premium-user-max-attachments-per-capsule}")
    private int premiumUserMaxAttachmentsPerCapsule;

    @Value("${aws.s3.max-file-size}")
    private DataSize maxFileSize;

    private final AttachmentService attachmentService;
    private final CapsuleService capsuleService;

    @GetMapping("/{capsuleId}/attachments")
    public ResponseEntity<List<AttachmentResponseDTO>> getAttachments(
            @PathVariable UUID capsuleId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        if (!capsuleService.userHasAccess(capsuleId, customUserDetails.getEmail())) {
            throw new AppException("You do not have access to this capsule",  HttpStatus.FORBIDDEN);
        }
        List<AttachmentResponseDTO> attachments = attachmentService.getAttachmentsByCapsuleId(capsuleId);
        return ResponseEntity.ok(attachments);
    }

    @PostMapping(
            value = "/{capsuleId}/attachments",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<Void> addAttachment(
            @PathVariable UUID capsuleId,
            @AuthenticationPrincipal CustomUserDetails user,
            @ModelAttribute("data") AttachmentCreateDTO dto,
            @RequestPart("file") MultipartFile file
    ) {
        int max = user.isPremiumUser() ? premiumUserMaxAttachmentsPerCapsule : userMaxAttachmentsPerCapsule;
        int current = attachmentService.getAttachmentsByCapsuleId(capsuleId).size();
        if (current >= max) {
            throw new AppException("Maximum number of attachments exceeded", HttpStatus.FORBIDDEN);
        }
        if (file.getSize() > maxFileSize.toBytes()) {
            throw new AppException("File size exceeded", HttpStatus.PAYLOAD_TOO_LARGE);
        }
        attachmentService.addAttachmentToCapsule(capsuleId, dto, user.getEmail(), file);
        return ResponseEntity.ok().build();
    }

}
