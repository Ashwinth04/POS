package com.increff.pos.controller;
import com.increff.pos.dao.UserDao;
import com.increff.pos.db.UserPojo;
import com.increff.pos.model.data.LoginResponse;
import com.increff.pos.model.form.CreateUserRequest;
import com.increff.pos.model.form.LoginRequest;
import com.increff.pos.security.Roles;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDao userDao;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(auth);

            HttpSessionSecurityContextRepository repo = new HttpSessionSecurityContextRepository();
            repo.saveContext(SecurityContextHolder.getContext(), httpRequest, httpResponse);

            return new LoginResponse(
                    auth.getName(),
                    auth.getAuthorities().iterator().next().getAuthority()
            );

        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid username or password"
            );
        }
    }

    @GetMapping("/me")
    public LoginResponse me(Authentication authentication) {

        if (authentication == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not logged in");
        }

        return new LoginResponse(
                authentication.getName(),
                authentication.getAuthorities().iterator().next().getAuthority()
        );
    }

    @PostMapping("/create-operator")
    public void createOperator(@RequestBody CreateUserRequest request) {
        try {
            System.out.println("REQUEST = " + request.getUsername());

            if (request.getUsername() == null || request.getUsername().isBlank()) {
                throw new RuntimeException("Username missing");
            }

            if (request.getPassword() == null || request.getPassword().isBlank()) {
                throw new RuntimeException("Password missing");
            }

            if (userDao.findByUsername(request.getUsername()) != null) {
                throw new RuntimeException("User already exists");
            }

            UserPojo user = new UserPojo();
            user.setUsername(request.getUsername());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setRole(Roles.OPERATOR);

            userDao.save(user);

            System.out.println("USER SAVED SUCCESSFULLY");

        } catch (Exception e) {
            e.printStackTrace();   // 👈 THIS IS THE IMPORTANT LINE
            throw e;
        }
    }

}

