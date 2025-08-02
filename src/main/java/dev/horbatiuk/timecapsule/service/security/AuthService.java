package dev.horbatiuk.timecapsule.service.security;

import dev.horbatiuk.timecapsule.exception.AppException;
import dev.horbatiuk.timecapsule.persistence.UserRepository;
import dev.horbatiuk.timecapsule.persistence.dto.security.AuthenticationRequestDTO;
import dev.horbatiuk.timecapsule.persistence.dto.security.AuthenticationResponseDTO;
import dev.horbatiuk.timecapsule.persistence.dto.security.RegisterRequestDTO;
import dev.horbatiuk.timecapsule.persistence.dto.user.UserDTO;
import dev.horbatiuk.timecapsule.persistence.entities.RefreshToken;
import dev.horbatiuk.timecapsule.persistence.entities.User;
import dev.horbatiuk.timecapsule.persistence.entities.VerificationToken;
import dev.horbatiuk.timecapsule.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Validated
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;
    private final UserVerificationService userVerificationService;

    public AuthenticationResponseDTO register(@Valid RegisterRequestDTO dto) {
        if (userRepository.findUserByEmail(dto.getEmail()).isPresent()) {
            throw new AppException("Email already exists", HttpStatus.CONFLICT);
        }

        User user = User.builder()
                .email(dto.getEmail())
                .name(dto.getName())
                .password(passwordEncoder.encode(dto.getPassword()))
                .createdAt(Timestamp.valueOf(LocalDateTime.now()))
                .roles(Set.of("ROLE_USER"))
                .capsules(new ArrayList<>())
                .build();
        userRepository.save(user);

        VerificationToken verificationToken = VerificationToken.builder()
                .user(user)
                .token(UUID.randomUUID())
                .build();
        userVerificationService.addToken(verificationToken);

        return generateTokens(user);
    }

    public AuthenticationResponseDTO authenticate(@Valid AuthenticationRequestDTO request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        User user = userRepository.findUserByEmail(request.getEmail())
                .orElseThrow(()  -> new AppException("User not found", HttpStatus.UNAUTHORIZED));
        return generateTokens(user);
    }

    private AuthenticationResponseDTO generateTokens(User user) {
        String jwt = jwtService.generateToken(new CustomUserDetails(user));
        RefreshToken refresh = refreshTokenService.createOrUpdateRefreshToken(user.getEmail());
        return returnAuthenticationResponseDTO(jwt, refresh);
    }

    private AuthenticationResponseDTO returnAuthenticationResponseDTO(String jwtToken, RefreshToken refreshToken) {
        return AuthenticationResponseDTO.builder()
                .token(jwtToken)
                .refreshToken(refreshToken.getToken())
                .build();
    }

    public UserDTO getUserInfo(String email) {
        User user = userRepository.findUserByEmail(email)
                .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND));
        return new UserDTO(user.getId(), user.getEmail(), user.getName());
    }

}