package com.increff.pos.flow;

import com.increff.pos.api.InventoryApiImpl;
import com.increff.pos.api.OrderApiImpl;
import com.increff.pos.api.ProductApiImpl;
import com.increff.pos.db.InventoryPojo;
import com.increff.pos.db.OrderPojo;
import com.increff.pos.db.ProductPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.helper.OrderHelper;
import com.increff.pos.model.data.MessageData;
import com.increff.pos.model.data.OrderItem;
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

        return orderApi.createOrder(orderPojo, isFulfillable);
    }

    public OrderPojo editOrder(OrderPojo orderPojo, String orderId) throws ApiException {

        createOrderItemIds(orderPojo);

        String existingStatus = orderApi.getOrderStatus(orderId);
        OrderHelper.checkOrderEditable(existingStatus);

        List<InventoryPojo> orderInventoryPojos = getInventoryPojosForOrder(orderPojo.getOrderItems());

        boolean isFulfillable = inventoryApi.checkOrderFulfillable(orderInventoryPojos);

        aggregateAndUpdateInventory(orderId, orderInventoryPojos, existingStatus, isFulfillable);

        return orderApi.editOrder(orderPojo, isFulfillable);
    }

    private void aggregateAndUpdateInventory(String orderId, List<InventoryPojo> incomingInventoryPojos, String existingStatus, boolean isFulfillable) throws ApiException {

        Map<String, Integer> aggregatedItemsIncoming = new HashMap<>();
        Map<String, Integer> aggregatedItemsExisting = new HashMap<>();

        if (existingStatus.equals("FULFILLABLE")) {
            OrderPojo existingOrderPojo = orderApi.getCheckByOrderId(orderId);
            List<InventoryPojo> existingInventoryPojos = getInventoryPojosForOrder(existingOrderPojo.getOrderItems());
            aggregatedItemsExisting = inventoryApi.aggregateItemsByProductId(existingInventoryPojos);
        }

        if (isFulfillable) {
            aggregatedItemsIncoming = inventoryApi.aggregateItemsByProductId(incomingInventoryPojos);
        }

        inventoryApi.calculateAndUpdateDeltaInventory(aggregatedItemsExisting, aggregatedItemsIncoming);
    }

    public MessageData cancelOrder(String orderId) throws ApiException {

        OrderPojo orderPojo = orderApi.getCheckByOrderId(orderId);

        String status = orderPojo.getOrderStatus();
        OrderHelper.checkOrderCancellable(status);

        if (status.equals("FULFILLABLE")) {
            List<InventoryPojo> orderInventoryPojos = getInventoryPojosForOrder(orderPojo.getOrderItems());
            inventoryApi.revertInventory(orderInventoryPojos);
        }

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

    public OrderPojo getOrder(String orderId) throws ApiException {
        return orderApi.getCheckByOrderId(orderId);
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

        Map<String, ProductPojo> barcodeToProductId = productApi.mapBarcodesToProductPojos(barcodes);

        List<InventoryPojo> inventoryPojos = new ArrayList<>();

        for (OrderItem item: orderItems) {
            InventoryPojo pojo = new InventoryPojo();
            ProductPojo productPojo = barcodeToProductId.get(item.getBarcode());
            pojo.setProductId(productPojo.getId());
            pojo.setQuantity(item.getOrderedQuantity());

            inventoryPojos.add(pojo);
        }

        return inventoryPojos;
    }

    public Map<String, ProductPojo> mapBarcodesToProductPojos(List<String> barcodes) {
        return productApi.mapBarcodesToProductPojos(barcodes);
    }

    public OrderPojo searchById(String orderId) throws ApiException {
        return orderApi.getCheckByOrderId(orderId);
    }
}
