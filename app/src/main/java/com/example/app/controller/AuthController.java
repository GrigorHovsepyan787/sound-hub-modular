package com.example.app.controller;

import com.example.app.service.security.SpringUser;
import com.example.model.User;
import com.example.model.UserType;
import com.example.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

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
        modelMap.addAttribute("msg", msg);
        return "registerPage";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute User registeredUser, @RequestParam("pic") MultipartFile multipartfile) {
        if (userService.findByUsername(registeredUser.getUsername()).isPresent()) {
            return "redirect:/registerPage?msg=Username already exists!";
        }
        registeredUser.setPassword(passwordEncoder.encode(registeredUser.getPassword()));
        userService.save(registeredUser, multipartfile);
        return "redirect:/loginPage?msg=Registration successful, pls login!";
    }
}
