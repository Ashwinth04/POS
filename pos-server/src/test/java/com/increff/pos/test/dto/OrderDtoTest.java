package com.increff.pos.test.dto;

import com.increff.pos.db.OrderPojo;
import com.increff.pos.db.ProductPojo;
import com.increff.pos.dto.OrderDto;
import com.increff.pos.exception.ApiException;
import com.increff.pos.flow.OrderFlow;
import com.increff.pos.helper.OrderHelper;
import com.increff.pos.model.data.*;
import com.increff.pos.model.form.OrderForm;
import com.increff.pos.model.form.OrderItemForm;
import com.increff.pos.model.form.PageForm;
import com.increff.pos.wrapper.InvoiceClientWrapper;
import com.increff.pos.util.FormValidator;
import com.increff.pos.util.NormalizationUtil;
import com.increff.pos.util.ValidationUtil;
import org.junit.jupiter.api.BeforeEach;
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
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderDtoTest {

    @InjectMocks
    private OrderDto orderDto;

    @Mock
    private OrderFlow orderFlow;

    @Mock
    private InvoiceClientWrapper invoiceClientWrapper;

    @Mock
    private FormValidator formValidator;

    private OrderForm orderForm;
    private OrderItemForm itemForm;
    private ProductPojo productPojo;

    @BeforeEach
    void setup() {
        itemForm = new OrderItemForm();
        itemForm.setBarcode("b1");
        itemForm.setSellingPrice(50.0);

        orderForm = new OrderForm();
        orderForm.setOrderItems(List.of(itemForm));

        productPojo = new ProductPojo();
        productPojo.setMrp(100.0);
    }

    // ---------- createOrder ----------

    @Test
    void testCreateOrder_success() throws ApiException {
        OrderPojo pojo = new OrderPojo();
        OrderData data = new OrderData();

        try (
                MockedStatic<NormalizationUtil> norm = mockStatic(NormalizationUtil.class);
                MockedStatic<OrderHelper> helper = mockStatic(OrderHelper.class)
        ) {
            when(orderFlow.mapBarcodesToProductPojos(anyList()))
                    .thenReturn(Map.of("b1", productPojo));
            when(orderFlow.createOrder(any())).thenReturn(pojo);

            helper.when(() -> OrderHelper.convertToEntity(orderForm)).thenReturn(pojo);
            helper.when(() -> OrderHelper.convertToData(pojo)).thenReturn(data);

            OrderData result = orderDto.createOrder(orderForm);

            assertNotNull(result);
            verify(formValidator).validate(orderForm);
        }
    }

    // ---------- editOrder ----------

    @Test
    void testEditOrder_success() throws ApiException {
        OrderPojo pojo = new OrderPojo();
        OrderData data = new OrderData();

        try (
                MockedStatic<NormalizationUtil> norm = mockStatic(NormalizationUtil.class);
                MockedStatic<OrderHelper> helper = mockStatic(OrderHelper.class);
                MockedStatic<ValidationUtil> validation = mockStatic(ValidationUtil.class)
        ) {
            validation.when(() -> ValidationUtil.validateOrderId("o1")).thenAnswer(i -> null);

            when(orderFlow.mapBarcodesToProductPojos(anyList()))
                    .thenReturn(Map.of("b1", productPojo));
            when(orderFlow.editOrder(any(), eq("o1"))).thenReturn(pojo);

            helper.when(() -> OrderHelper.convertToEntity(orderForm)).thenReturn(pojo);
            helper.when(() -> OrderHelper.convertToData(pojo)).thenReturn(data);

            OrderData result = orderDto.editOrder(orderForm, "o1");

            assertNotNull(result);
        }
    }

    // ---------- cancelOrder ----------

    @Test
    void testCancelOrder() throws ApiException {
        try (MockedStatic<ValidationUtil> validation = mockStatic(ValidationUtil.class)) {
            validation.when(() -> ValidationUtil.validateOrderId("o2")).thenAnswer(i -> null);

            when(orderFlow.cancelOrder("o2")).thenReturn(new MessageData());

            MessageData result = orderDto.cancelOrder("o2");

            assertNotNull(result);
        }
    }

    // ---------- getAllOrders ----------

    @Test
    void testGetAllOrders() throws ApiException {
        PageForm form = new PageForm();
        form.setPage(0);
        form.setSize(10);

        OrderPojo pojo = new OrderPojo();
        OrderData data = new OrderData();

        Page<OrderPojo> page = new PageImpl<>(List.of(pojo));

        try (MockedStatic<OrderHelper> helper = mockStatic(OrderHelper.class)) {
            when(orderFlow.getAllOrders(0, 10)).thenReturn(page);
            helper.when(() -> OrderHelper.convertToData(pojo)).thenReturn(data);

            Page<OrderData> result = orderDto.getAllOrders(form);

            assertEquals(1, result.getContent().size());
        }
    }

    // ---------- generateInvoice ----------

    @Test
    void testGenerateInvoice_success() throws ApiException {
        OrderPojo pojo = new OrderPojo();
        pojo.setOrderStatus("FULFILLABLE");

        OrderData data = new OrderData();
        FileData file = new FileData();

        try (
                MockedStatic<ValidationUtil> validation = mockStatic(ValidationUtil.class);
                MockedStatic<OrderHelper> helper = mockStatic(OrderHelper.class)
        ) {
            validation.when(() -> ValidationUtil.validateOrderId("o3")).thenAnswer(i -> null);

            when(orderFlow.getOrder("o3")).thenReturn(pojo);
            helper.when(() -> OrderHelper.convertToData(pojo)).thenReturn(data);
            when(invoiceClientWrapper.generateInvoice(data)).thenReturn(file);

            FileData result = orderDto.generateInvoice("o3");

            assertNotNull(result);
            verify(orderFlow).updatePlacedStatus("o3");
        }
    }

    @Test
    void testGenerateInvoice_notFulfillable() throws ApiException {
        OrderPojo pojo = new OrderPojo();
        pojo.setOrderStatus("CREATED");

        try (MockedStatic<ValidationUtil> validation = mockStatic(ValidationUtil.class)) {
            validation.when(() -> ValidationUtil.validateOrderId("o4")).thenAnswer(i -> null);
            when(orderFlow.getOrder("o4")).thenReturn(pojo);

            ApiException ex = assertThrows(ApiException.class,
                    () -> orderDto.generateInvoice("o4"));

            assertTrue(ex.getMessage().contains("ORDER CANNOT BE PLACED"));
        }
    }

    // ---------- downloadInvoice ----------

    @Test
    void testDownloadInvoice() throws ApiException {
        doNothing().when(orderFlow).checkInvoiceDownloadable("o5");
        when(invoiceClientWrapper.downloadInvoice("o5")).thenReturn(new FileData());

        FileData result = orderDto.downloadInvoice("o5");

        assertNotNull(result);
    }

    // ---------- filterOrders ----------

    @Test
    void testFilterOrders() throws ApiException {
        LocalDate start = LocalDate.now().minusDays(1);
        LocalDate end = LocalDate.now();

        OrderPojo pojo = new OrderPojo();
        OrderData data = new OrderData();

        Page<OrderPojo> page = new PageImpl<>(List.of(pojo));

        try (
                MockedStatic<ValidationUtil> validation = mockStatic(ValidationUtil.class);
                MockedStatic<OrderHelper> helper = mockStatic(OrderHelper.class)
        ) {
            validation.when(() -> ValidationUtil.validateDates(start, end)).thenAnswer(i -> null);

            ZonedDateTime zs = start.atStartOfDay(ZoneId.systemDefault());
            ZonedDateTime ze = end.atTime(23, 59, 59, 999_000_000).atZone(ZoneId.systemDefault());

            when(orderFlow.filterOrders(zs, ze, 0, 10)).thenReturn(page);
            helper.when(() -> OrderHelper.convertToData(pojo)).thenReturn(data);

            Page<OrderData> result = orderDto.filterOrders(start, end, 0, 10);

            assertEquals(1, result.getContent().size());
        }
    }

    // ---------- validateAllOrderItems ----------

    @Test
    void testValidateAllOrderItems_success() throws ApiException {
        when(orderFlow.mapBarcodesToProductPojos(anyList()))
                .thenReturn(Map.of("b1", productPojo));

        orderDto.validateAllOrderItems(orderForm);
    }

    @Test
    void testValidateAllOrderItems_invalidItem() {
        when(orderFlow.mapBarcodesToProductPojos(anyList()))
                .thenReturn(Map.of());

        ApiException ex = assertThrows(ApiException.class,
                () -> orderDto.validateAllOrderItems(orderForm));

        assertTrue(ex.getMessage().contains("Invalid barcode"));
    }

    // ---------- validateItem ----------

    @Test
    void testValidateItem_success() throws ApiException {
        orderDto.validateItem(itemForm, Map.of("b1", productPojo));
    }

    @Test
    void testValidateItem_invalidBarcode() {
        ApiException ex = assertThrows(ApiException.class,
                () -> orderDto.validateItem(itemForm, Map.of()));

        assertTrue(ex.getMessage().contains("Invalid barcode"));
    }

    @Test
    void testValidateItem_priceExceedsMrp() {
        itemForm.setSellingPrice(200.0);

        ApiException ex = assertThrows(ApiException.class,
                () -> orderDto.validateItem(itemForm, Map.of("b1", productPojo)));

        assertTrue(ex.getMessage().contains("Selling price exceeds MRP"));
    }

    @Test
    void testValidateItem_priceZeroOrNegative() {
        itemForm.setSellingPrice(0.0);

        ApiException ex = assertThrows(ApiException.class,
                () -> orderDto.validateItem(itemForm, Map.of("b1", productPojo)));

        assertTrue(ex.getMessage().contains("Selling price exceeds MRP"));
    }
}
