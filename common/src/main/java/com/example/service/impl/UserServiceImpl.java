package com.example.service.impl;

import com.example.model.User;
import com.example.model.UserStatus;
import com.example.model.UserType;
import com.example.repository.UserRepository;
import com.example.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    @Value("${sound-hub-modular.upload.image.directory.path}")
    private String imageDirectoryPath;

    private final UserRepository userRepository;

    @Override
    public Page<User> findAll(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Override
    public void save(User user, MultipartFile multipartFile) {
        if (multipartFile != null && !multipartFile.isEmpty()) {
            String fileName = System.currentTimeMillis() + "_" + multipartFile.getOriginalFilename();
            File file = new File(imageDirectoryPath + fileName);
            try {
                multipartFile.transferTo(file);
                user.setPictureName(fileName);
            } catch (IOException e) {
                log.error("Error while saving image for user {}: {}, {}", user.getEmail(), e.getMessage(), e.getStackTrace());
            }
        }
        userRepository.save(user);
    }

    @Override
    public void updateType(Integer id, UserType userType) {
        User user = userRepository.findById(id).orElseThrow();
        user.setUserType(userType);
        userRepository.save(user);
    }

    @Override
    public void banUser(Integer id) {
        User user = userRepository.findById(id).orElseThrow();
        user.setUserStatus(UserStatus.BANNED);
    }

    @Override
    public void unbanUser(Integer id) {
        User user = userRepository.findById(id).orElseThrow();
        user.setUserStatus(UserStatus.ENABLED);
    }

    @Override
    public void deleteById(Integer id) {
        User user = userRepository.findById(id).orElseThrow();
        user.setUserStatus(UserStatus.DELETED);
    }

    @Override
    public void update(Integer id, User newUser) {
        User user = userRepository.findById(id).orElseThrow();
        user.setEmail(newUser.getEmail());
        user.setName(newUser.getName());
        user.setUserType(newUser.getUserType());
        user.setPassword(newUser.getPassword());
        user.setSurname(newUser.getSurname());
        user.setUsername(newUser.getUsername());
        userRepository.save(user);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public Page<User> findAllWithSpecification(Specification<User> spec) {
        return userRepository.findAll(spec, Pageable.unpaged());
    }
}
