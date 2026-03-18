package com.example.app.controller;


import com.example.dto.UserSearchCriteria;
import com.example.model.User;
import com.example.model.UserType;
import com.example.service.UserService;
import com.example.service.specification.UserSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

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
                        @RequestParam("page") Optional<Integer> page,
                        @RequestParam("size") Optional<Integer> size,
                        @ModelAttribute UserSearchCriteria criteria
    ) {
        if (criteria.getName() == null && criteria.getUsername() == null && criteria.getEmail() == null) {
            int currentPage = page.orElse(1);
            int pageSize = size.orElse(5);
            Sort sort = Sort.by(Sort.Direction.DESC, "id");

            PageRequest pageRequest = PageRequest.of(currentPage - 1, pageSize, sort);

            Page<User> result = userService.findAll(pageRequest);
            int totalPages = result.getTotalPages();

            if (totalPages == 0) {
                List<Integer> pageNumbers = IntStream.rangeClosed(1, currentPage)
                        .boxed()
                        .toList();
                modelMap.addAttribute("pageNumbers", pageNumbers);
            }

            modelMap.addAttribute("users", result);
        } else {
            UserSpecification userSpecification = new UserSpecification(criteria);
            Page<User> result = userService.findAllWithSpecification(userSpecification);
            modelMap.addAttribute("users", result);
            modelMap.addAttribute("criteria", criteria);
        }

        return "users";
    }

    @PostMapping("/admin/users/{id}/delete")
    public String deleteUser(@PathVariable Integer id) {
        userService.deleteById(id);
        return "redirect:/admin/users";
    }

    @PostMapping("/admin/users/{id}/ban")
    public String banUser(@PathVariable Integer id) {
        userService.banUser(id);
        return "redirect:/admin/users";
    }

    @PostMapping("/admin/users/{id}/unban")
    public String unbanUser(@PathVariable Integer id) {
        userService.unbanUser(id);
        return "redirect:/admin/users";
    }

    @PostMapping("/admin/users/{id}/update-type")
    public String makeAdmin(@PathVariable Integer id, @RequestParam("userType") String userType) {
        userService.updateType(id, UserType.valueOf(userType));
        return "redirect:/admin/users";
    }
}
