package com.increff.pos.api;

import com.increff.pos.dao.SalesDao;
import com.increff.pos.db.ProductPojo;
import com.increff.pos.db.SalesPojo;
import com.increff.pos.model.data.ProductRow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Service
public class SalesApiImpl {

    @Autowired
    private SalesDao salesDao;

    public List<ProductRow> getSalesForClient(String clientName, ZonedDateTime startDate, ZonedDateTime endDate) {

        return salesDao.getSalesReport(clientName, startDate, endDate);
    }

    public SalesPojo getDailySales(ZonedDateTime start, ZonedDateTime end) {

        return salesDao.getDailySalesData(start, end);
    }

    @Transactional(rollbackFor = Exception.class)
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

    public Page<SalesPojo> getAllSales(int page, int size) {

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "date"));
        return salesDao.findAll(pageRequest);
    }
}

