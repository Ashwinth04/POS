package com.increff.pos.api;

import com.increff.pos.db.OrderPojo;
import com.increff.pos.exception.ApiException;

public interface OrderApi {
    OrderPojo add(OrderPojo orderPojo) throws ApiException;
}
