package com.increff.pos.helper;
import com.increff.pos.db.documents.UserPojo;
import com.increff.pos.model.data.OperatorData;

public class AuthHelper {
    public static OperatorData convertToData(UserPojo userPojo) {
        OperatorData operatorData = new OperatorData();
        operatorData.setUsername(userPojo.getEmail());
        return operatorData;
    }

    public static UserPojo createUserPojo(String encodedPassword, String email, String role) {
        UserPojo user = new UserPojo();
        user.setPassword(encodedPassword);
        user.setEmail(email);
        user.setRole(role);
        return user;
    }
}