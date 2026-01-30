package com.increff.pos.dto;

import com.increff.pos.api.OrderApiImpl;
import com.increff.pos.client.InvoiceClient;
import com.increff.pos.db.OrderPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.flow.OrderFlow;
import com.increff.pos.helper.OrderHelper;
import com.increff.pos.model.data.*;
import com.increff.pos.model.data.OrderStatusData;
import com.increff.pos.model.form.OrderForm;
import com.increff.pos.model.form.PageForm;
import com.increff.pos.util.ValidationUtil;
import jdk.jshell.spi.ExecutionControlProvider;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;

@Service
public class OrderDto {

    private final OrderFlow orderFlow;
    private final InvoiceClient invoiceClient;

    public OrderDto(OrderFlow orderFlow, InvoiceClient invoiceClient) {
        this.orderFlow = orderFlow;
        this.invoiceClient = invoiceClient;
    }

    public OrderStatusData createOrder(OrderForm orderForm) throws ApiException {
//        ValidationUtil.validateOrderForm(orderForm);
        OrderPojo orderPojo = OrderHelper.convertToEntity(orderForm);
        Map<String, OrderStatus> orderStatuses = orderFlow.createOrder(orderPojo);

        return OrderHelper.convertToDto(orderStatuses, orderPojo.getOrderId());
    }

    public OrderStatusData editOrder(OrderForm orderForm, String orderId) throws ApiException {

//        ValidationUtil.validateOrderForm(orderForm);
        ValidationUtil.validateOrderId(orderId);

        OrderPojo orderPojo = OrderHelper.convertToEntity(orderForm);
        orderPojo.setOrderId(orderId);
        Map<String, OrderStatus> orderStatuses = orderFlow.editOrder(orderPojo, orderId);

        return OrderHelper.convertToDto(orderStatuses, orderPojo.getOrderId());
    }

    public MessageData cancelOrder(String orderId) throws ApiException {
        ValidationUtil.validateOrderId(orderId);
        return orderFlow.cancelOrder(orderId);
    }

    public Page<OrderData> getAllOrders(PageForm form) throws ApiException {
        Page<OrderPojo> orderPage = orderFlow.getAllOrders(form.getPage(), form.getSize());
        return orderPage.map(OrderHelper::convertToOrderDto);
    }

    public FileData generateInvoice(String orderId) throws ApiException {

        ValidationUtil.validateOrderId(orderId);
        OrderPojo orderPojo = orderFlow.getOrder(orderId);

        String status = orderPojo.getOrderStatus();

        if (!status.equals("FULFILLABLE")) throw new ApiException("ORDER CANNOT BE PLACED");

        OrderData orderData = OrderHelper.convertToOrderDto(orderPojo);

        FileData response = invoiceClient.generateInvoice(orderData);

        orderFlow.updatePlacedStatus(orderId);
        return response;
    }

    public FileData downloadInvoice(String orderId) throws ApiException {

        orderFlow.checkInvoiceDownloadable(orderId);

        return invoiceClient.downloadInvoice(orderId);
    }

    public Page<OrderData> filterOrders(LocalDate startDate, LocalDate endDate, int page, int size) throws ApiException {

        if (startDate == null || endDate == null) {
            throw new ApiException("Start date and end date are required");
        }

        if (endDate.isBefore(startDate)) {
            throw new ApiException("End date cannot be before start date");
        }

        ZoneId zone = ZoneId.systemDefault(); // or ZoneId.of("UTC")

        ZonedDateTime start = startDate.atStartOfDay(zone);
        ZonedDateTime end = endDate.atTime(23, 59, 59, 999_000_000).atZone(zone);

        Page<OrderPojo> orderPage =  orderFlow.filterOrders(start, end, page, size);

        return orderPage.map(OrderHelper::convertToOrderDto);
    }
}
