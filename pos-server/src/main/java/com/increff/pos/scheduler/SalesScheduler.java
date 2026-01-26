package com.increff.pos.scheduler;

import com.increff.pos.api.SalesApiImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SalesScheduler {

    @Autowired
    private SalesApiImpl salesApi;

    @Scheduled(fixedRate = 5 * 60 * 1000)
    public void run() {
        salesApi.recalculateDailySales();
    }
}
