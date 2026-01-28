package com.increff.pos.scheduler;

import com.increff.pos.api.SalesApiImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
@Component
public class SalesScheduler {

    @Autowired
    private SalesApiImpl salesApi;

    @Scheduled(fixedDelay = 10000)
    public void run() {
        ZoneId zone = ZoneId.of("Asia/Kolkata");
        ZonedDateTime start = LocalDate.now(zone).atStartOfDay(zone);
        ZonedDateTime end = start.plusDays(1);

        salesApi.storeDailySales(start, end);
    }
}
