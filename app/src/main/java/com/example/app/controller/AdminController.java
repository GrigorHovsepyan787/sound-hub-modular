package com.example.app.controller;


import com.example.model.UserType;
import com.example.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;

    @GetMapping("/admin/home")
    public String home() {
        return "adminPage";
    }

    @GetMapping("/admin/users")
    public String users() {
        return "users";
    }

    @PatchMapping("/admin/users/{id}/ban")
    public String banUser(@PathVariable Integer id) {
        userService.banUser(id);
        return "redirect:/admin/users/";
    }

    @PatchMapping("/admin/users/{id}/unban")
    public String unbanUser(@PathVariable Integer id) {
        userService.unbanUser(id);
        return "redirect:/admin/users/";
    }

    @PatchMapping("/admin/users/{id}/update-type")
    public String makeAdmin(@PathVariable Integer id, @RequestParam("userType") String userType) {
        userService.updateType(id, UserType.valueOf(userType));
        return "redirect:/admin/users/";
    }
}
