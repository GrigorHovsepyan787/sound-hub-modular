package com.example.service;

import com.example.dto.UserSearchCriteria;
import com.example.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

public interface UserService {
    Page<User> findUsersPage(Pageable pageable, UserSearchCriteria criteria);

    void save(User user, MultipartFile multipartFile);

    void update(User user);

    Optional<User> findByUsername(String username);
}
