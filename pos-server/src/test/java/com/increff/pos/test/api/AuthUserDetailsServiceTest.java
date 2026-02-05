package com.increff.pos.test.api;

import com.increff.pos.api.AuthUserDetailsService;
import com.increff.pos.config.SupervisorConfig;
import com.increff.pos.constants.Constants;
import com.increff.pos.dao.UserDao;
import com.increff.pos.db.UserPojo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthUserDetailsServiceTest {

    @Mock
    private SupervisorConfig supervisorConfig;

    @Mock
    private UserDao userDao;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthUserDetailsService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // ------------------------------------------------
    // Supervisor login
    // ------------------------------------------------

    @Test
    void testLoadUserByUsername_supervisor() {

        when(supervisorConfig.getEmail()).thenReturn("admin");
        when(supervisorConfig.getPassword()).thenReturn("secret");
        when(passwordEncoder.encode("secret")).thenReturn("ENCODED");

        UserDetails user =
                service.loadUserByUsername("admin");

        assertThat(user.getUsername()).isEqualTo("admin");
        assertThat(user.getPassword()).isEqualTo("ENCODED");
        assertThat(user.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_" + Constants.SUPERVISOR);

        verifyNoInteractions(userDao);
    }

    // ------------------------------------------------
    // Operator login
    // ------------------------------------------------

    @Test
    void testLoadUserByUsername_operator_active() {

        when(supervisorConfig.getEmail()).thenReturn("admin");

        UserPojo pojo = new UserPojo();
        pojo.setEmail("operator1");
        pojo.setPassword("pwd");

        when(userDao.findByEmail("operator1"))
                .thenReturn(pojo);

        UserDetails user =
                service.loadUserByUsername("operator1");

        assertThat(user.getUsername()).isEqualTo("operator1");
        assertThat(user.getPassword()).isEqualTo("pwd");
        assertThat(user.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_" + Constants.OPERATOR);
    }

    // ------------------------------------------------
    // Failure cases
    // ------------------------------------------------

    @Test
    void testLoadUserByUsername_userNotFound() {

        when(supervisorConfig.getEmail()).thenReturn("adminn@gmail.com");
        when(userDao.findByEmail("missing"))
                .thenReturn(null);

        assertThatThrownBy(() ->
                service.loadUserByUsername("missing")
        )
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User with the given email not found");
    }
}
