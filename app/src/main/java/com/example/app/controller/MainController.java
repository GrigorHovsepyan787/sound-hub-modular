package com.example.app.controller;


import com.example.model.User;
import com.example.model.UserType;
import com.example.service.UserService;
import lombok.RequiredArgsConstructor;
import com.example.app.service.security.SpringUser;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Controller
@RequiredArgsConstructor
public class MainController {

    @Value("${sound-hub-modular.upload.image.directory.path}")
    private String imageDirectoryPath;

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;


    @GetMapping("/")
    public String mainPage(@AuthenticationPrincipal SpringUser springUser, ModelMap modelMap) {
        if (springUser != null) {
            modelMap.addAttribute("user", springUser.getUser());
        }
        return "feed";
    }

    @GetMapping("/successLogin")
    public String successLogin(@AuthenticationPrincipal SpringUser springUser) {
        if (springUser != null
                && springUser.getUser().getRole() == UserType.ADMIN) {
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

    @GetMapping("/image/get")
    public @ResponseBody byte[] getImage(@RequestParam("picName") String picName) {
        File file = new File(imageDirectoryPath + picName);
        if (file.exists() && file.isFile()) {
            try {
                return FileUtils.readFileToByteArray(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
