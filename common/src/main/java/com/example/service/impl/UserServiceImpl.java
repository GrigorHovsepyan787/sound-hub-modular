package com.example.service.impl;

import com.example.dto.AdminDashboardStats;
import com.example.dto.RegisterRequest;
import com.example.dto.UserSearchCriteria;
import com.example.mapper.RegisterRequestMapper;
import com.example.model.User;
import com.example.model.UserStatus;
import com.example.repository.ArtistRepository;
import com.example.repository.BandRepository;
import com.example.repository.SongPlayRepository;
import com.example.repository.UserRepository;
import com.example.service.SendMailService;
import com.example.service.UserService;
import com.example.service.specification.UserSpecification;
import com.example.storage.StorageService;
import com.example.util.DateRange;
import com.example.util.DateRangeUtils;
import jakarta.mail.MessagingException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;
import java.util.function.LongSupplier;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final ArtistRepository artistRepository;
    private final UserRepository userRepository;
    private final StorageService storageService;
    private final BandRepository bandRepository;
    private final SongPlayRepository songPlayRepository;
    private final Clock clock = Clock.systemDefaultZone();
    private final SendMailService sendMailService;
    private final RegisterRequestMapper registerRequestMapper;
    private final SecureRandom secureRandom = new SecureRandom();
    private static final int VERIFICATION_CODE_MIN = 100000;
    private static final int VERIFICATION_CODE_RANGE = 900000;
    private static final int CODE_EXPIRATION_MINS = 15;
    private static final String DEFAULT_USER_IMAGE_URL = "https://soundhub7.s3.eu-north-1.amazonaws.com/assets/UserDefault.png";

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
    public void isRegisterRequestPresent(ModelMap modelMap) {
        if (!modelMap.containsAttribute("registerRequest")) {
            modelMap.addAttribute("registerRequest", new RegisterRequest());
        }
    }

    @Override
    @Transactional
    public String save(RegisterRequest request, MultipartFile multipartFile, Locale locale, BindingResult bindingResult) {

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            return "redirect:/registerPage?msg=Username already exists!";
        }
        if (bindingResult.hasErrors()) {
            return "registerPage";
        }
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return "redirect:/registerPage?msg=Email already exists";
        }
        User user = registerRequestMapper.toEntity(request);

        user.setVerificationCode(generateVerificationCode());
        user.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(CODE_EXPIRATION_MINS));

        handleUserImage(user, multipartFile);

        userRepository.save(user);

        sendVerificationEmailSafe(user, locale);

        return "redirect:/verify?email=" + request.getEmail();
    }

    @Override
    @Transactional
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
    @Transactional
    public String verifyUser(String email, String code) {
        User user = userRepository.findByEmail(email).orElseThrow(EntityNotFoundException::new);

        if (user.getUserStatus() == UserStatus.ENABLED) {
            return "redirect:/loginPage?msg=Verification successful!";
        }

        if (user.getVerificationCode() == null) {
            return "redirect:/loginPage?msg=Verification failed!";
        }

        if (!user.getVerificationCode().equals(code)) {
            return "redirect:/loginPage?msg=Verification failed!";
        }

        if (user.getVerificationCodeExpiresAt().isBefore(LocalDateTime.now())) {
            return "redirect:/loginPage?msg=Verification failed!";
        }

        user.setUserStatus(UserStatus.ENABLED);

        user.setVerificationCode(null);

        user.setVerificationCodeExpiresAt(null);

        userRepository.save(user);

        return "redirect:/loginPage?msg=Verification successful!";
    }

    @Override
    @Transactional(readOnly = true)
    public AdminDashboardStats getAdminDashboardStats() {
        DateRange dateRange = DateRangeUtils.monthlyRange(clock);
        long totalListening = songPlayRepository.count();
        long totalUsers = userRepository.count();
        long totalArtists = artistRepository.count() + bandRepository.count();
        LocalDateTime currentStart = dateRange.currentStart();
        LocalDateTime currentEnd = dateRange.currentEnd();
        LocalDateTime previousStart = dateRange.previousStart();
        LocalDateTime previousEnd = dateRange.previousEnd();

        double listeningGrowth = calculateGrowthForPeriod(
                () -> songPlayRepository.countByPlayedAtBetween(currentStart, currentEnd),
                () -> songPlayRepository.countByPlayedAtBetween(previousStart, previousEnd)
        );

        double usersGrowth = calculateGrowthForPeriod(
                () -> userRepository.countByRegistrationDateBetween(currentStart, currentEnd),
                () -> userRepository.countByRegistrationDateBetween(previousStart, previousEnd)
        );

        double artistsGrowth = calculateGrowthForPeriod(
                () -> artistRepository.countByCreatedAtBetween(currentStart, currentEnd)
                        + bandRepository.countByCreatedAtBetween(currentStart, currentEnd),
                () -> artistRepository.countByCreatedAtBetween(previousStart, previousEnd)
                        + bandRepository.countByCreatedAtBetween(previousStart, previousEnd)
        );

        return new AdminDashboardStats(totalListening, totalUsers, totalArtists, listeningGrowth, usersGrowth, artistsGrowth);
    }

    private void sendVerificationEmailSafe(User user, Locale locale) {
        try {
            sendMailService.sendVerificationMail(
                    user.getEmail(),
                    user.getVerificationCode(),
                    locale
            );
        } catch (MessagingException e) {
            log.error("Error while sending verification mail to {}", user.getEmail(), e);
        }
    }

    private String generateVerificationCode() {
        int code = VERIFICATION_CODE_MIN + secureRandom.nextInt(VERIFICATION_CODE_RANGE);
        return String.valueOf(code);
    }

    private void handleUserImage(User user, MultipartFile multipartFile) {
        if (multipartFile == null || multipartFile.isEmpty()) {
            user.setPictureUrl(DEFAULT_USER_IMAGE_URL);
            return;
        }
        String imageUrl = storageService.upload(multipartFile, "user-images");
        if (imageUrl != null) {
            user.setPictureUrl(imageUrl);
            log.info("Image uploaded for user: {}", user.getUsername());
        } else {
            user.setPictureUrl(DEFAULT_USER_IMAGE_URL);
        }
    }

    private double calculateGrowthForPeriod(LongSupplier currentSupplier, LongSupplier previousSupplier) {
        long current = currentSupplier.getAsLong();
        long previous = previousSupplier.getAsLong();
        return calculateGrowth(current, previous);
    }

    private double calculateGrowth(long current, long previous) {
        if (previous == 0) {
            return current == 0 ? 0 : 100;
        }

        double growth =
                ((double) (current - previous) / previous) * 100;

        return Math.round(growth * 10.0) / 10.0;
    }
}
