package com.increff.pos.controller;
import com.increff.pos.dao.UserDao;
import com.increff.pos.db.UserPojo;
import com.increff.pos.dto.AuthDto;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.data.LoginResponse;
import com.increff.pos.model.data.OperatorData;
import com.increff.pos.model.form.CreateUserRequest;
import com.increff.pos.model.form.LoginRequest;
import com.increff.pos.model.form.PageForm;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.domain.Page;
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
    private AuthDto authDto;

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request, HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws ApiException {
        return authDto.login(request, httpRequest, httpResponse);
    }

    @GetMapping("/me")
    public LoginResponse me(Authentication authentication) throws ApiException {
        return authDto.me(authentication);
    }

    @PostMapping("/create-operator")
    public void createOperator(@RequestBody CreateUserRequest request) throws ApiException {
        authDto.createOperator(request);
    }

    @RequestMapping(value = "/get-all-operators", method = RequestMethod.POST)
    public Page<OperatorData> getAllOperatorsPaginated(@RequestBody PageForm form) throws ApiException {
        return authDto.getAllOperators(form);
    }

}

