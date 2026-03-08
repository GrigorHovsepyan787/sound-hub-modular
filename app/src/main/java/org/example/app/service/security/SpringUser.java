package org.example.app.service.security;

import com.example.model.User;
import lombok.Getter;
import org.springframework.security.core.authority.AuthorityUtils;

import java.util.Objects;

@Getter
public class SpringUser extends org.springframework.security.core.userdetails.User {

    private final User user;

    public SpringUser(User user) {
        super(user.getUsername(),
                user.getPassword(),
                AuthorityUtils.createAuthorityList(user.getRole().name()));
        this.user = user;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        SpringUser that = (SpringUser) o;
        return Objects.equals(user, that.user);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), user);
    }
}