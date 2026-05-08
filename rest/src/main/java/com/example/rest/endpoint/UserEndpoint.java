package com.example.rest.endpoint;

import com.example.dto.LoginRequest;
import com.example.dto.RegisterRequest;
import com.example.dto.TokenRegisterRequest;
import com.example.mapper.RegisterRequestMapper;
import com.example.model.User;
import com.example.repository.UserRepository;
import com.example.rest.util.JwtTokenUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserEndpoint {
    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtTokenUtil tokenUtil;

    private final RegisterRequestMapper userMapper;

    @GetMapping("/test")
    public String test() {
        Authentication auth =
                SecurityContextHolder.getContext().getAuthentication();

        System.out.println("AUTH IN CONTROLLER = " + auth);

        return "OK";
    }

    @PostMapping("/auth")
    public ResponseEntity<TokenRegisterRequest> login(@RequestBody LoginRequest request) {

        Optional<User> byUsername = userRepository.findByUsername(request.getUsername());

        if (byUsername.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .build();
        }

        User user = byUsername.get();
        if (passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity
                    .ok(TokenRegisterRequest.builder()
                            .token(tokenUtil.generateToken(user.getUsername()))
                            .name(user.getName())
                            .surname(user.getSurname())
                            .userId(user.getId())
                            .build());
        }

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .build();
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody @Valid RegisterRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .build();
        }
        request.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(userMapper.toEntity(request));
        return ResponseEntity
                .ok()
                .build();
    }
}
