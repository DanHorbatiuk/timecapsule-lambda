package dev.horbatiuk.timecapsule.service.security;

import dev.horbatiuk.timecapsule.exception.AppException;
import dev.horbatiuk.timecapsule.persistence.UserRepository;
import dev.horbatiuk.timecapsule.persistence.VerificationTokenRepository;
import dev.horbatiuk.timecapsule.persistence.entities.User;
import dev.horbatiuk.timecapsule.persistence.entities.VerificationToken;
import dev.horbatiuk.timecapsule.service.EmailSenderService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserVerificationService {

    private final EmailSenderService emailSenderService;
    private final UserRepository userRepository;
    private final VerificationTokenRepository verificationTokenRepository;

    @Value("${app.base-url}")
    private String baseUrl;

    @Async
    public void sendVerificationEmail(UUID userId, String email, boolean verified) {
        if (!verified) {
            User user = userRepository.findUserById(userId)
                    .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND));
            VerificationToken verificationToken = verificationTokenRepository
                    .findVerificationTokenByUser(user)
                    .orElseThrow(() -> new AppException("Verification token not found for user: " + userId, HttpStatus.NOT_FOUND));
            String verificationLink = UriComponentsBuilder.fromUriString(baseUrl)
                    .path("/api/v1/verify")
                    .queryParam("token", verificationToken.getToken())
                    .toUriString();
            emailSenderService.sendVerificationEmail(email, verificationLink);
        }
    }

    @Transactional
    public void addToken(VerificationToken token) {
        try {
            verificationTokenRepository.save(token);
        } catch (DataAccessException e) {
            throw new AppException("Failed to save verification token", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public void verifyToken(UUID token) {
        VerificationToken verificationToken = verificationTokenRepository
                .findVerificationTokenByToken(token)
                .orElseThrow(() -> new AppException("Verification token not found: " + token, HttpStatus.NOT_FOUND ));
        updateUserStatus(verificationToken.getUser().getId());
        try {
            verificationTokenRepository.delete(verificationToken);
        } catch (DataAccessException e) {
            throw new AppException("Failed to save verification token", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    protected void updateUserStatus(UUID userId) {
        User user = userRepository.findUserById(userId)
                .orElseThrow(() -> new AppException("User not found",  HttpStatus.NOT_FOUND));
        if (user.isVerified()) {
            return;
        }
        user.setVerified(true);
    }

}
