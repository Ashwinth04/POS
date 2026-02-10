package com.increff.pos.db.documents;

import com.increff.pos.db.subdocuments.DailyClientSalesPojo;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.ZonedDateTime;
import java.util.List;

@Getter
@Setter
@Document(collection = "pos_day_sales")
@CompoundIndex(name = "date_idx", def = "{'date':1}", unique = true)
public class SalesPojo extends AbstractPojo{
    private ZonedDateTime date;
    private int totalOrders;
    private int totalProducts;
    private double totalRevenue;
    private List<DailyClientSalesPojo> clients;
}
