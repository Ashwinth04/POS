package com.increff.pos.db;

import com.increff.pos.model.data.DailyClientSalesData;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

@Getter
@Setter
@Document(collection = "pos_day_sales")
public class SalesPojo extends AbstractPojo{
    @Indexed
    private ZonedDateTime date;
    private int totalOrders;
    private int totalProducts;
    private double totalRevenue;
    private List<DailyClientSalesData> clients;
}
