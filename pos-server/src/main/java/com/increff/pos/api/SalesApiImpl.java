package com.increff.pos.api;

import com.increff.pos.dao.SalesDao;
import com.increff.pos.db.SalesPojo;
import com.increff.pos.model.data.ProductRow;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Service
public class SalesApiImpl {

    private final SalesDao salesDao;

    public SalesApiImpl(SalesDao salesDao) {
        this.salesDao = salesDao;
    }

    public List<ProductRow> getSalesForClient(String clientName, ZonedDateTime startDate, ZonedDateTime endDate) {
        System.out.println("Inside API");
        return salesDao.getSalesReport(clientName, startDate, endDate);
    }

    public SalesPojo getDailySales(ZonedDateTime start, ZonedDateTime end) {
        return salesDao.getDailySalesData(start, end);
    }

    public void storeDailySales(ZonedDateTime start, ZonedDateTime end) {

        SalesPojo data = salesDao.getDailySalesData(start,end);
        ZoneId zone = ZoneId.of("Asia/Kolkata");
        data.setDate(LocalDate.now(zone).atStartOfDay(zone));

        SalesPojo existing = salesDao.findByDate(start);

        if (existing != null) {
            data.setId(existing.getId());
        }
        salesDao.save(data);
    }
}

