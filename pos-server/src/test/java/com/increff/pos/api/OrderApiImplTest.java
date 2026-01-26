//package com.increff.pos.api;
//
//import com.increff.pos.dao.InventoryDao;
//import com.increff.pos.dao.OrderDao;
//import com.increff.pos.dao.ProductDao;
//import com.increff.pos.db.OrderPojo;
//import com.increff.pos.db.ProductPojo;
//import com.increff.pos.exception.ApiException;
//import com.increff.pos.model.data.OrderItem;
//import com.increff.pos.model.data.OrderStatus;
//import com.increff.pos.storage.StorageService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageImpl;
//import org.springframework.data.domain.Pageable;
//
//import java.io.IOException;
//import java.util.List;
//import java.util.Map;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class OrderApiImplTest {
//
//    @Mock private OrderDao orderDao;
//    @Mock private ProductDao productDao;
//    @Mock private InventoryDao inventoryDao;
//    @Mock private StorageService storageService;
//
//    @InjectMocks
//    private OrderApiImpl orderApi;
//
//    private OrderPojo validOrder;
//    private OrderItem item;
//
//    @BeforeEach
//    void setup() {
//        item = new OrderItem();
//        item.setBarcode("BAR123");
//        item.setOrderedQuantity(2);
//        item.setSellingPrice(50.0);
//
//        validOrder = new OrderPojo();
//        validOrder.setOrderItems(List.of(item));
//    }
//
//    // ---------- createOrder ----------
//
//    @Test
//    void createOrder_success_fulfillable() throws ApiException {
//        ProductPojo product = new ProductPojo();
//        product.setBarcode("BAR123");
//        product.setMrp(100.0);
//
//        when(productDao.findByBarcode("BAR123")).thenReturn(product);
//        when(inventoryDao.getQuantity("BAR123")).thenReturn(10);
//
//        Map<String, OrderStatus> result = orderApi.createOrder(validOrder);
//
//        assertEquals(1, result.size());
//        assertEquals("FULFILLABLE", validOrder.getOrderStatus());
//
//        verify(orderDao).save(validOrder);
//        verify(inventoryDao).updateInventory(any());
//    }
//
//    @Test
//    void createOrder_invalidBarcode() throws ApiException {
//        when(productDao.findByBarcode("BAR123")).thenReturn(null);
//
//        Map<String, OrderStatus> result = orderApi.createOrder(validOrder);
//
//        OrderStatus status = result.values().iterator().next();
//        assertEquals("INVALID", status.getStatus());
//        verify(orderDao, never()).save(any());
//    }
//
//    @Test
//    void createOrder_invalidSellingPrice() throws ApiException {
//        ProductPojo product = new ProductPojo();
//        product.setBarcode("BAR123");
//        product.setMrp(40.0); // selling price > mrp
//
//        when(productDao.findByBarcode("BAR123")).thenReturn(product);
//
//        Map<String, OrderStatus> result = orderApi.createOrder(validOrder);
//
//        OrderStatus status = result.values().iterator().next();
//        assertEquals("INVALID", status.getStatus());
//        verify(orderDao, never()).save(any());
//    }
//
//    @Test
//    void createOrder_unfulfillable_inventoryLow() throws ApiException {
//        ProductPojo product = new ProductPojo();
//        product.setBarcode("BAR123");
//        product.setMrp(100.0);
//
//        when(productDao.findByBarcode("BAR123")).thenReturn(product);
//        when(inventoryDao.getQuantity("BAR123")).thenReturn(1); // less than required
//
//        Map<String, OrderStatus> result = orderApi.createOrder(validOrder);
//
//        OrderStatus status = result.values().iterator().next();
//        assertEquals("UNFULFILLABLE", status.getStatus());
//        assertEquals("UNFULFILLABLE", validOrder.getOrderStatus());
//
//        verify(orderDao).save(validOrder);
//        verify(inventoryDao, never()).updateInventory(any());
//    }
//
//    // ---------- getInvoice ----------
//
//    @Test
//    void getInvoice_success() throws Exception {
//        when(storageService.readInvoice("order1")).thenReturn("data".getBytes());
//
//        byte[] result = orderApi.getInvoice("order1");
//
//        assertNotNull(result);
//    }
//
//    @Test
//    void getInvoice_ioException_wrappedAsApiException() throws Exception {
//        when(storageService.readInvoice("order1")).thenThrow(new IOException());
//
//        ApiException ex = assertThrows(ApiException.class,
//                () -> orderApi.getInvoice("order1"));
//
//        assertTrue(ex.getMessage().contains("Failed to fetch invoice"));
//    }
//
//    // ---------- getAllOrders ----------
//
//    @Test
//    void getAllOrders_returnsPage() {
//        Page<OrderPojo> page = new PageImpl<>(List.of(validOrder));
//        when(orderDao.findAll((Pageable) any())).thenReturn(page);
//
//        Page<OrderPojo> result = orderApi.getAllOrders(0, 10);
//
//        assertEquals(1, result.getTotalElements());
//    }
//}
