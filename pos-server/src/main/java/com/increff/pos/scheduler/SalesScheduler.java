package com.increff.pos.scheduler;

import com.increff.pos.api.SalesApiImpl;
import com.increff.pos.dao.SalesDao;
import com.increff.pos.dto.SalesDto;
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

//    @Scheduled(fixedDelay = 10000)
    // second minute hour day-of-month month day-of-week
    @Scheduled(cron = "0 5 0 * * *", zone = "Asia/Kolkata")
    public void run() {
        ZoneId zone = ZoneId.of("Asia/Kolkata");
        ZonedDateTime start = LocalDate.now(zone).atStartOfDay(zone);
        ZonedDateTime end = start.plusDays(1);

        salesDto.storeDailySales(start, end);
    }
}
