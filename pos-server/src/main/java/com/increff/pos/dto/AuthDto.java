package com.increff.pos.dto;

import com.increff.pos.api.AuthApiImpl;
import com.increff.pos.db.UserPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.helper.AuthHelper;
import com.increff.pos.model.data.LoginResponse;
import com.increff.pos.model.data.OperatorData;
import com.increff.pos.model.form.CreateUserRequest;
import com.increff.pos.model.form.LoginRequest;
import com.increff.pos.model.form.PageForm;
import com.increff.pos.util.ValidationUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class AuthDto {

    @Autowired
    private AuthApiImpl authApi;

    public LoginResponse login(LoginRequest request, HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws ApiException {

        ValidationUtil.validateLoginRequest(request);

        String username = request.getUsername().trim().toLowerCase();
        String password = request.getPassword();

        return authApi.login(username, password, httpRequest, httpResponse);
    }

    public void createOperator(CreateUserRequest request) throws ApiException {
        try {
            // 1. Validate
            if (request.getUsername() == null || request.getUsername().isBlank()) {
                throw new ApiException("Username missing");
            }

            if (request.getPassword() == null || request.getPassword().isBlank()) {
                throw new ApiException("Password missing");
            }

            // 2. Sanitize
            String username = request.getUsername().trim().toLowerCase();
            String password = request.getPassword();

            // 3. Delegate to API
            authApi.createOperator(username, password);

        } catch (Exception e) {
            e.printStackTrace();   // 👈 your important line preserved
            throw e;
        }
    }

    public LoginResponse me(Authentication authentication) throws ApiException {

        if (authentication == null) {
            throw new ApiException("Not logged in");
        }

        return authApi.me(authentication);
    }

    public Page<OperatorData> getAllOperators(PageForm form) throws ApiException {
        Page<UserPojo> operatorPage = authApi.getAllOperators(form.getPage(), form.getSize());
        return operatorPage.map(AuthHelper::convertToData);
    }
}
