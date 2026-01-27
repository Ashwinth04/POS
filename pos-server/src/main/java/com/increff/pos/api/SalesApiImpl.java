package com.increff.pos.api;

import com.increff.pos.dao.OrderDao;
import com.increff.pos.dao.SalesDao;
import com.increff.pos.db.OrderPojo;
import com.increff.pos.db.SalesPojo;
import com.increff.pos.model.data.OrderItem;
import com.increff.pos.model.data.SalesReportRow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SalesApiImpl {

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private SalesDao salesDao;

    @Transactional
    public void recalculateDailySales() {

        System.out.println("Scheduler running!!");

        List<OrderPojo> orders = orderDao.findTodayFulfillableOrders();

        System.out.println("Today's orders: " + orders);

        // Group by date
        Map<LocalDate, List<OrderPojo>> ordersByDate = orders.stream()
                .collect(Collectors.groupingBy(order ->
                        LocalDate.ofInstant(order.getOrderTime(), ZoneId.systemDefault())
                ));

        for (Map.Entry<LocalDate, List<OrderPojo>> entry : ordersByDate.entrySet()) {
            LocalDate date = entry.getKey();
            List<OrderPojo> dailyOrders = entry.getValue();

            int orderCount = dailyOrders.size();

            int itemsCount = dailyOrders.stream()
                    .flatMap(o -> o.getOrderItems().stream())
                    .mapToInt(OrderItem::getOrderedQuantity)
                    .sum();

            double revenue = dailyOrders.stream()
                    .flatMap(o -> o.getOrderItems().stream())
                    .mapToDouble(i -> i.getOrderedQuantity() * i.getSellingPrice())
                    .sum();

            // Upsert into pos_day_sales
            SalesPojo sales = salesDao.findByDate(date);
            if (sales == null) {
                sales = new SalesPojo();
            }

            sales.setDate(date);
            sales.setInvoicedOrdersCount(orderCount);
            sales.setInvoicedItemsCount(itemsCount);
            sales.setTotalRevenue(revenue);

            salesDao.save(sales);
        }
    }

    public List<SalesReportRow> getSalesForClient(String clientName, ZonedDateTime startDate, ZonedDateTime endDate) {
        return salesDao.getSalesReport(clientName, startDate, endDate);
    }
}

