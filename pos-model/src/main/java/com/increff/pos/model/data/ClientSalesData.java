package com.increff.pos.model.data;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class ClientSalesData {
    String clientName;
    LocalDate startDate;
    LocalDate endDate;
    private List<ProductRevenueRow> products;
}
