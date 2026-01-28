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

import java.util.Map;

@Service
public class OrderDto {

    private final OrderApiImpl orderApi;
    private final OrderFlow orderFlow;
    private final InvoiceClient invoiceClient;

    public OrderDto(OrderApiImpl orderApi, OrderFlow orderFlow, InvoiceClient invoiceClient) {
        this.orderApi = orderApi;
        this.orderFlow = orderFlow;
        this.invoiceClient = invoiceClient;
    }

    public OrderStatusData createOrder(OrderForm orderForm) throws ApiException {
        ValidationUtil.validateOrderForm(orderForm);
        OrderPojo orderPojo = OrderHelper.convertToEntity(orderForm);
        Map<String, OrderStatus> orderStatuses = orderFlow.createOrder(orderPojo);

        return OrderHelper.convertToDto(orderStatuses, orderPojo.getOrderId());
    }

    public OrderStatusData editOrder(OrderForm orderForm, String orderId) throws ApiException {

        ValidationUtil.validateOrderForm(orderForm);
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
        ValidationUtil.validatePageForm(form);
        Page<OrderPojo> orderPage = orderFlow.getAllOrders(form.getPage(), form.getSize());
        return orderPage.map(OrderHelper::convertToOrderDto);
    }

//    public void generateInvoice(OrderPojo orderPojo) {
//        OrderData orderData = OrderHelper.convertToOrderDto(orderPojo);
//        InvoiceGenerator invoiceGenerator = new InvoiceGenerator();
//        invoiceGenerator.generate(orderData);
//    }

    public FileData generateInvoice(String orderId) throws ApiException {

        ValidationUtil.validateOrderId(orderId);
        OrderPojo orderPojo = orderFlow.getOrder(orderId);

        String status = orderPojo.getOrderStatus();

        if (!status.equals("FULFILLABLE")) throw new ApiException("ORDER CANNOT BE PLACED");

        if (orderPojo == null) throw new ApiException("ORDER WITH THE GIVEN ID DOESNT EXIST");

        OrderData orderData = OrderHelper.convertToOrderDto(orderPojo);

        FileData response = invoiceClient.generateInvoice(orderData);

        orderFlow.updatePlacedStatus(orderId);
        return response;
    }

    public FileData downloadInvoice(String orderId) throws ApiException {

        orderFlow.checkInvoiceDownloadable(orderId);

        return invoiceClient.downloadInvoice(orderId);
    }

    public byte[] fetchInvoice(String orderId) throws ApiException {
        ValidationUtil.validateOrderId(orderId);
        return orderFlow.getInvoice(orderId);
    }
}
