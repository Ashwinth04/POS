package com.increff.pos.dto;

import com.increff.pos.api.OrderApiImpl;
import com.increff.pos.db.OrderItemPojo;
import com.increff.pos.db.OrderPojo;
import com.increff.pos.db.ProductPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.flow.OrderFlow;
import com.increff.pos.helper.OrderHelper;
import com.increff.pos.model.data.*;
import com.increff.pos.model.form.OrderForm;
import com.increff.pos.model.form.OrderItemForm;
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

        FormValidator.validate(orderForm);
        NormalizationUtil.normalizeOrderForm(orderForm);
        Map<String, ProductPojo> barcodeToProductPojo = validateAllOrderItems(orderForm);

        OrderPojo orderPojo = OrderHelper.convertToEntity(orderForm, barcodeToProductPojo);

        OrderPojo resultOrderPojo = orderFlow.createOrder(orderPojo);

        // TODO: DONT USE THIS, USE ORDER FORM TO COVNERT TO DATA (Is it good to write a separate fn to convert form to data?) - its fine
        Map<String, ProductPojo> productIdToProductPojo = OrderHelper.mapProductIdToProductPojo(barcodeToProductPojo);

        return OrderHelper.convertToData(resultOrderPojo, productIdToProductPojo);
    }

    public OrderData editOrder(OrderForm orderForm) throws ApiException {

        // TODO: First check should if order is present or not (Done)
        FormValidator.validate(orderForm);
        String orderId = orderForm.getOrderId();
        ValidationUtil.validateOrderId(orderId);
        NormalizationUtil.normalizeOrderForm(orderForm);
        orderApi.getCheckByOrderId(orderId);
        Map<String, ProductPojo> barcodeToProductPojo = validateAllOrderItems(orderForm);
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

    public Page<OrderData> getAllOrders(PageForm form) throws ApiException {
        FormValidator.validate(form);
        Page<OrderPojo> orderPage = orderApi.getAllOrders(form.getPage(), form.getSize());

        List<String> productIds = orderPage.stream()
                .flatMap(order -> order.getOrderItems().stream())
                .map(OrderItemPojo::getProductId)
                .toList();

        Map<String, ProductPojo> productIdToProductPojo = orderFlow.mapProductIdsToProductPojos(productIds);
        return orderPage.map(orderPojo -> OrderHelper.convertToData(orderPojo, productIdToProductPojo));
    }

    public Page<OrderData> searchById(SearchOrderForm searchOrderForm) throws ApiException {

        FormValidator.validate(searchOrderForm);
        Page<OrderPojo> orderPage = orderApi.search(searchOrderForm.getOrderId(), searchOrderForm.getPage(), searchOrderForm.getSize());
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

        if (!status.equals("FULFILLABLE")) throw new ApiException("ORDER CANNOT BE PLACED");

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

        orderFlow.checkInvoiceDownloadable(orderId);

        return invoiceClientWrapper.downloadInvoice(orderId);
    }

    public Page<OrderData> filterOrders(LocalDate startDate, LocalDate endDate, int page, int size) throws ApiException {

        ValidationUtil.validateDates(startDate, endDate);

        ZoneId zone = ZoneId.systemDefault();

        ZonedDateTime start = startDate.atStartOfDay(zone);
        ZonedDateTime end = endDate.atTime(23, 59, 59, 999_000_000).atZone(zone);

        Page<OrderPojo> orderPage =  orderApi.filterOrders(start, end, page, size);

        List<String> productIds = orderPage.stream()
                .flatMap(order -> order.getOrderItems().stream())
                .map(OrderItemPojo::getProductId)
                .toList();

        Map<String, ProductPojo> productIdToProductPojo = orderFlow.mapProductIdsToProductPojos(productIds);
        return orderPage.map(orderPojo -> OrderHelper.convertToData(orderPojo, productIdToProductPojo));

    }

    public Map<String, ProductPojo> validateAllOrderItems(OrderForm orderForm) throws ApiException {
        List<String> barcodes = orderForm.getOrderItems().stream().map(OrderItemForm::getBarcode).toList();

        // TODO: change api method to getProductPojos and convert to map here
        Map<String, ProductPojo> barcodeToProductPojos = orderFlow.mapBarcodesToProductPojos(barcodes);

        for (OrderItemForm item : orderForm.getOrderItems()) {
            try {
                ProductPojo product = barcodeToProductPojos.get(item.getBarcode());
                validateOrderItem(item, product);
            } catch (ApiException e) {
                throw new ApiException(e.getMessage());
            }
        }

        return barcodeToProductPojos;
    }
}
