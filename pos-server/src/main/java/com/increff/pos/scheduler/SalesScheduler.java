package com.increff.pos.scheduler;

import com.increff.pos.api.SalesApiImpl;
import com.increff.pos.model.data.SalesReportRow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.List;

@Component
public class SalesScheduler {

    @Autowired
    private SalesApiImpl salesApi;

    @Scheduled(fixedDelay = 5 * 60 * 1000)
    public void run() {
        salesApi.recalculateDailySales();

        ZonedDateTime start = ZonedDateTime.parse("2026-01-01T00:00:00+00:00");
        ZonedDateTime end   = ZonedDateTime.parse("2026-01-31T23:59:59+00:00");

        List<SalesReportRow> report = salesApi.getSalesForClient("Calvin Klein", start, end);

        for (SalesReportRow row: report) {
            System.out.println(row.getProduct());
            System.out.println(row.getQuantity());
            System.out.println(row.getRevenue() + "\n");
        }
    }
}
