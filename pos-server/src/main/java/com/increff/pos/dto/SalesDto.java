package com.increff.pos.dto;

import com.increff.pos.api.SalesApiImpl;
import com.increff.pos.dao.SalesDao;
import com.increff.pos.db.ProductPojo;
import com.increff.pos.db.SalesPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.helper.ProductHelper;
import com.increff.pos.helper.SalesHelper;
import com.increff.pos.model.data.DailySalesData;
import com.increff.pos.model.data.ProductRow;
import com.increff.pos.model.form.PageForm;
import com.increff.pos.model.form.ProductForm;
import com.increff.pos.util.ValidationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Service
public class SalesDto {

    @Autowired
    private SalesApiImpl salesApi;

    public List<ProductRow> getSalesForClient(String clientName, LocalDate startDate, LocalDate endDate) throws ApiException {

        ValidationUtil.validateName(clientName);
        ValidationUtil.validateDates(startDate, endDate);

        ZoneId zone = ZoneId.systemDefault();

        ZonedDateTime start = startDate.atStartOfDay(zone);
        ZonedDateTime end = endDate.atTime(23, 59, 59, 999_000_000).atZone(zone);

        return salesApi.getSalesForClient(clientName, start, end);
    }

    public DailySalesData getSalesForPeriod(LocalDate startDate, LocalDate endDate) throws ApiException {

        ValidationUtil.validateDates(startDate, endDate);

        ZoneId zone = ZoneId.systemDefault();

        ZonedDateTime start = startDate.atStartOfDay(zone);
        ZonedDateTime end = endDate.atTime(23, 59, 59, 999_000_000).atZone(zone);

        SalesPojo pojo = salesApi.getDailySales(start, end);

        return SalesHelper.convertToDto(pojo);
    }

    public Page<DailySalesData> getAllPaginated(PageForm pageForm) {

        Page<SalesPojo> productPage = salesApi.getAllSales(pageForm.getPage(), pageForm.getSize());
        return productPage.map(SalesHelper::convertToDto);
    }

}