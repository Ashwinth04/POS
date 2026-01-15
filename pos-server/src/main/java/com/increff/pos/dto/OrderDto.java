package com.increff.pos.dto;

import com.increff.pos.api.OrderApiImpl;
import com.increff.pos.db.OrderPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.helper.OrderHelper;
import com.increff.pos.model.data.OrderData;
import com.increff.pos.model.form.OrderForm;
import com.increff.pos.util.ValidationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderDto {

    @Autowired
    private OrderApiImpl orderApi;

    public OrderData create(OrderForm orderForm) throws ApiException {
        ValidationUtil.validateOrderForm(orderForm);
        OrderPojo orderPojo = OrderHelper.convertToEntity(orderForm);
        OrderPojo savedOrderPojo = orderApi.add(orderPojo);

        return OrderHelper.convertToDto(savedOrderPojo);
    }

}
