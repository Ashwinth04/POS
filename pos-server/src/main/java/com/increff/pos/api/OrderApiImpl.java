package com.increff.pos.api;

import com.increff.pos.dao.OrderDao;
import com.increff.pos.storage.StorageService;
import com.increff.pos.db.OrderPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.data.OrderStatus;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.*;

@Service
public class OrderApiImpl implements OrderApi {
    private static final Logger logger = LoggerFactory.getLogger(OrderApiImpl.class);

    private final OrderDao orderDao;

    private final StorageService storageService;

    public OrderApiImpl(OrderDao orderDao, StorageService storageService) {
        this.orderDao = orderDao;
        this.storageService = storageService;
    }

    @Transactional(rollbackFor = ApiException.class)
    public Map<String, OrderStatus> placeOrder(OrderPojo orderPojo, Map<String, OrderStatus> statuses, boolean isFulFillable) throws ApiException {

        orderPojo.setOrderStatus(isFulFillable ? "FULFILLABLE" : "UNFULFILLABLE");

        orderDao.save(orderPojo);

        return statuses;
    }

    public byte[] getInvoice(String orderId) throws ApiException {

        try {
            return storageService.readInvoice(orderId);
        } catch (IOException e) {
            throw new ApiException("Failed to fetch invoice for order: " + orderId);
        }

    }

    public Page<OrderPojo> getAllOrders(int page, int size) {
        logger.info("Fetching orders page {} with size {}", page, size);
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return orderDao.findAll(pageRequest);
    }
}
