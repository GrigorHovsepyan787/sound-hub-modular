package com.example.app.controller;


import com.example.dto.UserSearchCriteria;
import com.example.model.UserStatus;
import com.example.model.UserType;
import com.example.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
    public String users(ModelMap modelMap,
                        @PageableDefault(size = 5, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
                        @ModelAttribute UserSearchCriteria criteria
    ) {
        modelMap.addAttribute("users", userService.findUsersPage(pageable, criteria));
        modelMap.addAttribute("criteria", criteria);
        return "users";
    }

    @PostMapping("/admin/users/{id}/delete")
    public String deleteUser(@PathVariable Integer id) {
        userService.update(id, UserStatus.DELETED);
        return "redirect:/admin/users";
    }

    @PostMapping("/admin/users/{id}/ban")
    public String banUser(@PathVariable Integer id) {
        userService.update(id, UserStatus.BANNED);
        return "redirect:/admin/users";
    }

    @PostMapping("/admin/users/{id}/unban")
    public String unbanUser(@PathVariable Integer id) {
        userService.update(id, UserStatus.ENABLED);
        return "redirect:/admin/users";
    }

    @PostMapping("/admin/users/{id}/update-type")
    public String makeAdmin(@PathVariable Integer id, @RequestParam("userType") String userType) {
        userService.update(id, UserType.valueOf(userType));
        return "redirect:/admin/users";
    }
}
