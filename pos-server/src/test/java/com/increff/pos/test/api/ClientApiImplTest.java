package com.increff.pos.test.api;

import com.increff.pos.api.ClientApiImpl;
import com.increff.pos.dao.ClientDao;
import com.increff.pos.db.ClientPojo;
import com.increff.pos.exception.ApiException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientApiImplTest {

    @Mock
    private ClientDao clientDao;

    @InjectMocks
    private ClientApiImpl clientApi;

    // ---------- addClient ----------

    @Test
    void addClient_success() {
        ClientPojo client = new ClientPojo();
        client.setName("Amazon");

        when(clientDao.save(client)).thenReturn(client);

        ClientPojo result = clientApi.addClient(client);

        assertEquals("Amazon", result.getName());
        verify(clientDao).save(client);
    }

    // ---------- getAllClients ----------

    @Test
    void getAllClients_success() {
        Page<ClientPojo> page = new PageImpl<>(List.of(new ClientPojo()));
        when(clientDao.findAll(any(Pageable.class))).thenReturn(page);

        Page<ClientPojo> result = clientApi.getAllClients(0, 10);

        assertEquals(1, result.getContent().size());
        verify(clientDao).findAll(any(Pageable.class));
    }

    // ---------- updateClient ----------

    @Test
    void updateClient_success() throws ApiException {
        ClientPojo existing = new ClientPojo();
        existing.setId("1");
        existing.setName("Flipkart");

        ClientPojo updated = new ClientPojo();
        updated.setName("Flipkart");

        when(clientDao.findByName("Flipkart")).thenReturn(existing);
        when(clientDao.save(any(ClientPojo.class))).thenAnswer(i -> i.getArgument(0));

        ClientPojo result = clientApi.updateClient(updated);

        assertEquals("1", result.getId());
        verify(clientDao).save(updated);
    }

    @Test
    void updateClient_clientNotFound() {
        ClientPojo client = new ClientPojo();
        client.setName("NonExisting");

        when(clientDao.findByName("NonExisting")).thenReturn(null);

        ApiException ex = assertThrows(ApiException.class,
                () -> clientApi.updateClient(client));

        assertEquals("Client with the given name doesn't exist", ex.getMessage());
    }

    // ---------- checkNameExists ----------

    @Test
    void checkNameExists_throwsException() {
        ClientPojo existing = new ClientPojo();
        existing.setName("Google");

        when(clientDao.findByName("Google")).thenReturn(existing);

        ApiException ex = assertThrows(ApiException.class,
                () -> clientApi.checkNameExists("Google"));

        assertEquals("Client already exists", ex.getMessage());
    }

    @Test
    void checkNameExists_success() throws ApiException {
        when(clientDao.findByName("NewClient")).thenReturn(null);

        assertDoesNotThrow(() -> clientApi.checkNameExists("NewClient"));
    }

    // ---------- fetchExistingClientNames ----------

    @Test
    void fetchExistingClientNames_success() {
        ClientPojo c1 = new ClientPojo();
        c1.setName("A");

        ClientPojo c2 = new ClientPojo();
        c2.setName("B");

        when(clientDao.findExistingClientNames(List.of("A", "B")))
                .thenReturn(List.of(c1, c2));

        Map<String, ClientPojo> result =
                clientApi.fetchExistingClientNames(List.of("A", "B"));

        assertEquals(2, result.size());
        assertTrue(result.containsKey("A"));
        assertTrue(result.containsKey("B"));
    }

    // ---------- search ----------

    @Test
    void search_byName() throws ApiException {
        Page<ClientPojo> page = new PageImpl<>(List.of(new ClientPojo()));
        when(clientDao.searchByName(eq("abc"), any(Pageable.class)))
                .thenReturn(page);

        Page<ClientPojo> result = clientApi.search("name", "abc", 0, 10);

        assertEquals(1, result.getContent().size());
    }

    @Test
    void search_byEmail() throws ApiException {
        Page<ClientPojo> page = new PageImpl<>(List.of(new ClientPojo()));
        when(clientDao.searchByEmail(eq("abc"), any(Pageable.class)))
                .thenReturn(page);

        Page<ClientPojo> result = clientApi.search("email", "abc", 0, 10);

        assertEquals(1, result.getContent().size());
    }

    @Test
    void search_byPhone() throws ApiException {
        Page<ClientPojo> page = new PageImpl<>(List.of(new ClientPojo()));
        when(clientDao.searchByPhoneNumber(eq("123"), any(Pageable.class)))
                .thenReturn(page);

        Page<ClientPojo> result = clientApi.search("phone", "123", 0, 10);

        assertEquals(1, result.getContent().size());
    }

    @Test
    void search_invalidType() {
        ApiException ex = assertThrows(ApiException.class,
                () -> clientApi.search("invalid", "x", 0, 10));

        assertEquals("Invalid search type: invalid", ex.getMessage());
    }

    // ---------- getCheckByClientName ----------

    @Test
    void getCheckByClientName_success() throws ApiException {
        ClientPojo client = new ClientPojo();
        client.setName("Netflix");

        when(clientDao.findByName("Netflix")).thenReturn(client);

        ClientPojo result = clientApi.getCheckByClientName("Netflix");

        assertEquals("Netflix", result.getName());
    }

    @Test
    void getCheckByClientName_notFound() {
        when(clientDao.findByName("Missing")).thenReturn(null);

        ApiException ex = assertThrows(ApiException.class,
                () -> clientApi.getCheckByClientName("Missing"));

        assertEquals("Client with the given name doesn't exist", ex.getMessage());
    }
}
