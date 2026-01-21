package com.increff;

import com.increff.pos.model.data.OrderData;
import com.increff.pos.model.data.OrderItem;
import com.increff.service.InvoiceGenerator;

import java.time.Instant;
import java.util.List;

public class Main {
    public static void main(String[] args) {

        OrderItem i1 = new OrderItem();
        i1.setBarcode("B002");
        i1.setOrderedQuantity(10);
        i1.setSellingPrice(10.0);

        OrderItem i2 = new OrderItem();
        i2.setBarcode("B002");
        i2.setOrderedQuantity(10);
        i2.setSellingPrice(10.0);

        OrderData order = new OrderData();
        order.setId("696e0b40e0507464d26873a0");
        order.setOrderStatus("FULFILLABLE");
        order.setOrderTime(Instant.now());
        order.setOrderItems(List.of(i1, i2));

        new InvoiceGenerator().generate(order);

        System.out.println("Invoice generated: invoice.pdf");
    }
}
