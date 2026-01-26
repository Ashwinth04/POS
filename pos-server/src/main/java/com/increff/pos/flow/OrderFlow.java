package com.increff.pos.flow;

import com.increff.pos.api.InventoryApiImpl;
import com.increff.pos.api.OrderApiImpl;
import com.increff.pos.api.ProductApiImpl;
import com.increff.pos.db.OrderPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.data.OrderItem;
import com.increff.pos.model.data.OrderStatus;
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

        boolean isFulfillable = inventoryApi.checkOrderFulfillable(orderPojo.getOrderItems(), statuses);

        if (isFulfillable) inventoryApi.updateInventory(orderPojo.getOrderItems());

        return orderApi.placeOrder(orderPojo, statuses, isFulfillable);
    }

    private void createOrderItemIds(OrderPojo orderPojo) {
        for (OrderItem item : orderPojo.getOrderItems()) {
            item.setOrderItemId(UUID.randomUUID().toString());
        }
    }
}
