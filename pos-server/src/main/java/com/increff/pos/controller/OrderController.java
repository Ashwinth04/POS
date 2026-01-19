package com.increff.pos.controller;

import com.increff.pos.dto.OrderDto;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.data.*;
import com.increff.pos.model.data.OrderStatusData;
import com.increff.pos.model.form.OrderForm;
import com.increff.pos.model.form.PageForm;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
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
    public OrderStatusData createOrder(@RequestBody OrderForm orderForm) throws ApiException {
        return orderDto.create(orderForm);
    }

    @Operation(summary = "Get all orders with pagination")
    @RequestMapping(path = "/get-all-paginated", method = RequestMethod.POST)
    public Page<OrderData> getAllProducts(@RequestBody PageForm form) throws ApiException {
        return orderDto.getAll(form);
    }
}
