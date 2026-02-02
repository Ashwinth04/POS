package com.increff.pos.test.dao;

import com.increff.pos.config.TestConfig;
import com.increff.pos.dao.ClientDao;
import com.increff.pos.db.ClientPojo;
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
class ClientDaoTest {

    @Autowired
    private MongoOperations mongoOperations;

    @Autowired
    private ClientDao clientDao;

    @BeforeEach
    void setUp() {
        mongoOperations.dropCollection(ClientPojo.class);
        seedClients();
    }

    // ------------------------------------------------
    // SEED DATA
    // ------------------------------------------------

    private void seedClients() {
        insertClient("Client One", "one@test.com", "1111111111");
        insertClient("Client Two", "two@test.com", "2222222222");
        insertClient("Alpha Client", "alpha@test.com", "3333333333");
        insertClient("Beta Client", "beta@test.com", "4444444444");
    }

    private void insertClient(String name, String email, String phone) {
        ClientPojo client = new ClientPojo();
        client.setName(name);
        client.setEmail(email);
        client.setPhoneNumber(phone);
        mongoOperations.insert(client);
    }

    // ------------------------------------------------
    // findByName()
    // ------------------------------------------------

    @Test
    void testFindByName_found() {
        ClientPojo client = clientDao.findByName("Client One");

        assertThat(client).isNotNull();
        assertThat(client.getEmail()).isEqualTo("one@test.com");
    }

    @Test
    void testFindByName_notFound() {
        ClientPojo client = clientDao.findByName("Unknown");
        assertThat(client).isNull();
    }

    // ------------------------------------------------
    // findExistingClientNames()
    // ------------------------------------------------

    @Test
    void testFindExistingClientNames_success() {
        List<ClientPojo> result =
                clientDao.findExistingClientNames(
                        List.of("Client One", "Alpha Client", "INVALID")
                );

        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(ClientPojo::getName)
                .containsExactlyInAnyOrder("Client One", "Alpha Client");
    }

    @Test
    void testFindExistingClientNames_emptyInput() {
        List<ClientPojo> result =
                clientDao.findExistingClientNames(List.of());

        assertThat(result).isEmpty();
    }

    @Test
    void testFindExistingClientNames_nullInput() {
        List<ClientPojo> result =
                clientDao.findExistingClientNames(null);

        assertThat(result).isEmpty();
    }

    @Test
    void testFindExistingClientNames_projectionOnlyName() {
        List<ClientPojo> result =
                clientDao.findExistingClientNames(List.of("Client One"));

        ClientPojo client = result.get(0);

        assertThat(client.getName()).isEqualTo("Client One");
        assertThat(client.getEmail()).isNull();
        assertThat(client.getPhoneNumber()).isNull();
    }

    // ------------------------------------------------
    // searchByName()
    // ------------------------------------------------

    @Test
    void testSearchByName_caseInsensitive() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<ClientPojo> page =
                clientDao.searchByName("client", pageable);

        assertThat(page.getTotalElements()).isEqualTo(4);
        assertThat(page.getContent()).hasSize(4);
    }

    @Test
    void testSearchByName_withPagination() {
        Page<ClientPojo> page1 =
                clientDao.searchByName("client", PageRequest.of(0, 2));

        Page<ClientPojo> page2 =
                clientDao.searchByName("client", PageRequest.of(1, 2));

        assertThat(page1.getContent()).hasSize(2);
        assertThat(page2.getContent()).hasSize(2);
        assertThat(page1.getTotalElements()).isEqualTo(4);
    }

    // ------------------------------------------------
    // searchByEmail()
    // ------------------------------------------------

    @Test
    void testSearchByEmail_found() {
        Pageable pageable = PageRequest.of(0, 5);

        Page<ClientPojo> page =
                clientDao.searchByEmail("alpha@test.com", pageable);

        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().get(0).getName())
                .isEqualTo("Alpha Client");
    }

    @Test
    void testSearchByEmail_notFound() {
        Pageable pageable = PageRequest.of(0, 5);

        Page<ClientPojo> page =
                clientDao.searchByEmail("missing@test.com", pageable);

        assertThat(page.getTotalElements()).isZero();
        assertThat(page.getContent()).isEmpty();
    }

    // ------------------------------------------------
    // searchByPhoneNumber()
    // ------------------------------------------------

    @Test
    void testSearchByPhoneNumber_found() {
        Pageable pageable = PageRequest.of(0, 5);

        Page<ClientPojo> page =
                clientDao.searchByPhoneNumber("2222222222", pageable);

        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().get(0).getName())
                .isEqualTo("Client Two");
    }

    @Test
    void testSearchByPhoneNumber_notFound() {
        Pageable pageable = PageRequest.of(0, 5);

        Page<ClientPojo> page =
                clientDao.searchByPhoneNumber("9999999999", pageable);

        assertThat(page.getTotalElements()).isZero();
        assertThat(page.getContent()).isEmpty();
    }
}
