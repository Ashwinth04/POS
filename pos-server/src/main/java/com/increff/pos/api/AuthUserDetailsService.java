package com.increff.pos.api;

import com.increff.pos.config.SupervisorConfig;
import com.increff.pos.constants.Constants;
import com.increff.pos.dao.UserDao;
import com.increff.pos.db.documents.UserPojo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Objects;

@Service
public class AuthUserDetailsService implements UserDetailsService {

    @Autowired
    private SupervisorConfig supervisorConfig;

    @Autowired
    private UserDao userDao;

    @Autowired
    private PasswordEncoder encoder;

    public UserDetails loadUserByUsername(String username) {

        if (username.equals(supervisorConfig.getEmail())) {
            return User.builder()
                    .username(supervisorConfig.getEmail())
                    .password(encoder.encode(supervisorConfig.getPassword()))
                    .roles(Constants.SUPERVISOR)
                    .build();
        }

        UserPojo user = getCheckByUsername(username);

        return User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .roles(Constants.OPERATOR)
                .build();
    }

    private UserPojo getCheckByUsername(String username) {

        UserPojo user = userDao.findByEmail(username);

        if (Objects.isNull(user)) {
            throw new UsernameNotFoundException("User with the given email not found");
        }

        return user;
    }
}
