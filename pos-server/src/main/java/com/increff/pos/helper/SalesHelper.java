package com.increff.pos.helper;

import com.increff.pos.db.subdocuments.DailyClientSalesPojo;
import com.increff.pos.db.documents.SalesPojo;
import com.increff.pos.model.data.ClientSalesData;
import com.increff.pos.model.data.DailyClientSalesData;
import com.increff.pos.model.data.DailySalesData;
import com.increff.pos.model.data.ProductRevenueRow;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SalesHelper {
    public static DailySalesData convertToData(SalesPojo salesPojo) {
        DailySalesData data = new DailySalesData();
        data.setDate(salesPojo.getDate());
        data.setTotalOrders(salesPojo.getTotalOrders());
        data.setTotalProducts(salesPojo.getTotalProducts());
        data.setTotalRevenue(salesPojo.getTotalRevenue());

        List<DailyClientSalesData> clientSalesData = new ArrayList<>();

        for (DailyClientSalesPojo pojo: salesPojo.getClients()) {
            DailyClientSalesData clientData = new DailyClientSalesData();
            clientData.setClientName(pojo.getClientName());
            clientData.setTotalProducts(pojo.getTotalProducts());
            clientData.setTotalRevenue(pojo.getTotalRevenue());
            clientSalesData.add(clientData);
        }

        data.setClients(clientSalesData);
        return data;
    }

    public static ClientSalesData convertToClientSalesData(String clientName, LocalDate startDate, LocalDate endDate, List<ProductRevenueRow> productRevenueRows) {
        ClientSalesData clientSalesData = new ClientSalesData();
        clientSalesData.setClientName(clientName);
        clientSalesData.setStartDate(startDate);
        clientSalesData.setEndDate(endDate);
        clientSalesData.setProducts(productRevenueRows);
        return clientSalesData;
    }
}