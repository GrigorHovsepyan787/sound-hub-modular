package com.example.app.controller;

import com.example.app.service.security.SpringUser;
import com.example.dto.RegisterRequest;
import com.example.model.User;
import com.example.model.UserType;
import com.example.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthController authController;

    @Test
    void successLogin_adminUser_redirectsToAdminHome() {
        SpringUser springUser = mock(SpringUser.class);
        User user = mock(User.class);

        when(springUser.getUser()).thenReturn(user);
        when(user.getUserType()).thenReturn(UserType.ADMIN);

        String view = authController.successLogin(springUser);

        assertEquals("redirect:/admin/home", view);
    }

    @Test
    void successLogin_nonAdminUser_redirectsToRoot() {
        SpringUser springUser = mock(SpringUser.class);
        User user = mock(User.class);

        when(springUser.getUser()).thenReturn(user);
        when(user.getUserType()).thenReturn(UserType.USER);

        String view = authController.successLogin(springUser);

        assertEquals("redirect:/", view);
    }

    @Test
    void successLogin_nullSpringUser_redirectsToRoot() {
        String view = authController.successLogin(null);
        
        assertEquals("redirect:/", view);
    }

    @Test
    void loginPage_withMsg_returnsLoginPageViewWithMsgInModel() {
        ModelMap modelMap = new ModelMap();

        String view = authController.loginPage("error", modelMap);
        
        assertEquals("loginPage", view);
        assertEquals("error", modelMap.get("msg"));
    }

    @Test
    void loginPage_withNullMsg_modelContainsNull() {
        ModelMap modelMap = new ModelMap();
        
        String view = authController.loginPage(null, modelMap);
        
        assertEquals("loginPage", view);
        assertEquals(null, modelMap.get("msg"));
    }

    @Test
    void registerPage_withMsg_returnsRegisterPageViewAndCallsIsRegisterRequestPresent() {
        ModelMap modelMap = new ModelMap();

        String view = authController.registerPage("welcome", modelMap);

        assertEquals("registerPage", view);
        assertEquals("welcome", modelMap.get("msg"));
        verify(userService).isRegisterRequestPresent(modelMap);
    }

    @Test
    void registerPage_withNullMsg_modelContainsNull() {
        ModelMap modelMap = new ModelMap();

        String view = authController.registerPage(null, modelMap);
        
        assertEquals("registerPage", view);
        assertEquals(null, modelMap.get("msg"));
        verify(userService).isRegisterRequestPresent(modelMap);
    }

    @Test
    void verifyPage_withEmail_returnsVerifyPageViewWithEmailInModel() {
        ModelMap modelMap = new ModelMap();
        
        String view = authController.verifyPage("user@example.com", modelMap);
        
        assertEquals("verifyPage", view);
        assertEquals("user@example.com", modelMap.get("email"));
    }

    @Test
    void verifyPage_emailStoredCorrectlyInModel() {
        ModelMap modelMap = new ModelMap();
        String email = "test@domain.org";
        
        authController.verifyPage(email, modelMap);
        
        assertEquals(email, modelMap.get("email"));
    }

    @Test
    void register_validRequest_encodesPasswordAndDelegatesToUserService() {
        RegisterRequest registeredUser = new RegisterRequest();
        registeredUser.setPassword("plaintext");
        MultipartFile multipartFile = mock(MultipartFile.class);
        BindingResult bindingResult = mock(BindingResult.class);
        Locale locale = Locale.ENGLISH;

        when(passwordEncoder.encode("plaintext")).thenReturn("encoded");
        when(userService.save(eq(registeredUser), eq(multipartFile), eq(locale), eq(bindingResult)))
                .thenReturn("redirect:/verify?email=test");

        try (MockedStatic<LocaleContextHolder> localeHolder = mockStatic(LocaleContextHolder.class)) {
            localeHolder.when(LocaleContextHolder::getLocale).thenReturn(locale);

            String view = authController.register(registeredUser, bindingResult, multipartFile);
            
            assertEquals("redirect:/verify?email=test", view);
            assertEquals("encoded", registeredUser.getPassword());
            verify(passwordEncoder).encode("plaintext");
            verify(userService).save(registeredUser, multipartFile, locale, bindingResult);
        }
    }

    @Test
    void register_passwordIsEncodedBeforeSave() {
        RegisterRequest registeredUser = new RegisterRequest();
        registeredUser.setPassword("secret");
        MultipartFile multipartFile = mock(MultipartFile.class);
        BindingResult bindingResult = mock(BindingResult.class);
        Locale locale = Locale.ENGLISH;

        when(passwordEncoder.encode("secret")).thenReturn("hashed_secret");
        when(userService.save(eq(registeredUser), eq(multipartFile), eq(locale), eq(bindingResult)))
                .thenReturn("registerPage");

        try (MockedStatic<LocaleContextHolder> localeHolder = mockStatic(LocaleContextHolder.class)) {
            localeHolder.when(LocaleContextHolder::getLocale).thenReturn(locale);
            
            authController.register(registeredUser, bindingResult, multipartFile);

            assertEquals("hashed_secret", registeredUser.getPassword());
        }
    }

    @Test
    void verify_validEmailAndCode_returnsViewFromUserService() {
        String email = "user@example.com";
        String code = "123456";

        when(userService.verifyUser(email, code)).thenReturn("redirect:/loginPage?msg=verified");

        String view = authController.verify(email, code);

        assertEquals("redirect:/loginPage?msg=verified", view);
        verify(userService).verifyUser(email, code);
    }

    @Test
    void verify_invalidCode_returnsErrorViewFromService() {
        String email = "user@example.com";
        String code = "wrong";

        when(userService.verifyUser(email, code)).thenReturn("redirect:/verify?email=user@example.com");

        String view = authController.verify(email, code);

        assertEquals("redirect:/verify?email=user@example.com", view);
        verify(userService).verifyUser(eq(email), eq(code));
    }

    @Test
    void verify_serviceCalledWithCorrectArguments() {
        String email = "admin@test.com";
        String code = "ABC123";

        when(userService.verifyUser(email, code)).thenReturn("loginPage");

        authController.verify(email, code);

        verify(userService).verifyUser(eq("admin@test.com"), eq("ABC123"));
    }
}