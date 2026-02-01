package com.increff.pos.test.api;

import com.increff.pos.api.SalesApiImpl;
import com.increff.pos.dao.SalesDao;
import com.increff.pos.db.SalesPojo;
import com.increff.pos.model.data.ProductRow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SalesApiImplTest {

    @Mock
    private SalesDao salesDao;

    @InjectMocks
    private SalesApiImpl salesApi;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    // ---------- getSalesForClient ----------

    @Test
    void shouldGetSalesForClient() {
        ZonedDateTime start = ZonedDateTime.now().minusDays(5);
        ZonedDateTime end = ZonedDateTime.now();

        ProductRow row = new ProductRow();
        row.setProduct("Pen");

        when(salesDao.getSalesReport("ClientA", start, end))
                .thenReturn(List.of(row));

        List<ProductRow> result =
                salesApi.getSalesForClient("ClientA", start, end);

        assertEquals(1, result.size());
        assertEquals("Pen", result.get(0).getProduct());

        verify(salesDao).getSalesReport("ClientA", start, end);
    }

    // ---------- getDailySales ----------

    @Test
    void shouldGetDailySales() {
        ZonedDateTime start = ZonedDateTime.now().minusDays(1);
        ZonedDateTime end = ZonedDateTime.now();

        SalesPojo pojo = new SalesPojo();

        when(salesDao.getDailySalesData(start, end))
                .thenReturn(pojo);

        SalesPojo result = salesApi.getDailySales(start, end);

        assertNotNull(result);
        verify(salesDao).getDailySalesData(start, end);
    }

    // ---------- storeDailySales (new record) ----------

    @Test
    void shouldStoreDailySalesWhenNoExistingRecord() {
        ZonedDateTime start = ZonedDateTime.now().minusDays(1);
        ZonedDateTime end = ZonedDateTime.now();

        SalesPojo data = new SalesPojo();

        when(salesDao.getDailySalesData(start, end))
                .thenReturn(data);

        when(salesDao.findByDate(start))
                .thenReturn(null);

        salesApi.storeDailySales(start, end);

        assertNotNull(data.getDate());
        assertEquals(
                ZoneId.of("Asia/Kolkata"),
                data.getDate().getZone()
        );

        verify(salesDao).save(data);
    }

    // ---------- storeDailySales (update existing) ----------

    @Test
    void shouldUpdateDailySalesWhenRecordExists() {
        ZonedDateTime start = ZonedDateTime.now().minusDays(1);
        ZonedDateTime end = ZonedDateTime.now();

        SalesPojo newData = new SalesPojo();
        SalesPojo existing = new SalesPojo();
        existing.setId("existing-id");

        when(salesDao.getDailySalesData(start, end))
                .thenReturn(newData);

        when(salesDao.findByDate(start))
                .thenReturn(existing);

        salesApi.storeDailySales(start, end);

        assertEquals("existing-id", newData.getId());
        verify(salesDao).save(newData);
    }

    // ---------- getAllSales ----------

    @Test
    void shouldGetAllSalesPaginated() {
        SalesPojo pojo = new SalesPojo();
        Page<SalesPojo> page =
                new PageImpl<>(List.of(pojo));

        when(salesDao.findAll(any(PageRequest.class)))
                .thenReturn(page);

        Page<SalesPojo> result =
                salesApi.getAllSales(0, 10);

        assertEquals(1, result.getContent().size());
        verify(salesDao).findAll(any(PageRequest.class));
    }
}
