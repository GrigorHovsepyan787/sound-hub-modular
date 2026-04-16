package com.example.app.controller;


import com.example.dto.UserSearchCriteria;
import com.example.model.User;
import com.example.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;

    @GetMapping("/admin/home")
    public String home(ModelMap modelMap) {
        modelMap.addAttribute("totalListening", userService.getAdminDashboardStats().totalListening());
        modelMap.addAttribute("listeningGrowth", userService.getAdminDashboardStats().listeningGrowthPercent());
        modelMap.addAttribute("totalUsers", userService.getAdminDashboardStats().totalUsers());
        modelMap.addAttribute("usersGrowth", userService.getAdminDashboardStats().usersGrowthPercent());
        modelMap.addAttribute("totalArtists", userService.getAdminDashboardStats().totalArtists());
        modelMap.addAttribute("artistsGrowth", userService.getAdminDashboardStats().artistsGrowthPercent());
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

    @PostMapping("/admin/users/update")
    public String updateUser(@ModelAttribute User user) {
        userService.update(user);
        return "redirect:/admin/users";
    }
}
