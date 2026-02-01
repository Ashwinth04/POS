package com.increff.pos.api;

import com.increff.pos.db.OrderPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.data.OrderStatus;

import java.util.Map;

public interface OrderApi {
    OrderPojo placeOrder(OrderPojo orderPojo, boolean isFulfillable) throws ApiException;
}
