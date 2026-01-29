package com.increff.pos.flow;

import com.increff.pos.api.InventoryApiImpl;
import com.increff.pos.api.OrderApiImpl;
import com.increff.pos.api.ProductApiImpl;
import com.increff.pos.db.OrderPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.data.MessageData;
import com.increff.pos.model.data.OrderData;
import com.increff.pos.model.data.OrderItem;
import com.increff.pos.model.data.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class OrderFlow {

    private final OrderApiImpl orderApi;
    private final ProductApiImpl productApi;
    private final InventoryApiImpl inventoryApi;

    public OrderFlow(OrderApiImpl orderApi, ProductApiImpl productApi, InventoryApiImpl inventoryApi) {
        this.orderApi = orderApi;
        this.productApi = productApi;
        this.inventoryApi = inventoryApi;
    }

    public Map<String, OrderStatus> createOrder(OrderPojo orderPojo) throws ApiException {

        createOrderItemIds(orderPojo);

        Map<String, OrderStatus> statuses = new LinkedHashMap<>();

        boolean hasValidationErrors = productApi.validateAllOrderItems(orderPojo, statuses);
        if (hasValidationErrors) return statuses;

        boolean isFulfillable = inventoryApi.reserveInventory(orderPojo.getOrderItems(), statuses);

        return orderApi.placeOrder(orderPojo, statuses, isFulfillable);
    }

    public Map<String, OrderStatus> editOrder(OrderPojo orderPojo, String orderId) throws ApiException {

        createOrderItemIds(orderPojo);

        String existingStatus = orderApi.getOrderStatus(orderId);
        checkOrderEditable(existingStatus);

        Map<String, OrderStatus> statuses = new LinkedHashMap<>();

        boolean hasValidationErrors = productApi.validateAllOrderItems(orderPojo, statuses);
        if (hasValidationErrors) return statuses;

        boolean isFulfillable = inventoryApi.checkOrderFulfillable(orderPojo.getOrderItems(),statuses);

        if (existingStatus.equals("UNFULFILLABLE") && !isFulfillable) {
            return statuses;
        }

        aggregateAndUpdateInventory(orderId, orderPojo, existingStatus, isFulfillable);

        return orderApi.editOrder(orderPojo, statuses, isFulfillable);
    }

    public void checkOrderEditable(String status) throws ApiException {

        if (status.equals("CANCELLED")) throw new ApiException("CANCELLED ORDERS CANNOT BE EDITED");

        if (status.equals("PLACED")) throw new ApiException("PLACED ORDERS CANNOT BE EDITED");
    }

    private void aggregateAndUpdateInventory(String orderId, OrderPojo orderPojo, String existingStatus, boolean isFulfillable) throws ApiException {

        Map<String, Integer> aggregatedItemsIncoming = new HashMap<>();
        Map<String, Integer> aggregatedItemsExisting = new HashMap<>();

        if (existingStatus.equals("FULFILLABLE")) {
            aggregatedItemsExisting = orderApi.aggregateItems(orderId);
        }

        if (isFulfillable) {
            aggregatedItemsIncoming = orderApi.aggregateItems(orderPojo.getOrderItems());
        }

        inventoryApi.editOrder(aggregatedItemsExisting, aggregatedItemsIncoming);
    }

    public MessageData cancelOrder(String orderId) throws ApiException {

        OrderPojo orderPojo = orderApi.getOrderByOrderId(orderId);

        String status = orderApi.getOrderStatus(orderId);
        checkOrderCancellable(status);

        if (status.equals("FULFILLABLE")) {
            inventoryApi.revertInventory(orderPojo.getOrderItems());
        }

        return orderApi.cancelOrder(orderId);
    }

    public void checkOrderCancellable(String status) throws ApiException {

        if (status.equals("CANCELLED")) throw new ApiException("ORDER CANCELLED ALREADY");

        if (status.equals("PLACED")) throw new ApiException("PLACED ORDERS CANNOT BE CANCELLED");
    }

    private void createOrderItemIds(OrderPojo orderPojo) {
        for (OrderItem item : orderPojo.getOrderItems()) {
            item.setOrderItemId(UUID.randomUUID().toString());
        }
    }

    public Page<OrderPojo> getAllOrders(int page, int size) {
        return orderApi.getAllOrders(page, size);
    }

    public OrderPojo getOrder(String orderId) throws ApiException {
        return orderApi.getOrderByOrderId(orderId);
    }

    public void updatePlacedStatus(String orderId) throws ApiException {
        orderApi.updatePlacedStatus(orderId);
    }

    public void checkInvoiceDownloadable(String orderId) throws ApiException {
        OrderPojo orderPojo = getOrder(orderId);

        if (orderPojo == null) throw new ApiException("ORDER WITH THE GIVEN ID DOESN'T EXIST");

        String status = orderPojo.getOrderStatus();
        if (!status.equals("PLACED")) throw new ApiException("ORDER NOT PLACED YET. PLEASE PLACE THE ORDER TO DOWNLOAD INVOICE");
    }

    public Page<OrderPojo> filterOrders(ZonedDateTime startDate, ZonedDateTime endDate, int page, int size) {
        return orderApi.filterOrders(startDate, endDate, page, size);
    }
}
