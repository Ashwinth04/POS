package com.increff.pos.controller;

import com.increff.pos.dto.SalesDto;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.data.DailySalesData;
import com.increff.pos.model.data.ProductRow;
import com.increff.pos.model.form.PageForm;
import org.apache.coyote.Request;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("api/sales")
public class SalesController {

    @Autowired
    private SalesDto salesDto;

    @RequestMapping(value = "/get-client-sales", method = RequestMethod.GET)
    public List<ProductRow> getSalesReport(@RequestParam String clientName, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) throws ApiException {
        return salesDto.getSalesForClient(clientName, startDate, endDate);
    }

    @RequestMapping(value = "/get-daily-sales", method = RequestMethod.GET)
    public DailySalesData getSalesForPeriod(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) throws ApiException {
        return salesDto.getSalesForPeriod(startDate, endDate);
    }

    @RequestMapping(value = "/get-all-paginated", method = RequestMethod.POST)
    public Page<DailySalesData> getDailySalesPaginated(@RequestBody PageForm pageForm) throws ApiException {
        return salesDto.getAllPaginated(pageForm);
    }
}