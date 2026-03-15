package com.example.service;

import com.example.model.User;
import com.example.model.UserType;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface UserService {
    List<User> findAll();

    void save(User User, MultipartFile multipartfile);

    void deleteById(Integer id);

    void banUser(Integer id);

    void unbanUser(Integer id);

    void updateType(Integer id, UserType userType);

    void update(Integer id, User user);

    Optional<User> findByUsername(String username);
}
