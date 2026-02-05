package com.increff.pos.scheduler;

import com.increff.pos.api.SalesApiImpl;
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

    private static final ZoneId ZONE = ZoneId.of("Asia/Kolkata");

    // Runs every 5 minutes
    @Scheduled(cron = "0 */5 * * * *", zone = "Asia/Kolkata")
    public void runEveryFiveMinutes() {
        ZonedDateTime yesterdayStart = LocalDate.now(ZONE).minusDays(1).atStartOfDay(ZONE);
        ZonedDateTime yesterdayEnd = yesterdayStart.plusDays(1);

        salesDto.storeDailySales(yesterdayStart, yesterdayEnd);
    }
}
