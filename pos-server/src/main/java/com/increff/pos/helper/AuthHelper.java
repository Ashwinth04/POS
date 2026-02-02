package com.increff.pos.helper;
import com.increff.pos.db.UserPojo;
import com.increff.pos.model.data.OperatorData;

public class AuthHelper {
    public static OperatorData convertToData(UserPojo userPojo) {
        OperatorData operatorData = new OperatorData();
        operatorData.setUsername(userPojo.getUsername());
        operatorData.setStatus(userPojo.getStatus());
        return operatorData;
    }
}