package com.increff.pos.flow;

import com.increff.pos.api.InventoryApiImpl;
import com.increff.pos.api.OrderApiImpl;
import com.increff.pos.api.ProductApiImpl;
import com.increff.pos.db.documents.InventoryPojo;
import com.increff.pos.db.documents.OrderPojo;
import com.increff.pos.db.documents.ProductPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.helper.OrderHelper;
import com.increff.pos.model.data.MessageData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        List<InventoryPojo> inventoryPojos = OrderHelper.getInventoryPojosForOrder(orderPojo.getOrderItems());
        boolean isFulfillable = inventoryApi.reserveInventory(inventoryPojos);
        orderPojo.setOrderStatus(isFulfillable ? "FULFILLABLE" : "UNFULFILLABLE");
        return orderApi.saveOrder(orderPojo);
    }

    @Transactional
    public OrderPojo editOrder(OrderPojo orderPojo, String orderId) throws ApiException {
        OrderPojo existingOrder = orderApi.getCheckByOrderId(orderId);
        String existingStatus = existingOrder.getOrderStatus();
        OrderHelper.checkOrderEditable(existingStatus);

        List<InventoryPojo> orderInventoryPojos = OrderHelper.getInventoryPojosForOrder(orderPojo.getOrderItems());
        boolean isFulfillable = inventoryApi.checkOrderFulfillable(orderInventoryPojos);
        aggregateAndUpdateInventory(orderId, orderInventoryPojos, existingStatus, isFulfillable);

        orderPojo.setOrderStatus(isFulfillable ? "FULFILLABLE" : "UNFULFILLABLE");
        return orderApi.editOrder(orderPojo);
    }

    @Transactional(rollbackFor = ApiException.class)
    public MessageData cancelOrder(String orderId) throws ApiException {

        OrderPojo orderPojo = orderApi.getCheckByOrderId(orderId);

        String status = orderPojo.getOrderStatus();
        OrderHelper.checkOrderCancellable(status);

        if (status.equals("FULFILLABLE")) {
            List<InventoryPojo> orderInventoryPojos = OrderHelper.getInventoryPojosForOrder(orderPojo.getOrderItems());
            inventoryApi.revertInventory(orderInventoryPojos);
        }

        return orderApi.cancelOrder(orderId);
    }

    @Transactional(readOnly = true)
    public Map<String, ProductPojo> mapBarcodesToProductPojos(List<String> barcodes) {
        List<ProductPojo> products = productApi.getProductPojosForBarcodes(barcodes);
        Map<String, ProductPojo> barcodeToProductId = new HashMap<>();
        for (ProductPojo product : products) {
            barcodeToProductId.put(
                    product.getBarcode(),
                    product
            );
        }

        return barcodeToProductId;
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, ProductPojo> mapProductIdsToProductPojos(List<String> productIds) {
        List<ProductPojo> products = productApi.getProductPojosForProductIds(productIds);
        Map<String, ProductPojo> productIdToProductPojo = new HashMap<>();

        for (ProductPojo product : products) {
            productIdToProductPojo.put(
                    product.getId(),
                    product
            );
        }

        return productIdToProductPojo;
    }

    private void aggregateAndUpdateInventory(String orderId, List<InventoryPojo> incomingInventoryPojos,
                                             String existingStatus, boolean isFulfillable) throws ApiException {

        Map<String, Integer> aggregatedItemsIncoming = new HashMap<>();
        Map<String, Integer> aggregatedItemsExisting = new HashMap<>();

        if (existingStatus.equals("FULFILLABLE")) {
            OrderPojo existingOrderPojo = orderApi.getCheckByOrderId(orderId);
            List<InventoryPojo> existingInventoryPojos = OrderHelper.getInventoryPojosForOrder(existingOrderPojo.getOrderItems());
            aggregatedItemsExisting = inventoryApi.aggregateItemsByProductId(existingInventoryPojos);
        }

        if (isFulfillable) {
            aggregatedItemsIncoming = inventoryApi.aggregateItemsByProductId(incomingInventoryPojos);
        }
        inventoryApi.calculateAndUpdateDeltaInventory(aggregatedItemsExisting, aggregatedItemsIncoming);
    }
}
