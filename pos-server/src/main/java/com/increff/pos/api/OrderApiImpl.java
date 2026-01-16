package com.increff.pos.api;

import com.increff.pos.dao.InventoryDao;
import com.increff.pos.dao.OrderDao;
import com.increff.pos.dao.ProductDao;
import com.increff.pos.db.InventoryPojo;
import com.increff.pos.db.OrderPojo;
import com.increff.pos.db.ProductPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.data.OrderItem;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class OrderApiImpl {
    private static final Logger logger = LoggerFactory.getLogger(OrderDao.class);

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private ProductDao productDao;

    @Autowired
    private InventoryDao inventoryDao;

    @Transactional(rollbackFor = ApiException.class)
    public OrderPojo add(OrderPojo orderPojo) throws ApiException {
        logger.info("Creating an order");

        orderPojo = createOrderItemIds(orderPojo);

        logger.info("Checking if sufficient quantity of each product exists..");
        List<OrderItem> items = orderPojo.getOrderItems();

        // Iterate through all the items and updateClient the inventory
        for (OrderItem item : items) {
            updateQuantity(item);
        }

        try {
            OrderPojo saved = orderDao.save(orderPojo);
            logger.info("Order created");
            return saved;
        } catch (Exception e) {
            throw e;
        }
    }

    private OrderPojo createOrderItemIds(OrderPojo orderPojo) throws ApiException {

        List<OrderItem> items = orderPojo.getOrderItems();

        for(OrderItem item: items) {
            item.setOrderItemId(UUID.randomUUID().toString());
        }

        orderPojo.setOrderItems(items);

        return orderPojo;
    }

    private void updateQuantity(OrderItem item) throws ApiException {
        String productId = item.getProductId();

        // Check if the product exists in the database
        ProductPojo product = productDao.findById(productId)
                .orElseThrow(() -> new ApiException("Product not found: " + productId));

        int availableQuantity = inventoryDao.getQuantity(productId);
        int requiredQuantity = item.getOrderedQuantity();

        if(requiredQuantity > availableQuantity) {
            throw new ApiException("Required quantity not available in inventory. Available quantity:" + availableQuantity);
        }

        InventoryPojo updatedInventory = new InventoryPojo();
        updatedInventory.setQuantity(availableQuantity - requiredQuantity);
        updatedInventory.setProductId(productId);

        inventoryDao.updateInventory(updatedInventory);

        logger.info("Product updated!!");
    }
}
