package com.example.app.controller;

import com.example.app.service.security.SpringUser;
import com.example.dto.RegisterRequest;
import com.example.model.UserType;
import com.example.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.Locale;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/successLogin")
    public String successLogin(@AuthenticationPrincipal SpringUser springUser) {
        if (springUser != null
                && springUser.getUser().getUserType() == UserType.ADMIN) {
            return "redirect:/admin/home";
        } else {
            return "redirect:/";
        }
    }


    @GetMapping("/loginPage")
    public String loginPage(@RequestParam(required = false) String msg, ModelMap modelMap) {
        modelMap.addAttribute("msg", msg);
        return "loginPage";
    }

    @GetMapping("/registerPage")
    public String registerPage(@RequestParam(required = false) String msg, ModelMap modelMap) {
        if (!modelMap.containsAttribute("registerRequest")) {
            modelMap.addAttribute("registerRequest", new RegisterRequest());
        }
        modelMap.addAttribute("msg", msg);
        return "registerPage";
    }

    @GetMapping("/verify")
    public String verifyPage(@RequestParam("email") String email, ModelMap modelMap) {
        modelMap.addAttribute("email", email);
        return "verifyPage";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registerRequest") RegisterRequest registeredUser,
                           BindingResult bindingResult,
                           @RequestParam("pic") MultipartFile multipartfile) {
        Locale locale = LocaleContextHolder.getLocale();
        if (userService.findByUsername(registeredUser.getUsername()).isPresent()) {
            return "redirect:/registerPage?msg=Username already exists!";
        }
        if(bindingResult.hasErrors()) {
            return "registerPage";
        }
        registeredUser.setPassword(passwordEncoder.encode(registeredUser.getPassword()));
        userService.save(registeredUser, multipartfile, locale);
        return "redirect:/verify?email=" + registeredUser.getEmail();
    }

    @PostMapping("/verify")
    public String verify(@RequestParam("email") String email, @RequestParam("verificationCode")  String verificationCode) {
        boolean isVerified = userService.verifyUser(email, verificationCode);
        if(isVerified) {
            return "redirect:/loginPage?msg=Verification successful!";
        }
        else {
            return "redirect:/loginPage?msg=Verification failed!";
        }
    }
}
