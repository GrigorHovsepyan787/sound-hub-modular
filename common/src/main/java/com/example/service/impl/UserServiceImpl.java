package com.example.service.impl;

import com.example.dto.UserSearchCriteria;
import com.example.model.User;
import com.example.model.UserStatus;
import com.example.model.UserType;
import com.example.repository.UserRepository;
import com.example.service.SendMailService;
import com.example.service.UserService;
import com.example.service.specification.UserSpecification;
import com.example.storage.StorageService;
import jakarta.mail.MessagingException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final StorageService storageService;
    private final SendMailService sendMailService;
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public Page<User> findUsersPage(Pageable pageable, UserSearchCriteria criteria) {
        boolean emptyCriteria = (criteria.getName() == null && criteria.getUsername() == null && criteria.getEmail() == null);

        if (emptyCriteria) {
            return userRepository.findAll(pageable);
        } else {
            UserSpecification userSpecification = new UserSpecification(criteria);
            return userRepository.findAll(userSpecification, pageable);
        }
    }

    @Override
    public void save(User user, MultipartFile multipartFile, Locale locale) {
        user.setUserStatus(UserStatus.UNENABLED);
        user.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(15));
        user.setVerificationCode(generateVerificationCode());
        if (multipartFile != null && !multipartFile.isEmpty()) {
            String imageUrl = storageService.upload(multipartFile, "user-images");

            if (imageUrl != null) {
                user.setPictureUrl(imageUrl);
                log.info("Image uploaded for user: {}", user.getName());
            } else {
                user.setPictureUrl("https://soundhub7.s3.eu-north-1.amazonaws.com/assets/UserDefault.png");
            }
        }
        user.setUserType(UserType.USER);
        userRepository.save(user);
        if (user.getEmail().contains("@")) {
            try {
                sendMailService.sendVerificationMail(user.getEmail(), user.getVerificationCode(), locale);
            } catch (MessagingException e) {
                log.error("Error while sending verification mail {}", e.getMessage());
            }
        }
    }

    @Override
    public void update(User user) {
        User existingUser = userRepository
                .findById(user.getId())
                .orElseThrow();

        if (user.getUserType() != null) {
            existingUser.setUserType(user.getUserType());
        }

        if (user.getUserStatus() != null) {
            existingUser.setUserStatus(user.getUserStatus());
        }

        userRepository.save(existingUser);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public boolean verifyUser(String email, String code) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(EntityNotFoundException::new);

        if (!user.getVerificationCode().equals(code)) {
            return false;
        }

        if (user.getVerificationCodeExpiresAt()
                .isBefore(LocalDateTime.now())) {
            return false;
        }

        user.setUserStatus(UserStatus.ENABLED);
        user.setVerificationCode(null);
        user.setVerificationCodeExpiresAt(null);
        userRepository.save(user);
        return true;
    }

    private String generateVerificationCode() {
        int code = 100000 + secureRandom.nextInt(900000);
        return String.valueOf(code);
    }
}
