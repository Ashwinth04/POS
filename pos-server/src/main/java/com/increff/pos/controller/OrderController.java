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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Order Management", description = "Create, view and filter orders")
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderDto orderDto;

    public OrderController(OrderDto orderDto) {
        this.orderDto = orderDto;
    }

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

    @Operation(summary = "Get all orders with pagination")
    @RequestMapping(path = "/get-all-paginated", method = RequestMethod.POST)
    public Page<OrderData> getAllProducts(@RequestBody PageForm form) throws ApiException {
        return orderDto.getAllOrders(form);
    }

    @Operation(summary = "Download invoice PDF for an order")
    @RequestMapping(value = "/{orderId}/invoice", method = RequestMethod.GET)
    public ResponseEntity<byte[]> downloadInvoice(@PathVariable String orderId) throws ApiException {

        byte[] pdfBytes = orderDto.fetchInvoice(orderId); // you'll add this in DTO

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=invoice-" + orderId + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
}
