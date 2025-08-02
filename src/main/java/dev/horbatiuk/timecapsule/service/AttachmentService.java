package dev.horbatiuk.timecapsule.service;

import dev.horbatiuk.timecapsule.exception.AppException;
import dev.horbatiuk.timecapsule.persistence.AttachmentRepository;
import dev.horbatiuk.timecapsule.persistence.dto.attachment.AttachmentCreateDTO;
import dev.horbatiuk.timecapsule.persistence.dto.attachment.AttachmentResponseDTO;
import dev.horbatiuk.timecapsule.persistence.dto.capsule.CapsuleResponseDTO;
import dev.horbatiuk.timecapsule.persistence.entities.Attachment;
import dev.horbatiuk.timecapsule.persistence.entities.Capsule;
import dev.horbatiuk.timecapsule.persistence.mapper.AttachmentMapper;
import dev.horbatiuk.timecapsule.service.aws.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttachmentService {

    private final AttachmentRepository attachmentRepository;
    private final CapsuleService capsuleService;
    private final AttachmentMapper attachmentMapper;
    private final S3Service s3Service;

    public List<AttachmentResponseDTO> getAttachmentsByCapsuleId(UUID capsuleId) {
        return attachmentRepository.findByCapsuleId(capsuleId).stream()
                .map(attachmentMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void addAttachmentToCapsule(UUID capsuleId, AttachmentCreateDTO dto, String email, MultipartFile file)
    throws AppException{
        if (file == null || file.isEmpty()) {
            throw new AppException("Attachment file is missing", HttpStatus.BAD_REQUEST);
        }

        CapsuleResponseDTO capsule = capsuleService.findCapsuleById(capsuleId);

        if (!capsule.getEmail().equals(email)) {
            throw new AppException("User does not have access to this capsule", HttpStatus.FORBIDDEN);
        }

        String originalFilename = Optional.ofNullable(file.getOriginalFilename()).orElse("unnamed_file");
        String safeFilename = UUID.randomUUID() + "_" +originalFilename;

        try {
            s3Service.uploadFile(
                    capsuleId.toString(),
                    safeFilename,
                    file.getInputStream(),
                    file.getSize(),
                    file.getContentType()
            );
        } catch (IOException e) {
            throw new AppException("Failed to upload attachment", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        Attachment attachment = attachmentMapper.toEntity(dto);
        Capsule capsuleEntity = capsuleService.findCapsuleEntityById(capsuleId);
        attachment.setCapsule(capsuleEntity);
        attachment.setFileKey(safeFilename);
        attachment.setFilename(originalFilename);

        try {
            attachmentRepository.save(attachment);
        } catch (Exception e) {
            s3Service.deleteFile(capsuleId.toString(), safeFilename);
            throw new AppException("Could not save attachment", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
