package com.increff.pos.controller;

import com.increff.pos.dto.OrderDto;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.data.OrderData;
import com.increff.pos.model.form.OrderForm;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Order Management", description = "Create, view and filter orders")
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderDto orderDto;

    @Operation(summary = "Create new order")
    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public OrderData createOrder(@RequestBody OrderForm orderForm) throws ApiException {
        return orderDto.create(orderForm);
    }

}
