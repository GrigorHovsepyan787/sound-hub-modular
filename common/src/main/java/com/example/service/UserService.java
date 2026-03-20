package com.example.service;

import com.example.model.User;
import com.example.model.UserType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

public interface UserService {
    Page<User> findAll(Pageable pageable);

    void save(User user, MultipartFile multipartFile);

    void add(User user, MultipartFile multipartFile);

    void deleteById(Integer id);

    void banUser(Integer id);

    void unbanUser(Integer id);

    void updateType(Integer id, UserType userType);

    void update(Integer id, User user);

    Optional<User> findByUsername(String username);

    Page<User> findAllWithSpecification(Specification<User> spec, PageRequest pageRequest);
}
