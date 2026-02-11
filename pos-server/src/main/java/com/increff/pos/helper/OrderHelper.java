package com.increff.pos.helper;

import com.increff.pos.db.documents.InventoryPojo;
import com.increff.pos.db.subdocuments.OrderItemPojo;
import com.increff.pos.db.documents.OrderPojo;
import com.increff.pos.db.documents.ProductPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.data.*;
import com.increff.pos.model.form.OrderForm;
import com.increff.pos.model.form.OrderItemForm;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class OrderHelper {

    public static OrderPojo convertToEntity(OrderForm orderForm, Map<String, ProductPojo> barcodeToProductPojo) {
        OrderPojo orderPojo = new OrderPojo();
        List<OrderItemPojo> items = new ArrayList<>();

        for (OrderItemForm item: orderForm.getOrderItems()) {
            OrderItemPojo OrderItemPojo = new OrderItemPojo();
            String barcode = item.getBarcode();
            ProductPojo productPojo = barcodeToProductPojo.get(barcode);

            OrderItemPojo.setOrderedQuantity(item.getOrderedQuantity());
            OrderItemPojo.setSellingPrice(item.getSellingPrice());
            OrderItemPojo.setProductId(productPojo.getId());
            OrderItemPojo.setOrderItemId(UUID.randomUUID().toString());
            items.add(OrderItemPojo);
        }

        orderPojo.setOrderItems(items);
        String id = generate();
        orderPojo.setOrderId(id);
        return orderPojo;
    }

    public static OrderData convertToData(OrderPojo orderPojo, Map<String, ProductPojo> productIdToProductPojo) {
        OrderData orderData = new OrderData();
        orderData.setOrderId(orderPojo.getOrderId());
        orderData.setOrderStatus(orderPojo.getOrderStatus());

        List<OrderItem> orderItems = new ArrayList<>();

        for (OrderItemPojo item: orderPojo.getOrderItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrderedQuantity(item.getOrderedQuantity());
            orderItem.setSellingPrice(item.getSellingPrice());

            String productId = item.getProductId();
            ProductPojo product = productIdToProductPojo.get(productId);
            if (product != null) {
                orderItem.setBarcode(product.getBarcode());
                orderItem.setProductName(product.getName());
            }
            orderItems.add(orderItem);
        }

        orderData.setOrderItems(orderItems);
        orderData.setCreatedAt(orderPojo.getUpdatedAt());
        return orderData;
    }

    public static OrderData convertOrderFormToData(OrderForm orderForm, OrderPojo resultOrderPojo, Map<String, ProductPojo> barcodeToProductPojoMap) {
        OrderData orderData = new OrderData();
        orderData.setOrderId(resultOrderPojo.getOrderId());
        orderData.setOrderStatus(resultOrderPojo.getOrderStatus());
//        orderData.setOrderItems(orderForm.getOrderItems());
        List<OrderItem> orderItems = new ArrayList<>();

        for (OrderItemForm orderItemForm: orderForm.getOrderItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setBarcode(orderItemForm.getBarcode());
            orderItem.setOrderedQuantity(orderItemForm.getOrderedQuantity());
            orderItem.setSellingPrice(orderItem.getSellingPrice());
            String productName = barcodeToProductPojoMap.get(orderItemForm.getBarcode()).getName();
            orderItem.setProductName(productName);
            orderItems.add(orderItem);
        }


        return orderData;
    }

    public static List<InventoryPojo> getInventoryPojosForOrder(List<OrderItemPojo> orderItems) {
        List<InventoryPojo> inventoryPojos = new ArrayList<>();
        for (OrderItemPojo item: orderItems) {
            InventoryPojo pojo = new InventoryPojo();
            pojo.setProductId(item.getProductId());
            pojo.setQuantity(item.getOrderedQuantity());
            inventoryPojos.add(pojo);
        }
        return inventoryPojos;
    }

    public static Map<String, ProductPojo> mapProductIdToProductPojo(Map<String, ProductPojo> barcodeToProductPojo) {
        Map<String, ProductPojo> productIdToProductPojo = new HashMap<>();

        for (ProductPojo product : barcodeToProductPojo.values()) {
            String productId = product.getId();
            productIdToProductPojo.put(productId, product);
        }

        return productIdToProductPojo;
    }


    public static String generate() {
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));

        String random = UUID.randomUUID()
                .toString()
                .substring(0, 4)
                .toUpperCase();

        return "ORD-" + timestamp + "-" + random;
    }

    public static void checkOrderEditable(String status) throws ApiException {
        if (status.equals("CANCELLED")) throw new ApiException("CANCELLED ORDERS CANNOT BE EDITED");

        if (status.equals("PLACED")) throw new ApiException("PLACED ORDERS CANNOT BE EDITED");
    }

    public static void checkOrderCancellable(String status) throws ApiException {
        if (status.equals("CANCELLED")) throw new ApiException("ORDER CANCELLED ALREADY");

        if (status.equals("PLACED")) throw new ApiException("PLACED ORDERS CANNOT BE CANCELLED");
    }

    public static void checkInvoiceDownloadable(OrderPojo orderPojo) throws ApiException {
        if (Objects.isNull(orderPojo)) throw new ApiException("ORDER WITH THE GIVEN ID DOESN'T EXIST");

        String status = orderPojo.getOrderStatus();
        if (!status.equals("PLACED")) throw new ApiException("ORDER NOT PLACED YET. PLEASE PLACE THE ORDER TO DOWNLOAD INVOICE");
    }
}
