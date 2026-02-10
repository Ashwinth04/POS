package com.increff.pos.dto;

import com.increff.pos.api.SalesApiImpl;
import com.increff.pos.db.documents.SalesPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.helper.SalesHelper;
import com.increff.pos.model.data.ClientSalesData;
import com.increff.pos.model.data.DailySalesData;
import com.increff.pos.model.data.ProductRevenueRow;
import com.increff.pos.model.form.PageForm;
import com.increff.pos.util.FormValidator;
import com.increff.pos.util.NormalizationUtil;
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

    public ClientSalesData getSalesForClient(String clientName, LocalDate startDate, LocalDate endDate) throws ApiException {
        clientName = NormalizationUtil.normalizeName(clientName);
        ValidationUtil.validateName(clientName);
        ValidationUtil.validateDates(startDate, endDate);

        ZoneId zone = ZoneId.systemDefault();
        ZonedDateTime start = startDate.atStartOfDay(zone);
        ZonedDateTime end = endDate.atTime(23, 59, 59, 999_000_000).atZone(zone);

        List<ProductRevenueRow> productRevenueRows = salesApi.getSalesForClient(clientName, start, end);
        return SalesHelper.convertToClientSalesData(clientName, startDate, endDate, productRevenueRows);
    }

    public DailySalesData getSalesForPeriod(LocalDate startDate, LocalDate endDate) throws ApiException {
        ValidationUtil.validateDates(startDate, endDate);

        ZoneId zone = ZoneId.systemDefault();
        ZonedDateTime start = startDate.atStartOfDay(zone);
        ZonedDateTime end = endDate.plusDays(1).atStartOfDay(zone);

        SalesPojo pojo = salesApi.getDailySales(start, end);
        return SalesHelper.convertToData(pojo);
    }

    public Page<DailySalesData> getAllPaginated(PageForm pageForm) throws ApiException {
        FormValidator.validate(pageForm);
        Page<SalesPojo> productPage = salesApi.getAllSales(pageForm.getPage(), pageForm.getSize());
        return productPage.map(SalesHelper::convertToData);
    }

    public void storeDailySales(ZonedDateTime startDate, ZonedDateTime endDate) {
        salesApi.storeDailySales(startDate, endDate);
    }
}