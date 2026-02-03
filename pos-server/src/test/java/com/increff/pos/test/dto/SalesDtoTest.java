package com.increff.pos.test.dto;

import com.increff.pos.api.SalesApiImpl;
import com.increff.pos.db.SalesPojo;
import com.increff.pos.dto.SalesDto;
import com.increff.pos.exception.ApiException;
import com.increff.pos.helper.SalesHelper;
import com.increff.pos.model.data.DailySalesData;
import com.increff.pos.model.data.ProductRow;
import com.increff.pos.model.form.PageForm;
import com.increff.pos.util.ValidationUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SalesDtoTest {

    @Mock
    private SalesApiImpl salesApi;

    @InjectMocks
    private SalesDto salesDto;

    // ---------- GET SALES FOR CLIENT ----------

    @Test
    void testGetSalesForClientSuccess() throws ApiException {

        String clientName = "client1";
        LocalDate startDate = LocalDate.now().minusDays(5);
        LocalDate endDate = LocalDate.now();

        List<ProductRow> rows = List.of(new ProductRow());

        try (MockedStatic<ValidationUtil> validationMock = mockStatic(ValidationUtil.class)) {

            validationMock.when(() -> ValidationUtil.validateName(clientName))
                    .thenAnswer(invocation -> null);

            validationMock.when(() -> ValidationUtil.validateDates(startDate, endDate))
                    .thenAnswer(invocation -> null);

            when(salesApi.getSalesForClient(
                    eq(clientName),
                    any(ZonedDateTime.class),
                    any(ZonedDateTime.class)
            )).thenReturn(rows);

            List<ProductRow> result =
                    salesDto.getSalesForClient(clientName, startDate, endDate);

            assertEquals(1, result.size());
            verify(salesApi).getSalesForClient(
                    eq(clientName),
                    any(ZonedDateTime.class),
                    any(ZonedDateTime.class)
            );
        }
    }

    // ---------- GET SALES FOR PERIOD ----------

    @Test
    void testGetSalesForPeriodSuccess() throws ApiException {

        LocalDate startDate = LocalDate.now().minusDays(2);
        LocalDate endDate = LocalDate.now();

        SalesPojo pojo = new SalesPojo();
        DailySalesData data = new DailySalesData();

        try (
                MockedStatic<ValidationUtil> validationMock = mockStatic(ValidationUtil.class);
                MockedStatic<SalesHelper> helperMock = mockStatic(SalesHelper.class)
        ) {

            validationMock.when(() -> ValidationUtil.validateDates(startDate, endDate))
                    .thenAnswer(invocation -> null);

            when(salesApi.getDailySales(
                    any(ZonedDateTime.class),
                    any(ZonedDateTime.class)
            )).thenReturn(pojo);

            helperMock.when(() -> SalesHelper.convertToData(pojo))
                    .thenReturn(data);

            DailySalesData result =
                    salesDto.getSalesForPeriod(startDate, endDate);

            assertNotNull(result);
            verify(salesApi).getDailySales(
                    any(ZonedDateTime.class),
                    any(ZonedDateTime.class)
            );
        }
    }

    // ---------- GET ALL PAGINATED ----------

    @Test
    void testGetAllPaginatedSuccess() throws ApiException {

        SalesPojo pojo = new SalesPojo();
        Page<SalesPojo> page = new PageImpl<>(List.of(pojo));

        when(salesApi.getAllSales(0, 10)).thenReturn(page);

        try (MockedStatic<SalesHelper> helperMock = mockStatic(SalesHelper.class)) {

            helperMock.when(() -> SalesHelper.convertToData(pojo))
                    .thenReturn(new DailySalesData());

            PageForm pageForm = new PageForm();
            pageForm.setPage(0);
            pageForm.setSize(10);

            Page<DailySalesData> result =
                    salesDto.getAllPaginated(pageForm);

            assertEquals(1, result.getTotalElements());
            verify(salesApi).getAllSales(0, 10);
        }
    }
}
