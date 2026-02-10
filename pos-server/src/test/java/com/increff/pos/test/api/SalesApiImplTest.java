package com.increff.pos.test.api;

import com.increff.pos.api.SalesApiImpl;
import com.increff.pos.dao.SalesDao;
import com.increff.pos.db.documents.SalesPojo;
import com.increff.pos.model.data.ProductRevenueRow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;

import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SalesApiImplTest {

    @InjectMocks
    private SalesApiImpl salesApi;

    @Mock
    private SalesDao salesDao;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    // ---------- getSalesForClient ----------
    @Test
    void testGetSalesForClient() {
        ZonedDateTime start = ZonedDateTime.now().minusDays(1);
        ZonedDateTime end = ZonedDateTime.now();

        List<ProductRevenueRow> rows = List.of(new ProductRevenueRow());

        when(salesDao.getSalesReport("client1", start, end))
                .thenReturn(rows);

        List<ProductRevenueRow> result =
                salesApi.getSalesForClient("client1", start, end);

        assertEquals(1, result.size());
        verify(salesDao).getSalesReport("client1", start, end);
    }

    // ---------- getDailySales ----------
    @Test
    void testGetDailySales() {
        ZonedDateTime start = ZonedDateTime.now().minusDays(1);
        ZonedDateTime end = ZonedDateTime.now();

        SalesPojo pojo = new SalesPojo();

        when(salesDao.getDailySalesData(start, end))
                .thenReturn(pojo);

        SalesPojo result = salesApi.getDailySales(start, end);

        assertEquals(pojo, result);
        verify(salesDao).getDailySalesData(start, end);
    }

    // ---------- storeDailySales (existing record) ----------
    @Test
    void testStoreDailySalesWhenAlreadyExists() {
        ZonedDateTime start = ZonedDateTime.now().minusDays(1);
        ZonedDateTime end = ZonedDateTime.now();

        SalesPojo existing = new SalesPojo();

        when(salesDao.findByDate(start)).thenReturn(existing);

        salesApi.storeDailySales(start, end);

        verify(salesDao).findByDate(start);
        verify(salesDao, never()).save(any());
        verify(salesDao, never()).getDailySalesData(any(), any());
    }

    // ---------- storeDailySales (new record) ----------
    @Test
    void testStoreDailySalesWhenNotExists() {
        ZonedDateTime start = ZonedDateTime.now().minusDays(1);
        ZonedDateTime end = ZonedDateTime.now();

        SalesPojo computed = new SalesPojo();

        when(salesDao.findByDate(start)).thenReturn(null);
        when(salesDao.getDailySalesData(start, end)).thenReturn(computed);

        salesApi.storeDailySales(start, end);

        assertEquals(start, computed.getDate());
        verify(salesDao).save(computed);
    }

    // ---------- getAllSales ----------
    @Test
    void testGetAllSales() {
        Page<SalesPojo> page =
                new PageImpl<>(List.of(new SalesPojo()));

        when(salesDao.findAll(any(Pageable.class)))
                .thenReturn(page);

        Page<SalesPojo> result = salesApi.getAllSales(0, 10);

        assertEquals(1, result.getContent().size());
        verify(salesDao).findAll(any(Pageable.class));
    }
}
