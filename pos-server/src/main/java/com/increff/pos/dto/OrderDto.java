package com.increff.pos.dto;

import com.increff.pos.api.OrderApiImpl;
import com.increff.pos.db.OrderPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.flow.OrderFlow;
import com.increff.pos.helper.OrderHelper;
import com.increff.pos.model.data.*;
import com.increff.pos.model.data.OrderStatusData;
import com.increff.pos.model.form.OrderForm;
import com.increff.pos.model.form.PageForm;
import com.increff.pos.util.ValidationUtil;
import com.increff.service.InvoiceGenerator;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class OrderDto {

    private final OrderApiImpl orderApi;
    private final OrderFlow orderFlow;

    public OrderDto(OrderApiImpl orderApi, OrderFlow orderFlow) {
        this.orderApi = orderApi;
        this.orderFlow = orderFlow;
    }

    public OrderStatusData createOrder(OrderForm orderForm) throws ApiException {
        ValidationUtil.validateOrderForm(orderForm);
        OrderPojo orderPojo = OrderHelper.convertToEntity(orderForm);
        Map<String, OrderStatus> orderStatuses = orderFlow.createOrder(orderPojo);

        generateInvoice(orderPojo);

        return OrderHelper.convertToDto(orderStatuses, orderPojo.getOrderId());
    }

    public Page<OrderData> getAllOrders(PageForm form) throws ApiException {
        ValidationUtil.validatePageForm(form);
        Page<OrderPojo> orderPage = orderApi.getAllOrders(form.getPage(), form.getSize());
        return orderPage.map(OrderHelper::convertToOrderDto);
    }

    public void generateInvoice(OrderPojo orderPojo) {
        OrderData orderData = OrderHelper.convertToOrderDto(orderPojo);
        InvoiceGenerator invoiceGenerator = new InvoiceGenerator();
        invoiceGenerator.generate(orderData);
    }

    public byte[] fetchInvoice(String orderId) throws ApiException {
        ValidationUtil.validateOrderId(orderId);
        return orderApi.getInvoice(orderId);
    }
}
