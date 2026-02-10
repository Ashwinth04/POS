package com.increff.pos.api;

import com.increff.pos.dao.SalesDao;
import com.increff.pos.db.documents.SalesPojo;
import com.increff.pos.model.data.ProductRevenueRow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class SalesApiImpl {

    @Autowired
    private SalesDao salesDao;

    public List<ProductRevenueRow> getSalesForClient(String clientName, ZonedDateTime startDate, ZonedDateTime endDate) {
        return salesDao.getSalesReport(clientName, startDate, endDate);
    }

    public SalesPojo getDailySales(ZonedDateTime start, ZonedDateTime end) {
            return salesDao.getDailySalesData(start, end);
    }

    @Transactional(rollbackFor = Exception.class)
    public void storeDailySales(ZonedDateTime start, ZonedDateTime end) {
        SalesPojo existing = salesDao.findByDate(start);
        if (Objects.nonNull(existing)) {
            return;
        }

        SalesPojo data = salesDao.getDailySalesData(start, end);
        data.setDate(start);
        salesDao.save(data);
    }

    public Page<SalesPojo> getAllSales(int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "date"));
        return salesDao.findAll(pageRequest);
    }
}