package com.increff.pos.api;

import com.increff.pos.db.OrderPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.data.OrderStatus;

import java.util.Map;

public interface OrderApi {
    Map<String, OrderStatus> placeOrder(OrderPojo orderPojo, Map<String, OrderStatus> statuses, boolean isFulfillable) throws ApiException;
}
