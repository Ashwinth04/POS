package com.increff.pos.controller;
import com.increff.pos.dao.UserDao;
import com.increff.pos.db.UserPojo;
import com.increff.pos.dto.AuthDto;
import com.increff.pos.exception.ApiException;
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

    private final AuthDto authDto;

    public AuthController(AuthDto authDto) {
        this.authDto = authDto;
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request, HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws ApiException {
        return authDto.login(request, httpRequest, httpResponse);
    }

    @GetMapping("/me")
    public LoginResponse me(Authentication authentication) throws ApiException {
        return authDto.me(authentication);
    }


//    @GetMapping("/me")
//    public LoginResponse me(Authentication authentication) {
//
//        if (authentication == null) {
//            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not logged in");
//        }
//
//        return new LoginResponse(
//                authentication.getName(),
//                authentication.getAuthorities().iterator().next().getAuthority()
//        );
//    }

    @PostMapping("/create-operator")
    public void createOperator(@RequestBody CreateUserRequest request) throws ApiException {
        authDto.createOperator(request);
    }

}

