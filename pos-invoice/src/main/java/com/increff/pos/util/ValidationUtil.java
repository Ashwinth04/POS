package com.increff.pos.util;

import com.increff.pos.exception.ApiException;

public class ValidationUtil {

    public static void validateOrderId(String orderId) throws ApiException {

        if (orderId.length() != 24) throw new ApiException("Not a valid order id");

    }
}
