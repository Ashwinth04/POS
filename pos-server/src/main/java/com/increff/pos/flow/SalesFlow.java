package com.increff.pos.flow;

import com.increff.pos.api.OrderApiImpl;
import com.increff.pos.api.ProductApiImpl;
import com.increff.pos.api.SalesApiImpl;
import com.increff.pos.db.OrderPojo;
import com.increff.pos.model.data.OrderItem;
import com.increff.pos.model.data.ProductSalesData;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SalesFlow {

    private final OrderApiImpl orderApi;
    private final ProductApiImpl productApi;
    private final SalesApiImpl salesApi;

    public SalesFlow(OrderApiImpl orderApi, ProductApiImpl productApi, SalesApiImpl salesApi) {
        this.orderApi = orderApi;
        this.productApi = productApi;
        this.salesApi = salesApi;
    }

    public void createDailySalesReport() {

        List<OrderPojo> todaysOrders = orderApi.getTodaysOrders();

        Map<String, ProductSalesData> productLevelData = new HashMap<>();

        for (OrderPojo order: todaysOrders) {
            List<OrderItem> products = order.getOrderItems();

            for (OrderItem product: products) {

                String barcode = product.getBarcode();
                int quantity = product.getOrderedQuantity();
                double sellingPrice = product.getSellingPrice();

                double revenue = quantity * sellingPrice;

                productLevelData.compute(barcode, (key, existing) -> {
                    if (existing == null) {
                        return new ProductSalesData(barcode, quantity, revenue);
                    } else {
                        existing.setQuantity(existing.getQuantity() + quantity);
                        existing.setRevenue(existing.getRevenue() + revenue);
                        return existing;
                    }
                });
            }
        }

        Map<String, List<ProductSalesData>> clientLevelData = new HashMap<>();


    }
}
