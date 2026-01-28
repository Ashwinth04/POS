package com.increff.pos.dto;

import com.increff.pos.api.SalesApiImpl;
import com.increff.pos.dao.SalesDao;
import com.increff.pos.db.SalesPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.helper.SalesHelper;
import com.increff.pos.model.data.DailySalesData;
import com.increff.pos.model.data.ProductRow;
import com.increff.pos.util.ValidationUtil;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Service
public class SalesDto {

    private final SalesApiImpl salesApi;

    public SalesDto(SalesApiImpl salesApi) {
        this.salesApi = salesApi;
    }

    public List<ProductRow> getSalesForClient(String clientName,
                                              LocalDate startDate,
                                              LocalDate endDate) throws ApiException {

        ValidationUtil.validateName(clientName);

        if (startDate == null || endDate == null) {
            throw new ApiException("Start date and end date are required");
        }

        if (endDate.isBefore(startDate)) {
            throw new ApiException("End date cannot be before start date");
        }

        ZoneId zone = ZoneId.systemDefault(); // or ZoneId.of("UTC")

        ZonedDateTime start = startDate.atStartOfDay(zone);
        ZonedDateTime end = endDate.atTime(23, 59, 59, 999_000_000).atZone(zone);

        return salesApi.getSalesForClient(clientName, start, end);
    }

    public DailySalesData getSalesForPeriod(LocalDate startDate,
                                            LocalDate endDate) throws ApiException {

        if (startDate == null || endDate == null) {
            throw new ApiException("Start date and end date are required");
        }

        if (endDate.isBefore(startDate)) {
            throw new ApiException("End date cannot be before start date");
        }

        ZoneId zone = ZoneId.systemDefault(); // or ZoneId.of("UTC")

        ZonedDateTime start = startDate.atStartOfDay(zone);
        ZonedDateTime end = endDate.atTime(23, 59, 59, 999_000_000).atZone(zone);

        SalesPojo pojo = salesApi.getDailySales(start, end);

        return SalesHelper.convertToDto(pojo);
    }

}