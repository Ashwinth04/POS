package com.increff.pos.flow;

import com.increff.pos.api.InventoryApiImpl;
import com.increff.pos.api.OrderApiImpl;
import com.increff.pos.api.ProductApiImpl;
import com.increff.pos.db.InventoryPojo;
import com.increff.pos.db.OrderPojo;
import com.increff.pos.db.ProductPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.data.MessageData;
import com.increff.pos.model.data.OrderData;
import com.increff.pos.model.data.OrderItem;
import com.increff.pos.model.data.OrderStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.*;

@Service
public class OrderFlow {

    @Autowired
    private OrderApiImpl orderApi;

    @Autowired
    private ProductApiImpl productApi;

    @Autowired
    private InventoryApiImpl inventoryApi;

    public OrderPojo createOrder(OrderPojo orderPojo) throws ApiException {

        createOrderItemIds(orderPojo);

        List<InventoryPojo> orderInventoryPojos = getInventoryPojosForOrder(orderPojo.getOrderItems());

        boolean isFulfillable = inventoryApi.reserveInventory(orderInventoryPojos);

        return orderApi.placeOrder(orderPojo, isFulfillable);
    }

    public OrderPojo editOrder(OrderPojo orderPojo, String orderId) throws ApiException {

        createOrderItemIds(orderPojo);

        String existingStatus = orderApi.getOrderStatus(orderId);
        checkOrderEditable(existingStatus);

        List<InventoryPojo> orderInventoryPojos = getInventoryPojosForOrder(orderPojo.getOrderItems());

        boolean isFulfillable = inventoryApi.checkOrderFulfillable(orderInventoryPojos);

        aggregateAndUpdateInventory(orderId, orderInventoryPojos, existingStatus, isFulfillable);

        return orderApi.editOrder(orderPojo, isFulfillable);
    }

    public void checkOrderEditable(String status) throws ApiException {

        if (status.equals("CANCELLED")) throw new ApiException("CANCELLED ORDERS CANNOT BE EDITED");

        if (status.equals("PLACED")) throw new ApiException("PLACED ORDERS CANNOT BE EDITED");
    }

    private void aggregateAndUpdateInventory(String orderId, List<InventoryPojo> incomingInventoryPojos, String existingStatus, boolean isFulfillable) throws ApiException {

        Map<String, Integer> aggregatedItemsIncoming = new HashMap<>();
        Map<String, Integer> aggregatedItemsExisting = new HashMap<>();

        if (existingStatus.equals("FULFILLABLE")) {
            OrderPojo existingOrderPojo = orderApi.getOrderByOrderId(orderId);
            List<InventoryPojo> existingInventoryPojos = getInventoryPojosForOrder(existingOrderPojo.getOrderItems());
            aggregatedItemsExisting = inventoryApi.aggregateItemsByProductId(existingInventoryPojos);
        }

        if (isFulfillable) {
            aggregatedItemsIncoming = inventoryApi.aggregateItemsByProductId(incomingInventoryPojos);
        }

        inventoryApi.editOrder(aggregatedItemsExisting, aggregatedItemsIncoming);
    }

    public MessageData cancelOrder(String orderId) throws ApiException {

        OrderPojo orderPojo = orderApi.getOrderByOrderId(orderId);

        String status = orderApi.getOrderStatus(orderId);
        checkOrderCancellable(status);

        if (status.equals("FULFILLABLE")) {
            List<InventoryPojo> orderInventoryPojos = getInventoryPojosForOrder(orderPojo.getOrderItems());
            inventoryApi.revertInventory(orderInventoryPojos);
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

    public List<InventoryPojo> getInventoryPojosForOrder(List<OrderItem> orderItems) {

        List<String> barcodes = orderItems
                .stream()
                .map(OrderItem::getBarcode)
                .toList();

        Map<String, String> barcodeToProductId = productApi.mapBarcodesToProductIds(barcodes);

        List<InventoryPojo> inventoryPojos = new ArrayList<>();

        for (OrderItem item: orderItems) {
            InventoryPojo pojo = new InventoryPojo();
            String productId = barcodeToProductId.get(item.getBarcode());
            pojo.setProductId(productId);
            pojo.setQuantity(item.getOrderedQuantity());

            inventoryPojos.add(pojo);
        }

        return inventoryPojos;
    }
}
