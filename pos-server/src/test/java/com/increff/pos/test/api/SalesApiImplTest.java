//package com.increff.pos.test.api;
//
//import com.increff.pos.api.SalesApiImpl;
//import com.increff.pos.dao.SalesDao;
//import com.increff.pos.db.documents.SalesPojo;
//import com.increff.pos.model.data.ProductRevenueRow;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageImpl;
//import org.springframework.data.domain.PageRequest;
//
//import java.time.ZonedDateTime;
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class SalesApiImplTest {
//
//    @Mock
//    private SalesDao salesDao;
//
//    @InjectMocks
//    private SalesApiImpl salesApi;
//
//    // ---------- getSalesForClient ----------
//
//    @Test
//    void getSalesForClient_success() {
//        ProductRevenueRow row = new ProductRevenueRow();
//
//        ZonedDateTime start = ZonedDateTime.now().minusDays(5);
//        ZonedDateTime end = ZonedDateTime.now();
//
//        when(salesDao.getSalesReport("client1", start, end))
//                .thenReturn(List.of(row));
//
//        List<ProductRevenueRow> result =
//                salesApi.getSalesForClient("client1", start, end);
//
//        assertEquals(1, result.size());
//        verify(salesDao).getSalesReport("client1", start, end);
//    }
//
//    // ---------- getDailySales ----------
//
//    @Test
//    void getDailySales_success() {
//        ZonedDateTime start = ZonedDateTime.now().minusDays(1);
//        ZonedDateTime end = ZonedDateTime.now();
//
//        SalesPojo pojo = new SalesPojo();
//
//        when(salesDao.getDailySalesData(start, end)).thenReturn(pojo);
//
//        SalesPojo result = salesApi.getDailySales(start, end);
//
//        assertEquals(pojo, result);
//    }
//
//    // ---------- storeDailySales ----------
//
//    @Test
//    void storeDailySales_newRecord() {
//        ZonedDateTime start = ZonedDateTime.now().minusDays(1);
//        ZonedDateTime end = ZonedDateTime.now();
//
//        SalesPojo pojo = new SalesPojo();
//
//        when(salesDao.getDailySalesData(start, end)).thenReturn(pojo);
//        when(salesDao.findByDate(start)).thenReturn(null);
//
//        salesApi.storeDailySales(start, end);
//
//        assertNotNull(pojo.getDate());
//        verify(salesDao).save(pojo);
//    }
//
//    // ---------- getAllSales ----------
//
//    @Test
//    void getAllSales_success() {
//        Page<SalesPojo> page =
//                new PageImpl<>(List.of(new SalesPojo()));
//
//        when(salesDao.findAll(any(PageRequest.class)))
//                .thenReturn(page);
//
//        Page<SalesPojo> result = salesApi.getAllSales(0, 10);
//
//        assertEquals(1, result.getContent().size());
//    }
//}
