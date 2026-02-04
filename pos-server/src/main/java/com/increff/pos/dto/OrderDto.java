package com.increff.pos.dto;

import com.increff.pos.api.ProductApiImpl;
import com.increff.pos.client.InvoiceClient;
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
import com.increff.pos.service.InvoiceService;
import com.increff.pos.util.FormValidator;
import com.increff.pos.util.NormalizationUtil;
import com.increff.pos.util.ValidationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class OrderDto {

    @Autowired
    private OrderFlow orderFlow;

    @Autowired
    private InvoiceService invoiceService;

    @Autowired
    private FormValidator formValidator;

    public OrderData createOrder(OrderForm orderForm) throws ApiException {

        formValidator.validate(orderForm);
        NormalizationUtil.normalizeOrderForm(orderForm);
        validateAllOrderItems(orderForm);
        OrderPojo orderPojo = OrderHelper.convertToEntity(orderForm);
        OrderPojo resultOrderPojo = orderFlow.createOrder(orderPojo);

        return OrderHelper.convertToData(resultOrderPojo);
    }

    public OrderData editOrder(OrderForm orderForm, String orderId) throws ApiException {

        formValidator.validate(orderForm);
        ValidationUtil.validateOrderId(orderId);
        NormalizationUtil.normalizeOrderForm(orderForm);
        validateAllOrderItems(orderForm);
        OrderPojo orderPojo = OrderHelper.convertToEntity(orderForm);
        orderPojo.setOrderId(orderId);
        OrderPojo resultOrderPojo = orderFlow.editOrder(orderPojo, orderId);

        return OrderHelper.convertToData(resultOrderPojo);
    }

    public MessageData cancelOrder(String orderId) throws ApiException {
        ValidationUtil.validateOrderId(orderId);
        return orderFlow.cancelOrder(orderId);
    }

    public Page<OrderData> getAllOrders(PageForm form) throws ApiException {

        formValidator.validate(form);
        Page<OrderPojo> orderPage = orderFlow.getAllOrders(form.getPage(), form.getSize());
        return orderPage.map(OrderHelper::convertToData);
    }

    public FileData generateInvoice(String orderId) throws ApiException {

        ValidationUtil.validateOrderId(orderId);
        OrderPojo orderPojo = orderFlow.getOrder(orderId);

        String status = orderPojo.getOrderStatus();

        if (!status.equals("FULFILLABLE")) throw new ApiException("ORDER CANNOT BE PLACED");

        OrderData orderData = OrderHelper.convertToData(orderPojo);

        FileData response = invoiceService.generateInvoice(orderData);

        orderFlow.updatePlacedStatus(orderId);
        return response;
    }

    public FileData downloadInvoice(String orderId) throws ApiException {

        orderFlow.checkInvoiceDownloadable(orderId);

        return invoiceService.downloadInvoice(orderId);
    }

    public Page<OrderData> filterOrders(LocalDate startDate, LocalDate endDate, int page, int size) throws ApiException {

        ValidationUtil.validateDates(startDate, endDate);

        ZoneId zone = ZoneId.systemDefault();

        ZonedDateTime start = startDate.atStartOfDay(zone);
        ZonedDateTime end = endDate.atTime(23, 59, 59, 999_000_000).atZone(zone);

        Page<OrderPojo> orderPage =  orderFlow.filterOrders(start, end, page, size);

        return orderPage.map(OrderHelper::convertToData);
    }

    public void validateAllOrderItems(OrderForm orderForm) throws ApiException {

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

    }

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
        return orderPage.map(OrderHelper::convertToData);
    }
}
