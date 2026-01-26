package com.increff.pos.db;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Getter
@Setter
@Document(collection = "pos_day_sales")
public class SalesPojo extends AbstractPojo{
    @Indexed(unique = true)
    LocalDate date;
    int invoicedOrdersCount;
    int invoicedItemsCount;
    double totalRevenue;
}
