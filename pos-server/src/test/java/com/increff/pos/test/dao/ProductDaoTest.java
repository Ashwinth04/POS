package com.increff.pos.test.dao;

import com.increff.pos.config.TestConfig;
import com.increff.pos.dao.ProductDao;
import com.increff.pos.db.ProductPojo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = TestConfig.class)
@ActiveProfiles("test")
class ProductDaoTest {

    @Autowired
    private MongoOperations mongoOperations;

    @Autowired
    private ProductDao productDao;

    @BeforeEach
    void setUp() {
        mongoOperations.dropCollection(ProductPojo.class);
        seedProducts();
    }

    // ------------------------------------------------
    // SEED DATA
    // ------------------------------------------------

    private void seedProducts() {
        insertProduct("B001", "Apple");
        insertProduct("B002", "Banana");
        insertProduct("B003", "Apricot");
        insertProduct("C001", "Carrot");
    }

    private void insertProduct(String barcode, String name) {
        ProductPojo pojo = new ProductPojo();
        pojo.setBarcode(barcode);
        pojo.setName(name);
        mongoOperations.insert(pojo);
    }

    // ------------------------------------------------
    // findByBarcode()
    // ------------------------------------------------

    @Test
    void testFindByBarcode_found() {
        ProductPojo product = productDao.findByBarcode("B001");

        assertThat(product).isNotNull();
        assertThat(product.getName()).isEqualTo("Apple");
    }

    @Test
    void testFindByBarcode_notFound() {
        ProductPojo product = productDao.findByBarcode("INVALID");
        assertThat(product).isNull();
    }

    // ------------------------------------------------
    // findByBarcodes()
    // ------------------------------------------------

    @Test
    void testFindByBarcodes_success() {
        List<ProductPojo> products =
                productDao.findByBarcodes(List.of("B001", "B003"));

        assertThat(products).hasSize(2);
        assertThat(products)
                .extracting(ProductPojo::getBarcode)
                .containsExactlyInAnyOrder("B001", "B003");
    }

    // ------------------------------------------------
    // searchByBarcode()
    // ------------------------------------------------

    @Test
    void testSearchByBarcode_prefixMatch() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<ProductPojo> page =
                productDao.searchByBarcode("B", pageable);

        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getContent()).hasSize(3);
    }

    @Test
    void testSearchByBarcode_withPagination() {
        Page<ProductPojo> page1 =
                productDao.searchByBarcode("B", PageRequest.of(0, 2));

        Page<ProductPojo> page2 =
                productDao.searchByBarcode("B", PageRequest.of(1, 2));

        assertThat(page1.getContent()).hasSize(2);
        assertThat(page2.getContent()).hasSize(1);
        assertThat(page1.getTotalElements()).isEqualTo(3);
    }

}
