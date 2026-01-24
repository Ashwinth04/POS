package com.increff.pos.api;

import com.increff.pos.config.SupervisorConfig;
import com.increff.pos.dao.UserDao;
import com.increff.pos.db.UserPojo;
import com.increff.pos.security.Roles;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final SupervisorConfig supervisorConfig;
    private final UserDao userDao;
    private final PasswordEncoder encoder;

    public CustomUserDetailsService(SupervisorConfig supervisorConfig, UserDao userDao, PasswordEncoder encoder) {
        this.supervisorConfig = supervisorConfig;
        this.userDao = userDao;
        this.encoder = encoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {

        // 1. Supervisor
        if (username.equals(supervisorConfig.getUsername())) {
            return User.builder()
                    .username(supervisorConfig.getUsername())
                    .password(encoder.encode(supervisorConfig.getPassword()))
                    .roles(Roles.SUPERVISOR)
                    .build();
        }

        // 2. Operator from DB
        UserPojo user = userDao.findByUsername(username);

        if (user == null) throw new UsernameNotFoundException("Username not found");

        return User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .roles(Roles.OPERATOR)
                .build();
    }
}
