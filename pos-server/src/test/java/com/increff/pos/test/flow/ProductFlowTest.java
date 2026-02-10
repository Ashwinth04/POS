package com.increff.pos.test.flow;

import com.increff.pos.api.InventoryApiImpl;
import com.increff.pos.api.ProductApiImpl;
import com.increff.pos.db.documents.InventoryPojo;
import com.increff.pos.db.documents.ProductPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.flow.ProductFlow;
import com.increff.pos.model.constants.ProductSearchType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductFlowTest {

    @Mock
    private ProductApiImpl productApi;

    @Mock
    private InventoryApiImpl inventoryApi;

    @InjectMocks
    private ProductFlow productFlow;

    private ProductPojo product(String id) {
        ProductPojo pojo = new ProductPojo();
        pojo.setId(id);
        pojo.setBarcode("b-" + id);
        pojo.setName("Product-" + id);
        pojo.setMrp(100.0);
        return pojo;
    }

    // ---------- addProduct ----------

    @Test
    void testAddProduct() throws ApiException {
        ProductPojo pojo = product("p1");

        when(productApi.addProduct(pojo)).thenReturn(pojo);
        doNothing().when(inventoryApi).createDummyInventoryRecord("p1");

        ProductPojo result = productFlow.addProduct(pojo);

        assertEquals(pojo, result);
        verify(inventoryApi).createDummyInventoryRecord("p1");
    }

    // ---------- editProduct ----------

    @Test
    void testEditProduct() throws ApiException {
        ProductPojo pojo = product("p1");

        when(productApi.editProduct(pojo)).thenReturn(pojo);

        ProductPojo result = productApi.editProduct(pojo);

        assertEquals(pojo, result);
    }

    // ---------- getAllProducts ----------

    @Test
    void testGetAllProducts() {
        Page<ProductPojo> page =
                new PageImpl<>(List.of(product("p1")));

        when(productApi.getAllProducts(0, 10)).thenReturn(page);

        Page<ProductPojo> result = productApi.getAllProducts(0, 10);

        assertEquals(1, result.getTotalElements());
    }

    // ---------- getInventoryForProducts ----------

    @Test
    void testGetInventoryForProducts() {
        ProductPojo p1 = product("p1");
        ProductPojo p2 = product("p2");

        Page<ProductPojo> page = new PageImpl<>(List.of(p1, p2));

        InventoryPojo inv1 = new InventoryPojo();
        inv1.setProductId("p1");
        inv1.setQuantity(0);

        InventoryPojo inv2 = new InventoryPojo();
        inv2.setProductId("p2");
        inv2.setQuantity(0);

        when(inventoryApi.getInventoryForProductIds(List.of("p1", "p2")))
                .thenReturn(List.of( inv1, inv2));

        Map<String, InventoryPojo> result =
                productFlow.getInventoryForProducts(page);

        assertEquals(2, result.size());
        assertEquals(0, result.get("p1").getQuantity());
    }

    // ---------- addProductsBulk ----------

    @Test
    void testAddProductsBulk() throws ApiException {
        ProductPojo p1 = product("p1");
        ProductPojo p2 = product("p2");

        List<ProductPojo> savedProducts = List.of(p1, p2);

        when(productApi.addProductsBulk(List.of(p1, p2)))
                .thenReturn(savedProducts);

        doNothing().when(inventoryApi)
                .createDummyInventoryRecordsBulk(List.of("p1", "p2"));

        productFlow.addProductsBulk(List.of(p1, p2));

        verify(inventoryApi)
                .createDummyInventoryRecordsBulk(List.of("p1", "p2"));
    }

    // ---------- searchProducts ----------

    @Test
    void testSearchProducts() throws ApiException {
        Page<ProductPojo> page =
                new PageImpl<>(List.of(product("p1")));

        when(productApi.searchProducts(ProductSearchType.NAME, "prod", 0, 10))
                .thenReturn(page);

        Page<ProductPojo> result =
                productFlow.searchProducts(ProductSearchType.NAME, "prod", 0, 10);

        assertEquals(1, result.getTotalElements());
    }
}
