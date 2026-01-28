package com.increff.pos.storage;

import java.io.IOException;

public interface StorageService {
    byte[] readInvoice(String orderId) throws IOException;
}