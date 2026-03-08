package com.example.service;

import com.example.model.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    List<User> findAll();

    void save(User User);

    void deleteById(Integer id);

    void update(Integer id, User user);

    Optional<User> findByUsername(String username);
}
