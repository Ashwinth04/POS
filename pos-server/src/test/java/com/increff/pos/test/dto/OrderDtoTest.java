//package com.increff.pos.test.dto;
//
//import com.increff.pos.db.OrderItemPojo;
//import com.increff.pos.db.OrderPojo;
//import com.increff.pos.db.ProductPojo;
//import com.increff.pos.dto.OrderDto;
//import com.increff.pos.exception.ApiException;
//import com.increff.pos.flow.OrderFlow;
//import com.increff.pos.model.data.*;
//import com.increff.pos.model.form.*;
//import com.increff.pos.wrapper.InvoiceClientWrapper;
//import com.increff.pos.util.FormValidator;
//import com.increff.pos.util.ValidationUtil;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockedStatic;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.data.domain.*;
//
//import java.time.LocalDate;
//import java.time.ZoneId;
//import java.time.ZonedDateTime;
//import java.util.*;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class OrderDtoTest {
//
//    @InjectMocks
//    private OrderDto orderDto;
//
//    @Mock
//    private OrderFlow orderFlow;
//
//    @Mock
//    private InvoiceClientWrapper invoiceClientWrapper;
//
//    @Mock
//    private FormValidator formValidator;
//
//    private OrderForm orderForm;
//    private OrderPojo orderPojo;
//    private ProductPojo productPojo;
//    private static final String ORDER_ID = "507f1f77bcf86cd799439011";
//
//    @BeforeEach
//    void setUp() {
//        OrderItemForm item = new OrderItemForm();
//        item.setBarcode("B1");
//        item.setOrderedQuantity(2);
//        item.setSellingPrice(100.0);
//
//        orderForm = new OrderForm();
//        orderForm.setOrderItems(List.of(item));
//
//        productPojo = new ProductPojo();
//        productPojo.setId("P1");
//        productPojo.setBarcode("B1");
//        productPojo.setMrp(200.0);
//
//        OrderItemPojo record = new OrderItemPojo();
//        record.setProductId("P1");
//        record.setOrderedQuantity(2);
//        record.setSellingPrice(100.0);
//
//        orderPojo = new OrderPojo();
//        orderPojo.setOrderId(ORDER_ID);
//        orderPojo.setOrderStatus("FULFILLABLE");
//        orderPojo.setOrderItems(List.of(record));
//    }
//
//    /* ---------------- CANCEL ORDER ---------------- */
//
//    @Test
//    void cancelOrder_success() throws Exception {
//        MessageData msg = new MessageData();
//        msg.setMessage("Cancelled");
//
//        when(orderFlow.cancelOrder(ORDER_ID)).thenReturn(msg);
//
//        MessageData result = orderDto.cancelOrder(ORDER_ID);
//
//        assertEquals("Cancelled", result.getMessage());
//    }
//
//    /* ---------------- GET ALL ORDERS ---------------- */
//
//    @Test
//    void getAllOrders_success() throws Exception {
//        Page<OrderPojo> page = new PageImpl<>(List.of(orderPojo));
//
//        when(orderFlow.getAllOrders(0, 10)).thenReturn(page);
//        when(orderFlow.mapProductIdsToProductPojos(anyList()))
//                .thenReturn(Map.of("P1", productPojo));
//
//        PageForm form = new PageForm();
//        form.setPage(0);
//        form.setSize(10);
//
//        Page<OrderData> result = orderDto.getAllOrders(form);
//
//        assertEquals(1, result.getTotalElements());
//    }
//
//    /* ---------------- GENERATE INVOICE ---------------- */
//
//    @Test
//    void generateInvoice_success() throws Exception {
//        when(orderFlow.getOrder(ORDER_ID)).thenReturn(orderPojo);
//        when(orderFlow.mapProductIdsToProductPojos(anyList()))
//                .thenReturn(Map.of("P1", productPojo));
//
//        FileData fileData = new FileData();
//        when(invoiceClientWrapper.generateInvoice(any())).thenReturn(fileData);
//
//        FileData result = orderDto.generateInvoice(ORDER_ID);
//
//        assertNotNull(result);
//        verify(orderFlow).updatePlacedStatus(ORDER_ID);
//    }
//
//    @Test
//    void generateInvoice_invalidStatus() throws Exception {
//        orderPojo.setOrderStatus("CANCELLED");
//        when(orderFlow.getOrder(ORDER_ID)).thenReturn(orderPojo);
//
//        ApiException ex = assertThrows(ApiException.class,
//                () -> orderDto.generateInvoice(ORDER_ID));
//
//        assertEquals("ORDER CANNOT BE PLACED", ex.getMessage());
//    }
//
//    /* ---------------- DOWNLOAD INVOICE ---------------- */
//
//    @Test
//    void downloadInvoice_success() throws Exception {
//        FileData fileData = new FileData();
//
//        when(invoiceClientWrapper.downloadInvoice("O1")).thenReturn(fileData);
//
//        FileData result = orderDto.downloadInvoice("O1");
//
//        assertNotNull(result);
//        verify(orderFlow).checkInvoiceDownloadable("O1");
//    }
//
//    /* ---------------- FILTER ORDERS ---------------- */
//
//    @Test
//    void filterOrders_success() throws Exception {
//        Page<OrderPojo> page = new PageImpl<>(List.of(orderPojo));
//
//        when(orderFlow.filterOrders(any(), any(), eq(0), eq(10))).thenReturn(page);
//        when(orderFlow.mapProductIdsToProductPojos(anyList()))
//                .thenReturn(Map.of("P1", productPojo));
//
//        Page<OrderData> result = orderDto.filterOrders(
//                LocalDate.now().minusDays(1),
//                LocalDate.now(),
//                0,
//                10
//        );
//
//        assertEquals(1, result.getTotalElements());
//    }
//
//    /* ---------------- VALIDATE ALL ORDER ITEMS ---------------- */
//
//    @Test
//    void validateAllOrderItems_success() throws Exception {
//        when(orderFlow.mapBarcodesToProductPojos(anyList()))
//                .thenReturn(Map.of("B1", productPojo));
//
//        Map<String, ProductPojo> result =
//                orderDto.validateAllOrderItems(orderForm);
//
//        assertEquals(1, result.size());
//    }
//
//    @Test
//    void validateAllOrderItems_invalidBarcode() {
//        when(orderFlow.mapBarcodesToProductPojos(anyList()))
//                .thenReturn(Collections.emptyMap());
//
//        ApiException ex = assertThrows(ApiException.class,
//                () -> orderDto.validateAllOrderItems(orderForm));
//
//        assertTrue(ex.getMessage().contains("Invalid barcode"));
//    }
//
//    @Test
//    void validateItem_priceExceedsMrp() {
//        OrderItemForm item = new OrderItemForm();
//        item.setBarcode("B1");
//        item.setSellingPrice(500.0);
//
//        ApiException ex = assertThrows(ApiException.class,
//                () -> orderDto.validateOrderItem(item, Map.of("B1", productPojo)));
//
//        assertTrue(ex.getMessage().contains("Selling price exceeds MRP"));
//    }
//
//    /* ---------------- SEARCH BY ID ---------------- */
//
//    @Test
//    void searchById_success() throws Exception {
//        Page<OrderPojo> page = new PageImpl<>(List.of(orderPojo));
//
//        when(orderFlow.searchById(eq("O1"), eq(0), eq(10))).thenReturn(page);
//        when(orderFlow.mapProductIdsToProductPojos(anyList()))
//                .thenReturn(Map.of("P1", productPojo));
//
//        SearchOrderForm form = new SearchOrderForm();
//        form.setOrderId("O1");
//        form.setPage(0);
//        form.setSize(10);
//
//        Page<OrderData> result = orderDto.searchById(form);
//
//        assertEquals(1, result.getTotalElements());
//    }
//}
