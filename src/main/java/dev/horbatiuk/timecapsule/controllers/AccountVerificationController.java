package dev.horbatiuk.timecapsule.controllers;

import dev.horbatiuk.timecapsule.exception.AppException;
import dev.horbatiuk.timecapsule.persistence.UserRepository;
import dev.horbatiuk.timecapsule.persistence.entities.User;
import dev.horbatiuk.timecapsule.security.CustomUserDetails;
import dev.horbatiuk.timecapsule.service.security.UserVerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@RequiredArgsConstructor
@Controller
@RequestMapping("/api/v1/verify")
public class AccountVerificationController {

    private final UserVerificationService userVerificationService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<?> sendVerificationEmail(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        try {
            User user = userRepository.findUserByEmail(userDetails.getEmail())
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            userVerificationService.sendVerificationEmail(user.getId(), user.getEmail(), user.isVerified());
        } catch (Exception e) {
            throw new AppException("Could not send verification email", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping
    public ResponseEntity<?> verifyAccount(
            @RequestParam UUID token
    ) {
        try {
            userVerificationService.verifyToken(token);
            return ResponseEntity.ok("Account successfully verified.");
        } catch (Exception e) {
            throw new AppException("Invalid or expired token.", HttpStatus.BAD_REQUEST);
        }
    }

}
