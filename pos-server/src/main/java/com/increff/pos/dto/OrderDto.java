package com.increff.pos.dto;

import com.increff.pos.api.OrderApiImpl;
import com.increff.pos.api.ProductApiImpl;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;

@Service
public class OrderDto {

    @Autowired
    private OrderFlow orderFlow;

    @Autowired
    private InvoiceClient invoiceClient;

    @Autowired
    private ProductApiImpl productApi;

    public OrderData createOrder(OrderForm orderForm) throws ApiException {
//        ValidationUtil.validateOrderForm(orderForm);

        OrderPojo orderPojo = OrderHelper.convertToEntity(orderForm);
        productApi.validateAllOrderItems(orderPojo);
        OrderPojo resultOrderPojo = orderFlow.createOrder(orderPojo);

        return OrderHelper.convertToDto(resultOrderPojo);
    }

    public OrderData editOrder(OrderForm orderForm, String orderId) throws ApiException {

//        ValidationUtil.validateOrderForm(orderForm);
        ValidationUtil.validateOrderId(orderId);
        OrderPojo orderPojo = OrderHelper.convertToEntity(orderForm);
        orderPojo.setOrderId(orderId);
        productApi.validateAllOrderItems(orderPojo);
        OrderPojo resultOrderPojo = orderFlow.editOrder(orderPojo, orderId);

        return OrderHelper.convertToDto(resultOrderPojo);
    }

    public MessageData cancelOrder(String orderId) throws ApiException {
        ValidationUtil.validateOrderId(orderId);
        return orderFlow.cancelOrder(orderId);
    }

    public Page<OrderData> getAllOrders(PageForm form) {
        Page<OrderPojo> orderPage = orderFlow.getAllOrders(form.getPage(), form.getSize());
        return orderPage.map(OrderHelper::convertToDto);
    }

    public FileData generateInvoice(String orderId) throws ApiException {

        ValidationUtil.validateOrderId(orderId);
        OrderPojo orderPojo = orderFlow.getOrder(orderId);

        String status = orderPojo.getOrderStatus();

        if (!status.equals("FULFILLABLE")) throw new ApiException("ORDER CANNOT BE PLACED");

        OrderData orderData = OrderHelper.convertToDto(orderPojo);

        FileData response = invoiceClient.generateInvoice(orderData);

        orderFlow.updatePlacedStatus(orderId);
        return response;
    }

    public FileData downloadInvoice(String orderId) throws ApiException {

        orderFlow.checkInvoiceDownloadable(orderId);

        return invoiceClient.downloadInvoice(orderId);
    }

    public Page<OrderData> filterOrders(LocalDate startDate, LocalDate endDate, int page, int size) throws ApiException {

        ValidationUtil.validateDates(startDate, endDate);

        ZoneId zone = ZoneId.systemDefault();

        ZonedDateTime start = startDate.atStartOfDay(zone);
        ZonedDateTime end = endDate.atTime(23, 59, 59, 999_000_000).atZone(zone);

        Page<OrderPojo> orderPage =  orderFlow.filterOrders(start, end, page, size);

        return orderPage.map(OrderHelper::convertToDto);
    }
}
