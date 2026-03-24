package com.example.service.impl;

import com.example.dto.UserSearchCriteria;
import com.example.model.User;
import com.example.model.UserStatus;
import com.example.model.UserType;
import com.example.repository.UserRepository;
import com.example.service.UserService;
import com.example.service.specification.UserSpecification;
import com.example.storage.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final StorageService storageService;

    @Override
    public Page<User> findUsersPage(Pageable pageable, UserSearchCriteria criteria) {
        boolean emptyCriteria = (criteria.getName() == null && criteria.getUsername() == null && criteria.getEmail() == null);

        if (emptyCriteria) {
            return userRepository.findAll(pageable);
        } else {
            UserSpecification userSpecification = new UserSpecification(criteria);
            return userRepository.findAll(userSpecification, pageable);
        }
    }

    @Override
    public void save(User user, MultipartFile multipartFile) {
        if (multipartFile != null && !multipartFile.isEmpty()) {
            String imageUrl = storageService.upload(multipartFile, "user-images");

            if (imageUrl != null) {
                user.setPictureUrl(imageUrl);
                log.info("Image uploaded for user: {}", user.getName());
            }else{
                user.setPictureUrl("https://soundhub7.s3.eu-north-1.amazonaws.com/assets/UserDefault.png");
            }
        }
        user.setUserStatus(UserStatus.ENABLED);
        user.setUserType(UserType.USER);
        userRepository.save(user);
    }

    @Override
    public void update(Integer id, UserType userType) {
        User user = userRepository.findById(id).orElseThrow();
        user.setUserType(userType);
        userRepository.save(user);
    }

    @Override
    public void update(Integer id, UserStatus userStatus) {
        User user = userRepository.findById(id).orElseThrow();
        user.setUserStatus(userStatus);
        userRepository.save(user);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}
