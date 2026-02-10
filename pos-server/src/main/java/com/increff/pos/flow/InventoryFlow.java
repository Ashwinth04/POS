package com.increff.pos.flow;

import com.increff.pos.api.InventoryApiImpl;
import com.increff.pos.api.ProductApiImpl;
import com.increff.pos.db.documents.InventoryPojo;
import com.increff.pos.db.documents.ProductPojo;
import com.increff.pos.exception.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InventoryFlow {

    @Autowired
    private ProductApiImpl productApi;

    public ProductPojo getCheckByBarcode(String barcode) throws ApiException {
        return productApi.getCheckByBarcode(barcode);
    }

    public List<ProductPojo> getProductPojosForBarcodes(List<String> barcodes) {
        return productApi.getProductPojosForBarcodes(barcodes);
    }
}
