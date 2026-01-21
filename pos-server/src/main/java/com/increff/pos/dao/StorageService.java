package com.increff.pos.dao;

import java.io.IOException;

public interface StorageService {
    byte[] readInvoice(String orderId) throws IOException;
}

