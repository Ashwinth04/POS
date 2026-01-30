package com.increff.pos.flow;

import com.increff.pos.api.OrderApiImpl;
import com.increff.pos.api.ProductApi;
import com.increff.pos.api.SalesApiImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SalesFlow {

    @Autowired
    private SalesApiImpl salesApi;

    @Autowired
    private OrderApiImpl orderApi;

    @Autowired
    private ProductApi productApi;
}
