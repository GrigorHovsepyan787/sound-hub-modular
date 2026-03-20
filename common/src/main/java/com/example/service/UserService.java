package com.example.service;

import com.example.dto.UserSearchCriteria;
import com.example.model.User;
import com.example.model.UserStatus;
import com.example.model.UserType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

public interface UserService {
    Page<User> findUsersPage(Pageable pageable, UserSearchCriteria criteria);

    void save(User user, MultipartFile multipartFile);

    void updateStatus(Integer id, UserStatus status);

    void updateType(Integer id, UserType userType);

    Optional<User> findByUsername(String username);
}
