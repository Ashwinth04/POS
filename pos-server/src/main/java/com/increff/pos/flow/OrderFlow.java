package com.increff.pos.flow;

import com.increff.pos.api.InventoryApiImpl;
import com.increff.pos.api.OrderApiImpl;
import com.increff.pos.api.ProductApiImpl;
import com.increff.pos.db.OrderPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.data.MessageData;
import com.increff.pos.model.data.OrderItem;
import com.increff.pos.model.data.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

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

        String existingStatus = orderApi.checkAndGetStatus(orderId);

        Map<String, OrderStatus> statuses = new LinkedHashMap<>();

        boolean hasValidationErrors = productApi.validateAllOrderItems(orderPojo, statuses);
        if (hasValidationErrors) return statuses;

        boolean isFulfillable = inventoryApi.checkOrderFulfillable(orderPojo.getOrderItems(),statuses);

        if (existingStatus.equals("UNFULFILLABLE") && !isFulfillable) {
            return statuses;
        }

        Map<String, Integer> aggregatedItemsIncoming = new HashMap<>();
        Map<String, Integer> aggregatedItemsExisting = new HashMap<>();

        if (existingStatus.equals("FULFILLABLE")) {
            aggregatedItemsExisting = orderApi.aggregateItems(orderId);
        }

        if (isFulfillable) {
            aggregatedItemsIncoming = orderApi.aggregateItems(orderPojo.getOrderItems());
        }

        inventoryApi.editOrder(aggregatedItemsExisting, aggregatedItemsIncoming);

        return orderApi.editOrder(orderPojo, statuses, isFulfillable);
    }

    public MessageData cancelOrder(String orderId) throws ApiException {

        OrderPojo orderPojo = orderApi.getOrderByOrderId(orderId);

        orderApi.checkOrderCancellable(orderId);

        inventoryApi.revertInventory(orderPojo.getOrderItems());

        return orderApi.cancelOrder(orderId);
    }

    private void createOrderItemIds(OrderPojo orderPojo) {
        for (OrderItem item : orderPojo.getOrderItems()) {
            item.setOrderItemId(UUID.randomUUID().toString());
        }
    }

    public Page<OrderPojo> getAllOrders(int page, int size) {
        return orderApi.getAllOrders(page, size);
    }

    public byte[] getInvoice(String orderId) throws ApiException {
        return orderApi.getInvoice(orderId);
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
}
