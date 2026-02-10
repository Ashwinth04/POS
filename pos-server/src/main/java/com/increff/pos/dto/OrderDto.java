package com.increff.pos.dto;

import com.increff.pos.api.OrderApiImpl;
import com.increff.pos.db.subdocuments.OrderItemPojo;
import com.increff.pos.db.documents.OrderPojo;
import com.increff.pos.db.documents.ProductPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.flow.OrderFlow;
import com.increff.pos.helper.OrderHelper;
import com.increff.pos.model.data.*;
import com.increff.pos.model.form.OrderFilterForm;
import com.increff.pos.model.form.OrderForm;
import com.increff.pos.model.form.PageForm;
import com.increff.pos.model.form.SearchOrderForm;
import com.increff.pos.wrapper.InvoiceClientWrapper;
import com.increff.pos.util.FormValidator;
import com.increff.pos.util.NormalizationUtil;
import com.increff.pos.util.ValidationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.increff.pos.util.ValidationUtil.validateOrderItem;

@Service
public class OrderDto {

    @Autowired
    private OrderFlow orderFlow;

    @Autowired
    private OrderApiImpl orderApi;

    @Autowired
    private InvoiceClientWrapper invoiceClientWrapper;

    public OrderData createOrder(OrderForm orderForm) throws ApiException {
        NormalizationUtil.normalizeOrderForm(orderForm);
        FormValidator.validate(orderForm);
        Map<String, ProductPojo> barcodeToProductPojoMap = getBarcodeToProductPojoMap(orderForm);
        ValidationUtil.validateAllOrderItems(orderForm, barcodeToProductPojoMap);
        OrderPojo orderPojo = OrderHelper.convertToEntity(orderForm, barcodeToProductPojoMap);
        OrderPojo resultOrderPojo = orderFlow.createOrder(orderPojo);
        return OrderHelper.convertOrderFormToData(orderForm, resultOrderPojo);
    }

    public OrderData editOrder(String orderId, OrderForm orderForm) throws ApiException {

        NormalizationUtil.normalizeOrderForm(orderForm);
        FormValidator.validate(orderForm);
        ValidationUtil.validateOrderId(orderId);
        orderApi.getCheckByOrderId(orderId);
        Map<String, ProductPojo> barcodeToProductPojo = getBarcodeToProductPojoMap(orderForm);
        ValidationUtil.validateAllOrderItems(orderForm, barcodeToProductPojo);
        OrderPojo orderPojo = OrderHelper.convertToEntity(orderForm, barcodeToProductPojo);
        orderPojo.setOrderId(orderId);
        OrderPojo resultOrderPojo = orderFlow.editOrder(orderPojo, orderId);
        Map<String, ProductPojo> productIdToProductPojo = OrderHelper.mapProductIdToProductPojo(barcodeToProductPojo);

        return OrderHelper.convertToData(resultOrderPojo, productIdToProductPojo);
    }

    public MessageData cancelOrder(String orderId) throws ApiException {
        ValidationUtil.validateOrderId(orderId);
        return orderFlow.cancelOrder(orderId);
    }

    public Page<OrderData> searchById(SearchOrderForm searchOrderForm) throws ApiException {

        FormValidator.validate(searchOrderForm);
        Page<OrderPojo> orderPage = orderApi.searchById(searchOrderForm.getOrderId(), searchOrderForm.getPage(), searchOrderForm.getSize());
        List<String> productIds = orderPage.stream()
                .flatMap(order -> order.getOrderItems().stream())
                .map(OrderItemPojo::getProductId)
                .toList();

        Map<String, ProductPojo> productIdToProductPojo = orderFlow.mapProductIdsToProductPojos(productIds);
        return orderPage.map(orderPojo -> OrderHelper.convertToData(orderPojo, productIdToProductPojo));
    }

    public FileData generateInvoice(String orderId) throws ApiException {

        ValidationUtil.validateOrderId(orderId);
        OrderPojo orderPojo = orderApi.getCheckByOrderId(orderId);

        String status = orderPojo.getOrderStatus();
        if (!status.equals("FULFILLABLE")) throw new ApiException("ORDER IS " + status + ". INVOICE CANNOT BE GENERATED");

        List<String> productIds = orderPojo.getOrderItems()
                .stream()
                .map(OrderItemPojo::getProductId)
                .toList();

        Map<String, ProductPojo> productIdToProductPojo = orderFlow.mapProductIdsToProductPojos(productIds);
        OrderData orderData = OrderHelper.convertToData(orderPojo, productIdToProductPojo);
        FileData response = invoiceClientWrapper.generateInvoice(orderData);
        orderApi.updatePlacedStatus(orderId);
        return response;
    }

    public FileData downloadInvoice(String orderId) throws ApiException {
        OrderPojo orderPojo = orderApi.getCheckByOrderId(orderId);
        OrderHelper.checkInvoiceDownloadable(orderPojo);
        return invoiceClientWrapper.downloadInvoice(orderId);
    }

    public Map<String, ProductPojo> getBarcodeToProductPojoMap(OrderForm orderForm) {
        List<String> barcodes = orderForm.getOrderItems()
                .stream()
                .map(OrderItem::getBarcode)
                .toList();

        return orderFlow.mapBarcodesToProductPojos(barcodes);
    }

    public Page<OrderData> getOrders(OrderFilterForm form) throws ApiException {

        FormValidator.validate(form);
        ValidationUtil.validateDateInputs(form);

        Page<OrderPojo> orderPage = fetchOrders(form);

        List<String> productIds = orderPage.stream()
                .flatMap(order -> order.getOrderItems().stream())
                .map(OrderItemPojo::getProductId)
                .toList();

        Map<String, ProductPojo> productMap =
                orderFlow.mapProductIdsToProductPojos(productIds);

        return orderPage.map(order ->
                OrderHelper.convertToData(order, productMap));
    }

    private Page<OrderPojo> fetchOrders(OrderFilterForm form) {

        if (Objects.isNull(form.getStartDate())) {
            return orderApi.getAllOrders(form.getPage(), form.getSize());
        }

        ZoneId zone = ZoneId.systemDefault();
        ZonedDateTime start = form.getStartDate().atStartOfDay(zone);
        ZonedDateTime end = form.getEndDate()
                .atTime(23, 59, 59, 999_000_000)
                .atZone(zone);

        return orderApi.filterOrdersByDate(start, end, form.getPage(), form.getSize());
    }

}