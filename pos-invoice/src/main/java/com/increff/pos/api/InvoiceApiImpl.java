package com.increff.pos.api;

import com.increff.pos.dao.OrderDao;
import com.increff.pos.db.OrderPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.helper.InvoiceHelper;
import com.increff.pos.model.data.FileData;
import com.increff.pos.model.data.OrderData;
import com.increff.pos.service.InvoiceGenerator;
import com.increff.pos.storage.StorageService;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class InvoiceApiImpl {

    private final OrderDao orderDao;
    private final StorageService storageService;

    public InvoiceApiImpl(OrderDao orderDao, StorageService storageService) {
        this.orderDao = orderDao;
        this.storageService = storageService;
    }

    public FileData generateInvoice(OrderData orderData) throws ApiException {

        String base64String = InvoiceGenerator.generate(orderData);

        FileData fileData = new FileData();
        fileData.setStatus("SUCCESS");
        fileData.setBase64file(base64String);

        return fileData;
    }

    private void changeOrderStatus(String orderId) throws ApiException {
        OrderPojo orderPojo = orderDao.findByOrderId(orderId);
        if (orderPojo == null) throw new ApiException("Order with the given id doesn't exist");

        orderPojo.setOrderStatus("PLACED");
        orderDao.save(orderPojo);
    }

    public FileData downloadInvoice(String orderId) throws ApiException, IOException {

        OrderPojo orderPojo = orderDao.findByOrderId(orderId);

        if (orderPojo == null) throw new ApiException("ORDER WITH THE GIVEN ID DOESN'T EXIST");

        if (orderPojo.getOrderStatus().equals("CANCELLED")) throw new ApiException("ORDER CANCELLED ALREADY. PLEASE CREATE A NEW ORDER");

        if (!orderPojo.getOrderStatus().equals("PLACED")) throw new ApiException("ORDER NOT PLACED YET. INVOICE CANNOT BE GENERATED");

        String base64String = storageService.readInvoice(orderId);
        FileData fileData = new FileData();
        fileData.setBase64file(base64String);

        return fileData;
    }
}
