package com.increff.pos.dto;

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
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class OrderDto {

    @Autowired
    private OrderFlow orderFlow;

    @Autowired
    private InvoiceClientWrapper invoiceClientWrapper;

    @Autowired
    private FormValidator formValidator;

    public OrderData createOrder(OrderForm orderForm) throws ApiException {

        formValidator.validate(orderForm);
        NormalizationUtil.normalizeOrderForm(orderForm);
        Map<String, ProductPojo> barcodeToProductPojo = validateAllOrderItems(orderForm);
        OrderPojo orderPojo = OrderHelper.convertToEntity(orderForm, barcodeToProductPojo);
        OrderPojo resultOrderPojo = orderFlow.createOrder(orderPojo);

        System.out.println("size: " + resultOrderPojo.getOrderItems().size());

        for (OrderItemRecord item: resultOrderPojo.getOrderItems()) {
            System.out.println("item: " + item.getProductId());
        }

        Map<String, ProductPojo> productIdToProductPojo = OrderHelper.mapProductIdToProductPojo(barcodeToProductPojo);

        for (String productId: productIdToProductPojo.keySet()) {
            System.out.println("PID: " + productId + " " + productIdToProductPojo.get(productId).getBarcode());
        }

        return OrderHelper.convertToData(resultOrderPojo, productIdToProductPojo);
    }

    public OrderData editOrder(OrderForm orderForm, String orderId) throws ApiException {

        formValidator.validate(orderForm);
        ValidationUtil.validateOrderId(orderId);
        NormalizationUtil.normalizeOrderForm(orderForm);
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

        formValidator.validate(form);
        Page<OrderPojo> orderPage = orderFlow.getAllOrders(form.getPage(), form.getSize());

        List<String> productIds = orderPage.stream()
                .flatMap(order -> order.getOrderItems().stream())
                .map(OrderItemRecord::getProductId)
                .toList();

        Map<String, ProductPojo> productIdToProductPojo = orderFlow.mapProductIdsToProductPojos(productIds);
        return orderPage.map(orderPojo -> OrderHelper.convertToData(orderPojo, productIdToProductPojo));
    }

    public FileData generateInvoice(String orderId) throws ApiException {

        ValidationUtil.validateOrderId(orderId);
        OrderPojo orderPojo = orderFlow.getOrder(orderId);

        String status = orderPojo.getOrderStatus();

        if (!status.equals("FULFILLABLE")) throw new ApiException("ORDER CANNOT BE PLACED");

        List<String> productIds = orderPojo.getOrderItems()
                .stream()
                .map(OrderItemRecord::getProductId)
                .toList();

        Map<String, ProductPojo> productIdToProductPojo = orderFlow.mapProductIdsToProductPojos(productIds);
        OrderData orderData = OrderHelper.convertToData(orderPojo, productIdToProductPojo);

        FileData response = invoiceClientWrapper.generateInvoice(orderData);

        orderFlow.updatePlacedStatus(orderId);
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

        Page<OrderPojo> orderPage =  orderFlow.filterOrders(start, end, page, size);

        List<String> productIds = orderPage.stream()
                .flatMap(order -> order.getOrderItems().stream())
                .map(OrderItemRecord::getProductId)
                .toList();

        Map<String, ProductPojo> productIdToProductPojo = orderFlow.mapProductIdsToProductPojos(productIds);
        return orderPage.map(orderPojo -> OrderHelper.convertToData(orderPojo, productIdToProductPojo));

    }

    public Map<String, ProductPojo> validateAllOrderItems(OrderForm orderForm) throws ApiException {

        List<OrderItemForm> orderItems = orderForm.getOrderItems();

        List<String> barcodes = orderItems.stream().map(OrderItemForm::getBarcode).toList();

        Map<String, ProductPojo> barcodeToProductPojos = orderFlow.mapBarcodesToProductPojos(barcodes);

        for (OrderItemForm item : orderForm.getOrderItems()) {
            try {
                validateItem(item, barcodeToProductPojos);
            } catch (ApiException e) {
                throw new ApiException(e.getMessage());
            }
        }

        return barcodeToProductPojos;

    }

    // TODO: move it to validation util
    public void validateItem(OrderItemForm item, Map<String, ProductPojo> productMap) throws ApiException {

        String barcode = item.getBarcode();
        ProductPojo product = productMap.get(barcode);

        if (product == null) {
            throw new ApiException("Invalid barcode: " + barcode);
        }

        if (item.getSellingPrice() > product.getMrp() || item.getSellingPrice() <= 0) {
            throw new ApiException("Selling price exceeds MRP for barcode: " + barcode);
        }
    }

    public Page<OrderData> searchById(SearchOrderForm searchOrderForm) throws ApiException {

        formValidator.validate(searchOrderForm);
        Page<OrderPojo> orderPage = orderFlow.searchById(searchOrderForm.getOrderId(), searchOrderForm.getPage(), searchOrderForm.getSize());
        List<String> productIds = orderPage.stream()
                .flatMap(order -> order.getOrderItems().stream())
                .map(OrderItemRecord::getProductId)
                .toList();

        Map<String, ProductPojo> productIdToProductPojo = orderFlow.mapProductIdsToProductPojos(productIds);
        return orderPage.map(orderPojo -> OrderHelper.convertToData(orderPojo, productIdToProductPojo));

    }
}
