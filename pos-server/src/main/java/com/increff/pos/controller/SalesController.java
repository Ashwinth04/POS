package com.increff.pos.controller;

import com.increff.pos.dto.SalesDto;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.data.ClientSalesData;
import com.increff.pos.model.data.DailySalesData;
import com.increff.pos.model.data.ProductRow;
import org.apache.coyote.Request;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@RestController
@RequestMapping("api/sales")
public class SalesController {

    private final SalesDto salesDto;

    public SalesController(SalesDto salesDto) {
        this.salesDto = salesDto;
    }

    @RequestMapping(value = "/get-client-sales", method = RequestMethod.GET)
    public List<ProductRow> getSalesReport(@RequestParam String clientName, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) throws ApiException {
        System.out.println("Inside controller");
        return salesDto.getSalesForClient(clientName, startDate, endDate);
    }

    @RequestMapping(value = "/get-daily-sales", method = RequestMethod.GET)
    public DailySalesData getSalesForPeriod(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) throws ApiException {
        return salesDto.getSalesForPeriod(startDate, endDate);
    }

}
