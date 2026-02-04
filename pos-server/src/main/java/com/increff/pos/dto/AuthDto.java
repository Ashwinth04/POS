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
import com.increff.pos.util.FormValidator;
import com.increff.pos.util.NormalizationUtil;
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

    @Autowired
    private FormValidator formValidator;

    public LoginResponse login(LoginRequest request, HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws ApiException {

        formValidator.validate(request);
        String email = request.getEmail();
        NormalizationUtil.normalizeEmail(email);
        String password = request.getPassword();

        return authApi.login(email, password, httpRequest, httpResponse);
    }

    public void createOperator(CreateUserRequest request) throws ApiException {

        formValidator.validate(request);
        String email = request.getEmail();
        NormalizationUtil.normalizeEmail(email);
        String password = request.getPassword();

        authApi.createOperator(email, password);
    }

    public LoginResponse me(Authentication authentication) throws ApiException {

        ValidationUtil.validateAuthentication(authentication);
        return authApi.me(authentication);
    }

    public Page<OperatorData> getAllOperators(PageForm form) throws ApiException {

        formValidator.validate(form);
        Page<UserPojo> operatorPage = authApi.getAllOperators(form.getPage(), form.getSize());
        return operatorPage.map(AuthHelper::convertToData);
    }
}
