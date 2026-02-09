package com.increff.pos.flow;

import com.increff.pos.api.InventoryApiImpl;
import com.increff.pos.api.OrderApiImpl;
import com.increff.pos.api.ProductApiImpl;
import com.increff.pos.db.InventoryPojo;
import com.increff.pos.db.OrderItemPojo;
import com.increff.pos.db.OrderPojo;
import com.increff.pos.db.ProductPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.helper.OrderHelper;
import com.increff.pos.model.data.MessageData;
import com.increff.pos.model.data.OrderItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional(rollbackFor = Exception.class)
    public OrderPojo createOrder(OrderPojo orderPojo) {

        List<InventoryPojo> inventoryPojos = getInventoryPojosForOrder(orderPojo.getOrderItems());

        boolean isFulfillable = inventoryApi.reserveInventory(inventoryPojos);

        orderPojo.setOrderStatus(isFulfillable ? "FULFILLABLE" : "UNFULFILLABLE");
        return orderApi.createOrder(orderPojo);
    }

    // TODO: non need to send orderId
    @Transactional
    public OrderPojo editOrder(OrderPojo orderPojo, String orderId) throws ApiException {

        OrderPojo existingOrder = orderApi.getCheckByOrderId(orderId);
        String existingStatus = existingOrder.getOrderStatus();
        OrderHelper.checkOrderEditable(existingStatus);

        List<InventoryPojo> orderInventoryPojos = getInventoryPojosForOrder(orderPojo.getOrderItems());

        boolean isFulfillable = inventoryApi.checkOrderFulfillable(orderInventoryPojos);

        aggregateAndUpdateInventory(orderId, orderInventoryPojos, existingStatus, isFulfillable);

        orderPojo.setOrderStatus(isFulfillable ? "FULFILLABLE" : "UNFULFILLABLE");
        return orderApi.editOrder(orderPojo);
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

    public void checkInvoiceDownloadable(String orderId) throws ApiException {
        OrderPojo orderPojo = orderApi.getCheckByOrderId(orderId);

        if (orderPojo == null) throw new ApiException("ORDER WITH THE GIVEN ID DOESN'T EXIST");

        String status = orderPojo.getOrderStatus();
        if (!status.equals("PLACED")) throw new ApiException("ORDER NOT PLACED YET. PLEASE PLACE THE ORDER TO DOWNLOAD INVOICE");
    }

    public List<InventoryPojo> getInventoryPojosForOrder(List<OrderItemPojo> orderItems) {

        List<InventoryPojo> inventoryPojos = new ArrayList<>();

        for (OrderItemPojo item: orderItems) {
            InventoryPojo pojo = new InventoryPojo();
            pojo.setProductId(item.getProductId());
            pojo.setQuantity(item.getOrderedQuantity());

            inventoryPojos.add(pojo);
        }

        return inventoryPojos;
    }

    // TODO: change method name
    public Map<String, ProductPojo> mapBarcodesToProductPojos(List<String> barcodes) {
        List<ProductPojo> products = productApi.mapBarcodesToProductPojos(barcodes);

        Map<String, ProductPojo> barcodeToProductId = new HashMap<>();

        for (ProductPojo product : products) {
            barcodeToProductId.put(
                    product.getBarcode(),
                    product
            );
        }

        return barcodeToProductId;
    }

    public Map<String, ProductPojo> mapProductIdsToProductPojos(List<String> productIds) {
        List<ProductPojo> products = productApi.mapProductIdsToProductPojos(productIds);

        Map<String, ProductPojo> productIdToProductPojo = new HashMap<>();

        for (ProductPojo product : products) {
            productIdToProductPojo.put(
                    product.getId(),
                    product
            );
        }

        return productIdToProductPojo;
    }
}
