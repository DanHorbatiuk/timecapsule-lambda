package dev.horbatiuk.timecapsule.service;

import dev.horbatiuk.timecapsule.exception.AppException;
import dev.horbatiuk.timecapsule.persistence.UserRepository;
import dev.horbatiuk.timecapsule.persistence.dto.user.UpdateUserInfoRequestDTO;
import dev.horbatiuk.timecapsule.persistence.entities.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public void updateProfile(String email, UpdateUserInfoRequestDTO requestDTO) {
        User user = userRepository.findUserByEmail(email)
                .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND));
        if (requestDTO.getName() != null && !requestDTO.getName().isEmpty()) {
            user.setName(requestDTO.getName());
        }
        if (requestDTO.getNewPassword() != null && !requestDTO.getNewPassword().isEmpty()) {
            if (!passwordEncoder.matches(requestDTO.getOldPassword(), user.getPassword())) {
                throw new AppException("Your old password does not match.", HttpStatus.FORBIDDEN);
            }
            String newPasswordEncoded = passwordEncoder.encode(requestDTO.getNewPassword());
            user.setPassword(newPasswordEncoded);
        }
        userRepository.save(user);
    }

}
