package com.example.service.impl;

import com.example.dto.AdminDashboardStats;
import com.example.dto.RegisterRequest;
import com.example.dto.UserSearchCriteria;
import com.example.mapper.RegisterRequestMapper;
import com.example.model.User;
import com.example.model.UserStatus;
import com.example.model.UserType;
import com.example.repository.ArtistRepository;
import com.example.repository.BandRepository;
import com.example.repository.SongPlayRepository;
import com.example.repository.UserRepository;
import com.example.service.SendMailService;
import com.example.service.specification.UserSpecification;
import com.example.storage.StorageService;
import jakarta.mail.MessagingException;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock private ArtistRepository artistRepository;
    @Mock private UserRepository userRepository;
    @Mock private StorageService storageService;
    @Mock private BandRepository bandRepository;
    @Mock private SongPlayRepository songPlayRepository;
    @Mock private SendMailService sendMailService;
    @Mock private RegisterRequestMapper registerRequestMapper;

    @InjectMocks private UserServiceImpl userService;

    // findUsersPage - empty criteria
    @Test
    void findUsersPage_emptyCriteria_returnsAllUsers() {
        Pageable pageable = PageRequest.of(0, 10);
        UserSearchCriteria criteria = new UserSearchCriteria();
        Page<User> expected = new PageImpl<>(List.of(new User()));
        when(userRepository.findAll(pageable)).thenReturn(expected);

        Page<User> result = userService.findUsersPage(pageable, criteria);

        assertThat(result).isEqualTo(expected);
        verify(userRepository).findAll(pageable);
    }

    @Test
    void findUsersPage_withCriteria_usesSpecification() {
        Pageable pageable = PageRequest.of(0, 10);
        UserSearchCriteria criteria = new UserSearchCriteria();
        criteria.setName("John");
        Page<User> expected = new PageImpl<>(List.of(new User()));
        when(userRepository.findAll(any(UserSpecification.class), eq(pageable))).thenReturn(expected);

        Page<User> result = userService.findUsersPage(pageable, criteria);

        assertThat(result).isEqualTo(expected);
        verify(userRepository).findAll(any(UserSpecification.class), eq(pageable));
    }

    // isRegisterRequestPresent
    @Test
    void isRegisterRequestPresent_attributeNotPresent_addsRegisterRequest() {
        ModelMap modelMap = new ModelMap();

        userService.isRegisterRequestPresent(modelMap);

        assertThat(modelMap.containsAttribute("registerRequest")).isTrue();
        assertThat(modelMap.getAttribute("registerRequest")).isInstanceOf(RegisterRequest.class);
    }

    @Test
    void isRegisterRequestPresent_attributeAlreadyPresent_doesNotOverwrite() {
        ModelMap modelMap = new ModelMap();
        RegisterRequest existing = new RegisterRequest();
        modelMap.addAttribute("registerRequest", existing);

        userService.isRegisterRequestPresent(modelMap);

        assertThat(modelMap.getAttribute("registerRequest")).isSameAs(existing);
    }

    // save - username already exists
    @Test
    void save_usernameExists_redirectsWithMessage() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("taken");
        request.setEmail("new@example.com");
        BindingResult bindingResult = mock(BindingResult.class);
        when(userRepository.findByUsername("taken")).thenReturn(Optional.of(new User()));

        String result = userService.save(request, null, Locale.ENGLISH, bindingResult);

        assertThat(result).isEqualTo("redirect:/registerPage?msg=Username already exists!");
    }

    // save - binding errors
    @Test
    void save_bindingErrors_returnsRegisterPage() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        BindingResult bindingResult = mock(BindingResult.class);
        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(bindingResult.hasErrors()).thenReturn(true);

        String result = userService.save(request, null, Locale.ENGLISH, bindingResult);

        assertThat(result).isEqualTo("registerPage");
    }

    // save - email already exists
    @Test
    void save_emailExists_redirectsWithMessage() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setEmail("taken@example.com");
        BindingResult bindingResult = mock(BindingResult.class);
        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(bindingResult.hasErrors()).thenReturn(false);
        when(userRepository.findByEmail("taken@example.com")).thenReturn(Optional.of(new User()));

        String result = userService.save(request, null, Locale.ENGLISH, bindingResult);

        assertThat(result).isEqualTo("redirect:/registerPage?msg=Email already exists");
    }

    // save - happy path
    @Test
    void save_happyPath_savesUserAndRedirectsToVerify() throws MessagingException {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setEmail("new@example.com");
        BindingResult bindingResult = mock(BindingResult.class);
        User user = new User();
        user.setEmail("new@example.com");
        user.setUsername("newuser");

        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(bindingResult.hasErrors()).thenReturn(false);
        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(registerRequestMapper.toEntity(request)).thenReturn(user);

        String result = userService.save(request, null, Locale.ENGLISH, bindingResult);

        assertThat(result).isEqualTo("redirect:/verify?email=new@example.com");
        verify(userRepository).save(user);
        assertThat(user.getVerificationCode()).isNotNull();
        assertThat(user.getVerificationCodeExpiresAt()).isNotNull();
    }

    @Test
    void save_withImage_uploadsImageForUser() throws MessagingException {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setEmail("new@example.com");
        BindingResult bindingResult = mock(BindingResult.class);
        User user = new User();
        user.setEmail("new@example.com");
        user.setUsername("newuser");
        MultipartFile file = mock(MultipartFile.class);

        when(file.isEmpty()).thenReturn(false);
        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(bindingResult.hasErrors()).thenReturn(false);
        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(registerRequestMapper.toEntity(request)).thenReturn(user);
        when(storageService.upload(file, "user-images")).thenReturn("http://img.url");

        userService.save(request, file, Locale.ENGLISH, bindingResult);

        assertThat(user.getPictureUrl()).isEqualTo("http://img.url");
    }

    // update
    @Test
    void update_withUserType_updatesUserType() {
        User existing = new User();
        existing.setId(1L);
        User update = new User();
        update.setId(1L);
        update.setUserType(UserType.ADMIN);
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));

        userService.update(update);

        assertThat(existing.getUserType()).isEqualTo(UserType.ADMIN);
        verify(userRepository).save(existing);
    }

    @Test
    void update_withUserStatus_updatesUserStatus() {
        User existing = new User();
        existing.setId(1L);
        User update = new User();
        update.setId(1L);
        update.setUserStatus(UserStatus.ENABLED);
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));

        userService.update(update);

        assertThat(existing.getUserStatus()).isEqualTo(UserStatus.ENABLED);
        verify(userRepository).save(existing);
    }

    @Test
    void update_userNotFound_throwsNoSuchElementException() {
        User update = new User();
        update.setId(99L);
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.update(update))
                .isInstanceOf(java.util.NoSuchElementException.class);
    }

    // findByUsername
    @Test
    void findByUsername_exists_returnsOptionalUser() {
        User user = new User();
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));

        Optional<User> result = userService.findByUsername("john");

        assertThat(result).contains(user);
    }

    @Test
    void findByUsername_notFound_returnsEmpty() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        Optional<User> result = userService.findByUsername("unknown");

        assertThat(result).isEmpty();
    }

    // verifyUser - already enabled
    @Test
    void verifyUser_alreadyEnabled_redirectsToLoginSuccess() {
        User user = new User();
        user.setUserStatus(UserStatus.ENABLED);
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        String result = userService.verifyUser("user@example.com", "123456");

        assertThat(result).isEqualTo("redirect:/loginPage?msg=Verification successful!");
    }

    // verifyUser - null verification code
    @Test
    void verifyUser_nullVerificationCode_redirectsToLoginFailed() {
        User user = new User();
        user.setUserStatus(UserStatus.UNENABLED);
        user.setVerificationCode(null);
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        String result = userService.verifyUser("user@example.com", "123456");

        assertThat(result).isEqualTo("redirect:/loginPage?msg=Verification failed!");
    }

    // verifyUser - wrong code
    @Test
    void verifyUser_wrongCode_redirectsToLoginFailed() {
        User user = new User();
        user.setUserStatus(UserStatus.UNENABLED);
        user.setVerificationCode("correct");
        user.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(10));
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        String result = userService.verifyUser("user@example.com", "wrong");

        assertThat(result).isEqualTo("redirect:/loginPage?msg=Verification failed!");
    }

    // verifyUser - expired code
    @Test
    void verifyUser_expiredCode_redirectsToLoginFailed() {
        User user = new User();
        user.setUserStatus(UserStatus.UNENABLED);
        user.setVerificationCode("123456");
        user.setVerificationCodeExpiresAt(LocalDateTime.now().minusMinutes(1));
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        String result = userService.verifyUser("user@example.com", "123456");

        assertThat(result).isEqualTo("redirect:/loginPage?msg=Verification failed!");
    }

    // verifyUser - happy path
    @Test
    void verifyUser_validCode_enablesUserAndRedirects() {
        User user = new User();
        user.setUserStatus(UserStatus.UNENABLED);
        user.setVerificationCode("123456");
        user.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(10));
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        String result = userService.verifyUser("user@example.com", "123456");

        assertThat(result).isEqualTo("redirect:/loginPage?msg=Verification successful!");
        assertThat(user.getUserStatus()).isEqualTo(UserStatus.ENABLED);
        assertThat(user.getVerificationCode()).isNull();
        assertThat(user.getVerificationCodeExpiresAt()).isNull();
        verify(userRepository).save(user);
    }

    // verifyUser - user not found
    @Test
    void verifyUser_userNotFound_throwsEntityNotFoundException() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.verifyUser("unknown@example.com", "123456"))
                .isInstanceOf(EntityNotFoundException.class);
    }

    // getAdminDashboardStats
    @Test
    void getAdminDashboardStats_happyPath_returnsStatsWithGrowthCalculations() {
        when(songPlayRepository.count()).thenReturn(1000L);
        when(userRepository.count()).thenReturn(500L);
        when(artistRepository.count()).thenReturn(100L);
        when(bandRepository.count()).thenReturn(50L);
        when(songPlayRepository.countByPlayedAtBetween(any(), any())).thenReturn(200L, 100L);
        when(userRepository.countByRegistrationDateBetween(any(), any())).thenReturn(50L, 40L);
        when(artistRepository.countByCreatedAtBetween(any(), any())).thenReturn(10L, 8L);
        when(bandRepository.countByCreatedAtBetween(any(), any())).thenReturn(5L, 4L);

        AdminDashboardStats stats = userService.getAdminDashboardStats();

        assertThat(stats).isNotNull();
        assertThat(stats.totalListening()).isEqualTo(1000L);
        assertThat(stats.totalUsers()).isEqualTo(500L);
        assertThat(stats.totalArtists()).isEqualTo(150L);
        assertThat(stats.listeningGrowthPercent()).isEqualTo(100.0);
        assertThat(stats.usersGrowthPercent()).isEqualTo(25.0);
    }

    @Test
    void getAdminDashboardStats_zeroPreviousPeriod_returnsHundredPercentGrowthIfCurrentNonZero() {
        when(songPlayRepository.count()).thenReturn(10L);
        when(userRepository.count()).thenReturn(5L);
        when(artistRepository.count()).thenReturn(2L);
        when(bandRepository.count()).thenReturn(1L);
        when(songPlayRepository.countByPlayedAtBetween(any(), any())).thenReturn(10L, 0L);
        when(userRepository.countByRegistrationDateBetween(any(), any())).thenReturn(0L, 0L);
        when(artistRepository.countByCreatedAtBetween(any(), any())).thenReturn(0L, 0L);
        when(bandRepository.countByCreatedAtBetween(any(), any())).thenReturn(0L, 0L);

        AdminDashboardStats stats = userService.getAdminDashboardStats();

        assertThat(stats.listeningGrowthPercent()).isEqualTo(100.0);
        assertThat(stats.usersGrowthPercent()).isEqualTo(0.0);
    }
}