package com.increff.pos.test.api;

import com.increff.pos.api.AuthApiImpl;
import com.increff.pos.dao.UserDao;
import com.increff.pos.db.documents.UserPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.data.LoginResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthApiImplTest {

    @InjectMocks
    private AuthApiImpl authApi;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserDao userDao;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    // ---------- login success ----------
    @Test
    void testLoginSuccess() throws Exception {
        Authentication auth = mock(Authentication.class);
        GrantedAuthority authority = () -> "ROLE_OPERATOR";

        Collection<GrantedAuthority> authorities = List.of(authority);

        when(authenticationManager.authenticate(any()))
                .thenReturn(auth);
        when(auth.getAuthorities())
                .thenAnswer(invocation -> List.of(authority));
        when(auth.getName()).thenReturn("user@example.com");

        LoginResponse result =
                authApi.login("user@example.com", "pass", request, response);

        assertEquals("user@example.com", result.getEmail());
        assertEquals("ROLE_OPERATOR", result.getRole());
        verify(authenticationManager).authenticate(any());
    }

    // ---------- login failure ----------
    @Test
    void testLoginFailure() throws Exception {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("bad"));

        ApiException ex = assertThrows(ApiException.class,
                () -> authApi.login("user@example.com", "pass", request, response));

        assertEquals("Invalid username or password", ex.getMessage());
    }

    // ---------- createOperator success ----------
    @Test
    void testCreateOperatorSuccess() throws Exception {
        UserPojo pojo = new UserPojo();
        pojo.setEmail("user@example.com");

        when(userDao.findByEmail("user@example.com")).thenReturn(null);

        authApi.createOperator(pojo);

        verify(userDao).save(pojo);
    }

    // ---------- createOperator user exists ----------
    @Test
    void testCreateOperatorUserExists() {
        UserPojo pojo = new UserPojo();
        pojo.setEmail("user@example.com");

        when(userDao.findByEmail("user@example.com")).thenReturn(new UserPojo());

        assertThrows(ApiException.class,
                () -> authApi.createOperator(pojo));

        verify(userDao, never()).save(any());
    }

    // ---------- getCurrentUser ----------
    @Test
    void testGetCurrentUser() {
        GrantedAuthority authority = () -> "ROLE_OPERATOR";

        Authentication auth = mock(Authentication.class);
        Collection<GrantedAuthority> authorities = List.of(authority);

        when(auth.getName()).thenReturn("user@example.com");
        when(auth.getAuthorities())
                .thenAnswer(invocation -> List.of(authority));

        LoginResponse result = authApi.getCurrentUser(auth);

        assertEquals("user@example.com", result.getEmail());
        assertEquals("ROLE_OPERATOR", result.getRole());
    }

    // ---------- getAllOperators ----------
    @Test
    void testGetAllOperators() {
        Page<UserPojo> page = new PageImpl<>(List.of(new UserPojo()));

        when(userDao.findAll(any(Pageable.class))).thenReturn(page);

        Page<UserPojo> result = authApi.getAllOperators(0, 10);

        assertEquals(1, result.getContent().size());
        verify(userDao).findAll(any(Pageable.class));
    }

    // ---------- checkUserExists ----------
    @Test
    void testCheckUserExistsPass() throws Exception {
        when(userDao.findByEmail("user@example.com")).thenReturn(null);

        authApi.checkUserExists("user@example.com");

        verify(userDao).findByEmail("user@example.com");
    }

    @Test
    void testCheckUserExistsThrows() {
        when(userDao.findByEmail("user@example.com")).thenReturn(new UserPojo());

        ApiException ex = assertThrows(ApiException.class,
                () -> authApi.checkUserExists("user@example.com"));

        assertEquals("User already exists", ex.getMessage());
    }
}
