package com.increff.pos.db.documents;

import com.increff.pos.db.subdocuments.DailyClientSalesPojo;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

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
    private List<DailyClientSalesPojo> clients;
}
