package com.increff.pos.api;

import com.increff.pos.dao.SalesDao;
import com.increff.pos.db.SalesPojo;
import com.increff.pos.model.data.ProductRow;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SalesApiImplTest {

    @Mock
    private SalesDao salesDao;

    @InjectMocks
    private SalesApiImpl salesApi;

    // ---------- getSalesForClient ----------

    @Test
    void testGetSalesForClient_success() {
        ZonedDateTime start = ZonedDateTime.now().minusDays(1);
        ZonedDateTime end = ZonedDateTime.now();

        List<ProductRow> rows = List.of(new ProductRow());

        when(salesDao.getSalesReport("ClientA", start, end))
                .thenReturn(rows);

        List<ProductRow> result =
                salesApi.getSalesForClient("ClientA", start, end);

        assertThat(result).hasSize(1);
    }

    // ---------- getDailySales ----------

    @Test
    void testGetDailySales_success() {
        ZonedDateTime start = ZonedDateTime.now().minusDays(1);
        ZonedDateTime end = ZonedDateTime.now();

        SalesPojo pojo = new SalesPojo();

        when(salesDao.getDailySalesData(start, end))
                .thenReturn(pojo);

        SalesPojo result =
                salesApi.getDailySales(start, end);

        assertThat(result).isEqualTo(pojo);
    }

    // ---------- storeDailySales ----------

    @Test
    void testStoreDailySales_noExistingRecord() {
        ZonedDateTime start = ZonedDateTime.now().minusDays(1);
        ZonedDateTime end = ZonedDateTime.now();

        SalesPojo newData = new SalesPojo();

        when(salesDao.getDailySalesData(start, end))
                .thenReturn(newData);

        when(salesDao.findByDate(start))
                .thenReturn(null);

        salesApi.storeDailySales(start, end);

        assertThat(newData.getDate())
                .isNotNull();

        verify(salesDao).save(newData);
    }

    @Test
    void testStoreDailySales_existingRecord() {
        ZonedDateTime start = ZonedDateTime.now().minusDays(1);
        ZonedDateTime end = ZonedDateTime.now();

        SalesPojo newData = new SalesPojo();
        SalesPojo existing = new SalesPojo();
        existing.setId("EXISTING_ID");

        when(salesDao.getDailySalesData(start, end))
                .thenReturn(newData);

        when(salesDao.findByDate(start))
                .thenReturn(existing);

        salesApi.storeDailySales(start, end);

        assertThat(newData.getId())
                .isEqualTo("EXISTING_ID");

        verify(salesDao).save(newData);
    }

    // ---------- getAllSales ----------

    @Test
    void testGetAllSales_success() {
        Page<SalesPojo> page =
                new PageImpl<>(List.of(new SalesPojo()));

        when(salesDao.findAll(any(Pageable.class)))
                .thenReturn(page);

        Page<SalesPojo> result =
                salesApi.getAllSales(0, 10);

        assertThat(result.getContent()).hasSize(1);
    }
}
