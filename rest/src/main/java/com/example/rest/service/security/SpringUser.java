package com.example.rest.service.security;

import com.example.model.User;
import com.example.model.UserStatus;
import lombok.Getter;
import org.springframework.security.core.authority.AuthorityUtils;

@Getter
public class SpringUser extends org.springframework.security.core.userdetails.User {

    private final User user;

    @Override
    public boolean isEnabled() {
        return user.getUserStatus() == UserStatus.ENABLED;
    }

    public SpringUser(User user) {
        super(user.getUsername(),
                user.getPassword(),
                AuthorityUtils.createAuthorityList(user.getUserType().name()));
        this.user = user;

    }
}