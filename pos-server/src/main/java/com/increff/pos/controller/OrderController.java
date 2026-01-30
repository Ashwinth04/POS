package com.increff.pos.controller;

import com.increff.pos.dto.OrderDto;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.data.*;
import com.increff.pos.model.data.OrderStatusData;
import com.increff.pos.model.form.OrderForm;
import com.increff.pos.model.form.PageForm;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.apache.coyote.Request;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Tag(name = "Order Management", description = "Create, view and filter orders")
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderDto orderDto;

    @Operation(summary = "Create new order")
    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public OrderStatusData createOrder(@RequestBody OrderForm orderForm) throws ApiException {
        return orderDto.createOrder(orderForm);
    }

    @Operation(summary = "Edit order")
    @RequestMapping(value = "/edit/{orderId}", method = RequestMethod.POST)
    public OrderStatusData editOrder(@RequestBody OrderForm orderForm, @PathVariable String orderId) throws ApiException {
        return orderDto.editOrder(orderForm, orderId);
    }

    @Operation(summary = "Cancel order")
    @RequestMapping(value = "/cancel/{orderId}", method = RequestMethod.PUT)
    public MessageData cancelOrder(@PathVariable String orderId) throws ApiException {
        return orderDto.cancelOrder(orderId);
    }

    @RequestMapping(value = "/generate-invoice/{orderId}", method = RequestMethod.GET)
    public FileData generateInvoice(@PathVariable String orderId) throws ApiException {
        return orderDto.generateInvoice(orderId);
    }

    @RequestMapping(value = "/download-invoice/{orderId}", method = RequestMethod.GET)
    public FileData downloadInvoice(@PathVariable String orderId) throws ApiException {
        return orderDto.downloadInvoice(orderId);
    }

    @RequestMapping(value = "/filter-orders", method = RequestMethod.GET)
    public Page<OrderData> filterOrders(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate, @RequestParam int page,@RequestParam int size) throws ApiException {
        return orderDto.filterOrders(startDate, endDate, page, size);
    }

    @Operation(summary = "Get all orders with pagination")
    @RequestMapping(path = "/get-all-paginated", method = RequestMethod.POST)
    public Page<OrderData> getAllOrders(@Valid @RequestBody PageForm form) throws ApiException {
        return orderDto.getAllOrders(form);
    }
}
