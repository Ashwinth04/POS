package com.increff.pos.test.dto;

import com.increff.pos.api.SalesApiImpl;
import com.increff.pos.db.SalesPojo;
import com.increff.pos.dto.SalesDto;
import com.increff.pos.exception.ApiException;
import com.increff.pos.helper.SalesHelper;
import com.increff.pos.model.data.DailySalesData;
import com.increff.pos.model.data.ProductRow;
import com.increff.pos.model.form.PageForm;
import com.increff.pos.util.FormValidator;
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
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SalesDtoTest {

    @InjectMocks
    private SalesDto salesDto;

    @Mock
    private SalesApiImpl salesApi;

    @Mock
    private FormValidator formValidator;

    // ---------- getSalesForClient ----------

    @Test
    void testGetSalesForClient_success() throws ApiException {
        LocalDate start = LocalDate.now().minusDays(2);
        LocalDate end = LocalDate.now();

        List<ProductRow> rows = List.of(new ProductRow());

        try (MockedStatic<ValidationUtil> validation = mockStatic(ValidationUtil.class)) {

            validation.when(() -> ValidationUtil.validateName("client1"))
                    .thenAnswer(i -> null);
            validation.when(() -> ValidationUtil.validateDates(start, end))
                    .thenAnswer(i -> null);

            ZonedDateTime zs = start.atStartOfDay(ZoneId.systemDefault());
            ZonedDateTime ze = end.atTime(23, 59, 59, 999_000_000)
                    .atZone(ZoneId.systemDefault());

            when(salesApi.getSalesForClient("client1", zs, ze))
                    .thenReturn(rows);

            List<ProductRow> result =
                    salesDto.getSalesForClient("client1", start, end);

            assertEquals(1, result.size());
        }
    }

    // ---------- getSalesForPeriod ----------

    @Test
    void testGetSalesForPeriod_success() throws ApiException {
        LocalDate start = LocalDate.now().minusDays(1);
        LocalDate end = LocalDate.now();

        SalesPojo pojo = new SalesPojo();
        DailySalesData data = new DailySalesData();

        try (
                MockedStatic<ValidationUtil> validation = mockStatic(ValidationUtil.class);
                MockedStatic<SalesHelper> helper = mockStatic(SalesHelper.class)
        ) {
            validation.when(() -> ValidationUtil.validateDates(start, end))
                    .thenAnswer(i -> null);

            ZonedDateTime zs = start.atStartOfDay(ZoneId.systemDefault());
            ZonedDateTime ze = end.atTime(23, 59, 59, 999_000_000)
                    .atZone(ZoneId.systemDefault());

            when(salesApi.getDailySales(zs, ze)).thenReturn(pojo);
            helper.when(() -> SalesHelper.convertToData(pojo))
                    .thenReturn(data);

            DailySalesData result =
                    salesDto.getSalesForPeriod(start, end);

            assertNotNull(result);
        }
    }

    // ---------- getAllPaginated ----------

    @Test
    void testGetAllPaginated_success() throws ApiException {
        PageForm form = new PageForm();
        form.setPage(0);
        form.setSize(10);

        SalesPojo pojo = new SalesPojo();
        DailySalesData data = new DailySalesData();

        Page<SalesPojo> page = new PageImpl<>(List.of(pojo));

        try (MockedStatic<SalesHelper> helper = mockStatic(SalesHelper.class)) {
            when(salesApi.getAllSales(0, 10)).thenReturn(page);
            helper.when(() -> SalesHelper.convertToData(pojo))
                    .thenReturn(data);

            Page<DailySalesData> result =
                    salesDto.getAllPaginated(form);

            assertEquals(1, result.getContent().size());
            verify(formValidator).validate(form);
        }
    }

    // ---------- storeDailySales ----------

    @Test
    void testStoreDailySales() {
        ZonedDateTime start = ZonedDateTime.now().minusDays(1);
        ZonedDateTime end = ZonedDateTime.now();

        doNothing().when(salesApi).storeDailySales(start, end);

        salesDto.storeDailySales(start, end);

        verify(salesApi).storeDailySales(start, end);
    }
}
