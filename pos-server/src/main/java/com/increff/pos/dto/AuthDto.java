package com.increff.pos.dto;

import com.increff.pos.api.AuthApiImpl;
import com.increff.pos.constants.UserRole;
import com.increff.pos.db.documents.UserPojo;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthDto {

    @Autowired
    private AuthApiImpl authApi;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public LoginResponse login(LoginRequest request, HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws ApiException {
        NormalizationUtil.normalizeLoginRequest(request);
        FormValidator.validate(request);
        return authApi.login(request.getEmail(), request.getPassword(), httpRequest, httpResponse);
    }

    public void createOperator(CreateUserRequest request) throws ApiException {
        NormalizationUtil.normalizeCreateOperator(request);
        FormValidator.validate(request);
        String password = passwordEncoder.encode(request.getPassword());
        String role = UserRole.OPERATOR.role();
        UserPojo userPojo = AuthHelper.createUserPojo(password, request.getEmail(), role);
        authApi.createOperator(userPojo);
    }

    public LoginResponse getCurrentUser(Authentication authentication) throws ApiException {
        ValidationUtil.validateAuthentication(authentication);
        return authApi.getCurrentUser(authentication);
    }

    public Page<OperatorData> getAllOperators(PageForm form) throws ApiException {
        FormValidator.validate(form);
        Page<UserPojo> operatorPage = authApi.getAllOperators(form.getPage(), form.getSize());
        return operatorPage.map(AuthHelper::convertToData);
    }
}
