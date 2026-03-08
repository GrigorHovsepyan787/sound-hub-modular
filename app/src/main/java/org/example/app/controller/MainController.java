package org.example.app.controller;


import com.example.model.User;
import com.example.model.UserType;
import com.example.service.UserService;
import lombok.RequiredArgsConstructor;
import org.example.app.service.security.SpringUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class MainController {
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
    public String register(@ModelAttribute User registeredUser) {
        if (userService.findByUsername(registeredUser.getUsername()).isPresent()) {
            return "redirect:/registerPage?msg=Username already exists!";
        }
        registeredUser.setPassword(passwordEncoder.encode(registeredUser.getPassword()));
        userService.save(registeredUser);
        return "redirect:/loginPage?msg=Registration successful, pls login!";
    }
}
