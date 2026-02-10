package com.increff.pos.api;

import com.increff.pos.db.documents.OrderPojo;
import com.increff.pos.exception.ApiException;

public interface OrderApi {
    OrderPojo saveOrder(OrderPojo orderPojo) throws ApiException;
}
