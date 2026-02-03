package com.increff.pos.test.dto;

import com.increff.pos.api.ProductApiImpl;
import com.increff.pos.client.InvoiceClient;
import com.increff.pos.db.OrderPojo;
import com.increff.pos.dto.OrderDto;
import com.increff.pos.exception.ApiException;
import com.increff.pos.flow.OrderFlow;
import com.increff.pos.helper.OrderHelper;
import com.increff.pos.model.data.FileData;
import com.increff.pos.model.data.MessageData;
import com.increff.pos.model.data.OrderData;
import com.increff.pos.model.form.OrderForm;
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
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderDtoTest {

    @Mock
    private OrderFlow orderFlow;

    @Mock
    private InvoiceClient invoiceClient;

    @Mock
    private ProductApiImpl productApi;

    @InjectMocks
    private OrderDto orderDto;

    // ---------- CREATE ORDER ----------

    @Test
    void testCreateOrderSuccess() throws ApiException {

        OrderForm form = new OrderForm();
        OrderPojo pojo = new OrderPojo();
        OrderPojo savedPojo = new OrderPojo();
        OrderData data = new OrderData();

        try (MockedStatic<OrderHelper> helperMock = mockStatic(OrderHelper.class)) {

            helperMock.when(() -> OrderHelper.convertToEntity(form))
                    .thenReturn(pojo);

            helperMock.when(() -> OrderHelper.convertToData(savedPojo))
                    .thenReturn(data);

            doNothing().when(productApi).validateAllOrderItems(pojo);
            when(orderFlow.createOrder(pojo)).thenReturn(savedPojo);

            OrderData result = orderDto.createOrder(form);

            assertNotNull(result);
            verify(productApi).validateAllOrderItems(pojo);
            verify(orderFlow).createOrder(pojo);
        }
    }

    // ---------- EDIT ORDER ----------

    @Test
    void testEditOrderSuccess() throws ApiException {

        OrderForm form = new OrderForm();
        String orderId = "ORD-1";

        OrderPojo pojo = new OrderPojo();
        OrderPojo updatedPojo = new OrderPojo();
        OrderData data = new OrderData();

        try (
                MockedStatic<OrderHelper> helperMock = mockStatic(OrderHelper.class);
                MockedStatic<ValidationUtil> validationMock = mockStatic(ValidationUtil.class)
        ) {

            validationMock.when(() -> ValidationUtil.validateOrderId(orderId))
                    .thenAnswer(invocation -> null);

            helperMock.when(() -> OrderHelper.convertToEntity(form))
                    .thenReturn(pojo);

            helperMock.when(() -> OrderHelper.convertToData(updatedPojo))
                    .thenReturn(data);

            doNothing().when(productApi).validateAllOrderItems(any());
            when(orderFlow.editOrder(any(OrderPojo.class), eq(orderId)))
                    .thenReturn(updatedPojo);

            OrderData result = orderDto.editOrder(form, orderId);

            assertNotNull(result);
            verify(orderFlow).editOrder(any(OrderPojo.class), eq(orderId));
        }
    }

    // ---------- CANCEL ORDER ----------

    @Test
    void testCancelOrderSuccess() throws ApiException {

        String orderId = "ORD-1";
        MessageData message = new MessageData();

        try (MockedStatic<ValidationUtil> validationMock = mockStatic(ValidationUtil.class)) {

            validationMock.when(() -> ValidationUtil.validateOrderId(orderId))
                    .thenAnswer(invocation -> null);

            when(orderFlow.cancelOrder(orderId)).thenReturn(message);

            MessageData result = orderDto.cancelOrder(orderId);

            assertNotNull(result);
            verify(orderFlow).cancelOrder(orderId);
        }
    }

    // ---------- GET ALL ORDERS ----------

    @Test
    void testGetAllOrders() throws ApiException {

        OrderPojo pojo = new OrderPojo();
        Page<OrderPojo> pojoPage = new PageImpl<>(List.of(pojo));

        when(orderFlow.getAllOrders(0, 10)).thenReturn(pojoPage);

        PageForm pageForm = new PageForm();
        pageForm.setPage(0);
        pageForm.setSize(10);

        try (MockedStatic<OrderHelper> helperMock = mockStatic(OrderHelper.class)) {

            helperMock.when(() -> OrderHelper.convertToData(any()))
                    .thenReturn(new OrderData());

            Page<OrderData> result = orderDto.getAllOrders(pageForm);

            assertEquals(1, result.getTotalElements());
            verify(orderFlow).getAllOrders(0, 10);
        }
    }

    // ---------- GENERATE INVOICE ----------

    @Test
    void testGenerateInvoiceSuccess() throws ApiException {

        String orderId = "ORD-1";

        OrderPojo pojo = new OrderPojo();
        pojo.setOrderStatus("FULFILLABLE");

        OrderData data = new OrderData();
        FileData fileData = new FileData();

        try (
                MockedStatic<ValidationUtil> validationMock = mockStatic(ValidationUtil.class);
                MockedStatic<OrderHelper> helperMock = mockStatic(OrderHelper.class)
        ) {

            validationMock.when(() -> ValidationUtil.validateOrderId(orderId))
                    .thenAnswer(invocation -> null);

            when(orderFlow.getOrder(orderId)).thenReturn(pojo);
            helperMock.when(() -> OrderHelper.convertToData(pojo)).thenReturn(data);
            when(invoiceClient.generateInvoice(data)).thenReturn(fileData);

            doNothing().when(orderFlow).updatePlacedStatus(orderId);

            FileData result = orderDto.generateInvoice(orderId);

            assertNotNull(result);
            verify(orderFlow).updatePlacedStatus(orderId);
        }
    }

    @Test
    void testGenerateInvoiceInvalidStatus() throws ApiException {

        String orderId = "ORD-1";
        OrderPojo pojo = new OrderPojo();
        pojo.setOrderStatus("CANCELLED");

        try (MockedStatic<ValidationUtil> validationMock = mockStatic(ValidationUtil.class)) {

            validationMock.when(() -> ValidationUtil.validateOrderId(orderId))
                    .thenAnswer(invocation -> null);

            when(orderFlow.getOrder(orderId)).thenReturn(pojo);

            assertThrows(ApiException.class, () ->
                    orderDto.generateInvoice(orderId)
            );
        }
    }

    // ---------- DOWNLOAD INVOICE ----------

    @Test
    void testDownloadInvoiceSuccess() throws ApiException {

        String orderId = "ORD-1";
        FileData fileData = new FileData();

        doNothing().when(orderFlow).checkInvoiceDownloadable(orderId);
        when(invoiceClient.downloadInvoice(orderId)).thenReturn(fileData);

        FileData result = orderDto.downloadInvoice(orderId);

        assertNotNull(result);
        verify(orderFlow).checkInvoiceDownloadable(orderId);
    }

    // ---------- FILTER ORDERS ----------

    @Test
    void testFilterOrdersSuccess() throws ApiException {

        LocalDate startDate = LocalDate.now().minusDays(1);
        LocalDate endDate = LocalDate.now();

        Page<OrderPojo> pojoPage = new PageImpl<>(List.of(new OrderPojo()));

        try (
                MockedStatic<ValidationUtil> validationMock = mockStatic(ValidationUtil.class);
                MockedStatic<OrderHelper> helperMock = mockStatic(OrderHelper.class)
        ) {

            validationMock.when(() -> ValidationUtil.validateDates(startDate, endDate))
                    .thenAnswer(invocation -> null);

            when(orderFlow.filterOrders(
                    any(ZonedDateTime.class),
                    any(ZonedDateTime.class),
                    eq(0),
                    eq(10))
            ).thenReturn(pojoPage);

            helperMock.when(() -> OrderHelper.convertToData(any()))
                    .thenReturn(new OrderData());

            Page<OrderData> result =
                    orderDto.filterOrders(startDate, endDate, 0, 10);

            assertEquals(1, result.getTotalElements());
        }
    }
}
