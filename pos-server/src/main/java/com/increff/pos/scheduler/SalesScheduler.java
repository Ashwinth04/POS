package com.increff.pos.scheduler;

import com.increff.pos.api.SalesApiImpl;
import com.increff.pos.dao.SalesDao;
import com.increff.pos.dto.SalesDto;
import com.increff.pos.model.form.ClientForm;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
@Component
public class SalesScheduler {

    @Autowired
    private SalesDto salesDto;

    @Autowired
    private SalesDao salesDao;

    private static final ZoneId ZONE = ZoneId.of("Asia/Kolkata");

    // ✅ Runs once when application starts
    @PostConstruct
    public void onStartup() {
        System.out.println("ON startup");
        backfillMissingDays();
    }

    // ✅ Runs every day at 00:05 IST
    @Scheduled(cron = "0 5 0 * * *", zone = "Asia/Kolkata")
    public void runDaily() {
        runForYesterday();
    }

    private void backfillMissingDays() {

        ZonedDateTime latest = salesDao.findLatestDate();

        // If no data exists, start from yesterday
        ZonedDateTime start = (latest == null)
                ? LocalDate.now(ZONE).minusDays(1).atStartOfDay(ZONE)
                : latest.plusDays(1);

        ZonedDateTime yesterday =
                LocalDate.now(ZONE).minusDays(1).atStartOfDay(ZONE);

        while (!start.isAfter(yesterday)) {
            System.out.println("CALLING");
            salesDto.storeDailySales(start, start.plusDays(1));
            start = start.plusDays(1);
        }
    }

    private void runForYesterday() {
        ZonedDateTime start =
                LocalDate.now(ZONE).minusDays(1).atStartOfDay(ZONE);

        salesDto.storeDailySales(start, start.plusDays(1));
    }
}
