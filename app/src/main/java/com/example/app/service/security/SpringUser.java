package com.example.app.service.security;

import com.example.model.User;
import lombok.Data;
import org.springframework.security.core.authority.AuthorityUtils;

@Data
public class SpringUser extends org.springframework.security.core.userdetails.User {

    private final User user;

    public SpringUser(User user) {
        super(user.getUsername(),
                user.getPassword(),
                AuthorityUtils.createAuthorityList(user.getUserType().name()));
        this.user = user;
    }
}