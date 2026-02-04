package com.increff.pos.test.scheduler;

import com.increff.pos.config.TestConfig;
import com.increff.pos.dto.SalesDto;
import com.increff.pos.scheduler.SalesScheduler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

@SpringBootTest(classes = TestConfig.class)
public class SalesSchedulerTest {

    @Autowired
    private SalesScheduler salesScheduler;

    @MockBean
    private SalesDto salesDto;

    @Test
    void testRunCallsStoreDailySales() {
        // Call the scheduler method directly
        salesScheduler.run();

        // Capture the arguments passed to storeDailySales
        ArgumentCaptor<ZonedDateTime> startCaptor = ArgumentCaptor.forClass(ZonedDateTime.class);
        ArgumentCaptor<ZonedDateTime> endCaptor = ArgumentCaptor.forClass(ZonedDateTime.class);

        verify(salesDto).storeDailySales(startCaptor.capture(), endCaptor.capture());

        ZonedDateTime start = startCaptor.getValue();
        ZonedDateTime end = endCaptor.getValue();

        // Validate that end is exactly 1 day after start
        assertEquals(start.plusDays(1), end);

        // Optionally, you can also check that start is at midnight in Asia/Kolkata
        assertEquals(0, start.getHour());
        assertEquals(0, start.getMinute());
        assertEquals(0, start.getSecond());
    }
}
