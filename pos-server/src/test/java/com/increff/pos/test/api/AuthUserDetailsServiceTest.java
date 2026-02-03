package com.increff.pos.test.api;

import com.increff.pos.api.AuthUserDetailsService;
import com.increff.pos.config.SupervisorConfig;
import com.increff.pos.constants.Constants;
import com.increff.pos.dao.UserDao;
import com.increff.pos.db.UserPojo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

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

        when(supervisorConfig.getUsername()).thenReturn("admin");
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

        when(supervisorConfig.getUsername()).thenReturn("admin");

        UserPojo pojo = new UserPojo();
        pojo.setUsername("operator1");
        pojo.setPassword("pwd");
        pojo.setStatus("ACTIVE");

        when(userDao.findByUsername("operator1"))
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

        when(supervisorConfig.getUsername()).thenReturn("admin");
        when(userDao.findByUsername("missing"))
                .thenReturn(null);

        assertThatThrownBy(() ->
                service.loadUserByUsername("missing")
        )
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("Username not found");
    }

    @Test
    void testLoadUserByUsername_userInactive() {

        when(supervisorConfig.getUsername()).thenReturn("admin");

        UserPojo pojo = new UserPojo();
        pojo.setUsername("operator2");
        pojo.setStatus("INACTIVE");

        when(userDao.findByUsername("operator2"))
                .thenReturn(pojo);

        assertThatThrownBy(() ->
                service.loadUserByUsername("operator2")
        )
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("Username not found");
    }
}
