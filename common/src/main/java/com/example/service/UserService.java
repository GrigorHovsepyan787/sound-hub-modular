package com.example.service;

import com.example.dto.AdminDashboardStats;
import com.example.dto.RegisterRequest;
import com.example.dto.UserSearchCriteria;
import com.example.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import java.util.Locale;
import java.util.Optional;

public interface UserService {
    Page<User> findUsersPage(Pageable pageable, UserSearchCriteria criteria);

    void isRegisterRequestPresent(ModelMap modelMap);

    String save(RegisterRequest registerRequest, MultipartFile multipartFile, Locale locale, BindingResult bindingResult);

    void update(User user);

    Optional<User> findByUsername(String username);

    String verifyUser(String email, String code);

    AdminDashboardStats getAdminDashboardStats();
}
