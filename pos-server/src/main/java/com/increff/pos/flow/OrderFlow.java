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

        orderApi.checkOrderEditable(orderId);

        Map<String, OrderStatus> statuses = new LinkedHashMap<>();

        boolean hasValidationErrors = productApi.validateAllOrderItems(orderPojo, statuses);
        if (hasValidationErrors) return statuses;

        Map<String, Integer> aggregatedItemsIncoming = orderApi.aggregateItems(orderPojo.getOrderItems());
        Map<String, Integer> aggregatedItemsExisting = orderApi.aggregateItems(orderId);

        boolean isFulfillable = inventoryApi.editOrder(orderPojo.getOrderItems(), aggregatedItemsExisting, aggregatedItemsIncoming, statuses);

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
}
