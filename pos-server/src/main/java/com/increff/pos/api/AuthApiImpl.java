package com.increff.pos.api;

import com.increff.pos.dao.UserDao;
import com.increff.pos.db.ClientPojo;
import com.increff.pos.db.UserPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.data.LoginResponse;
import com.increff.pos.security.Roles;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthApiImpl {

    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final UserDao userDao;

    public AuthApiImpl(AuthenticationManager authenticationManager, UserDao userDao, PasswordEncoder passwordEncoder)  {
        this.authenticationManager = authenticationManager;
        this.userDao = userDao;
        this.passwordEncoder = passwordEncoder;
    }

    public LoginResponse login(String username, String password, HttpServletRequest request, HttpServletResponse response) throws ApiException {

        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );

            // Set authentication into context
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(auth);
            SecurityContextHolder.setContext(context);

            // Persist session
            HttpSessionSecurityContextRepository repo = new HttpSessionSecurityContextRepository();
            repo.saveContext(context, request, response);

            // Build response
            String role = auth.getAuthorities()
                    .iterator()
                    .next()
                    .getAuthority();

            return new LoginResponse(auth.getName(), role);

        } catch (AuthenticationException e) {
            e.printStackTrace();
            throw new ApiException("Invalid username or password");
        }
    }

    public void createOperator(String username, String password) throws ApiException {

        if (userDao.findByUsername(username) != null) {
            throw new ApiException("User already exists");
        }

        UserPojo user = new UserPojo();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(Roles.OPERATOR);
        user.setStatus("ACTIVE");

        userDao.save(user);

        System.out.println("USER SAVED SUCCESSFULLY");
    }

    public LoginResponse me(Authentication authentication) {

        String username = authentication.getName();
        String role = authentication.getAuthorities()
                .iterator()
                .next()
                .getAuthority();

        return new LoginResponse(username, role);
    }

    @Transactional(rollbackFor = ApiException.class)
    public Page<UserPojo> getAllOperators(int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return userDao.findAll(pageRequest);
    }
}
