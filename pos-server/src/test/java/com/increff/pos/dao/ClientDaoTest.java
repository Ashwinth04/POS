package com.increff.pos.dao;

import com.increff.pos.config.SpringConfig;
import com.increff.pos.db.ClientPojo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest
@ContextConfiguration(classes = SpringConfig.class)
@ImportAutoConfiguration(exclude = {
        org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class
})
class ClientDaoTest {

    @Autowired
    private ClientDao clientDao;

    @BeforeEach
    void setup() {
        clientDao.deleteAll();

        ClientPojo c1 = new ClientPojo();
        c1.setName("Apple");
        c1.setEmail("apple@test.com");
        c1.setPhoneNumber("111");

        ClientPojo c2 = new ClientPojo();
        c2.setName("Amazon");
        c2.setEmail("amazon@test.com");
        c2.setPhoneNumber("222");

        ClientPojo c3 = new ClientPojo();
        c3.setName("Google");
        c3.setEmail("google@test.com");
        c3.setPhoneNumber("333");

        clientDao.save(c1);
        clientDao.save(c2);
        clientDao.save(c3);
    }

    @Test
    void findByName_returnsCorrectClient() {
        ClientPojo client = clientDao.findByName("Apple");

        assertNotNull(client);
        assertEquals("apple@test.com", client.getEmail());
    }

    @Test
    void findByEmail_returnsCorrectClient() {
        ClientPojo client = clientDao.findByEmail("amazon@test.com");

        assertNotNull(client);
        assertEquals("Amazon", client.getName());
    }

    @Test
    void findByPhoneNumber_returnsCorrectClient() {
        ClientPojo client = clientDao.findByPhoneNumber("333");

        assertNotNull(client);
        assertEquals("Google", client.getName());
    }

    @Test
    void findExistingClientNames_returnsOnlyMatches() {
        List<String> result = clientDao.findExistingClientNames(
                List.of("Apple", "Google", "Netflix")
        );

        assertEquals(2, result.size());
        assertTrue(result.contains("Apple"));
        assertTrue(result.contains("Google"));
    }

    @Test
    void findExistingClientNames_emptyInput_returnsEmpty() {
        List<String> result = clientDao.findExistingClientNames(List.of());
        assertTrue(result.isEmpty());
    }

    @Test
    void search_byName_prefix_caseInsensitive() {
        List<ClientPojo> result = clientDao.search("a");

        assertEquals(2, result.size()); // Apple + Amazon
        List<String> names = result.stream().map(ClientPojo::getName).toList();
        assertTrue(names.contains("Apple"));
        assertTrue(names.contains("Amazon"));
    }

    @Test
    void searchByEmail_prefix_caseInsensitive() {
        List<ClientPojo> result = clientDao.searchByEmail("goo");

        assertEquals(1, result.size());
        assertEquals("google@test.com", result.get(0).getEmail());
    }
}
