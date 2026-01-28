package com.increff.storage;

import java.io.IOException;

public interface StorageService {
    String readInvoice(String orderId) throws IOException;
}
