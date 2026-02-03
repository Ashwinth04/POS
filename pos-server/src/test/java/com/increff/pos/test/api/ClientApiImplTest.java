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
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientApiImplTest {

    @Mock
    private ClientDao clientDao;

    @InjectMocks
    private ClientApiImpl clientApi;

    private ClientPojo createClient(String id, String name) {
        ClientPojo pojo = new ClientPojo();
        pojo.setId(id);
        pojo.setName(name);
        pojo.setEmail(name + "@test.com");
        pojo.setPhoneNumber("9999999999");
        pojo.setCreatedAt(ZonedDateTime.now());
        return pojo;
    }

    // ---------- addClient ----------

    @Test
    void testAddClient() throws ApiException {
        ClientPojo pojo = createClient(null, "Client1");
        when(clientDao.save(pojo)).thenReturn(pojo);

        ClientPojo result = clientApi.addClient(pojo);

        assertEquals(pojo, result);
        verify(clientDao).save(pojo);
    }

    // ---------- getAllClients ----------

    @Test
    void testGetAllClients() {
        ClientPojo pojo = createClient("101", "Client1");
        Page<ClientPojo> page = new PageImpl<>(List.of(pojo));

        when(clientDao.findAll(any(Pageable.class))).thenReturn(page);

        Page<ClientPojo> result = clientApi.getAllClients(0, 10);

        assertEquals(1, result.getTotalElements());
        verify(clientDao).findAll(any(PageRequest.class));
    }

    // ---------- updateClient ----------

    @Test
    void testUpdateClient() throws ApiException {
        ClientPojo existing = createClient("101", "Client1");
        ClientPojo updated = createClient(null, "Client1");

        when(clientDao.findByName("Client1")).thenReturn(existing);
        when(clientDao.save(any(ClientPojo.class))).thenAnswer(i -> i.getArgument(0));

        ClientPojo result = clientApi.updateClient(updated);

        assertEquals(existing.getId(), result.getId());
        verify(clientDao).save(updated);
    }

    // ---------- checkNameExists ----------

    @Test
    void testCheckNameExistsThrowsException() {
        when(clientDao.findByName("Client1"))
                .thenReturn(createClient("101", "Client1"));

        ApiException ex = assertThrows(ApiException.class,
                () -> clientApi.checkNameExists("Client1"));

        assertEquals("Client already exists", ex.getMessage());
    }

    @Test
    void testCheckNameExistsSuccess() throws ApiException {
        when(clientDao.findByName("Client1")).thenReturn(null);

        assertDoesNotThrow(() -> clientApi.checkNameExists("Client1"));
    }

    // ---------- fetchExistingClientNames ----------

    @Test
    void testFetchExistingClientNames() {
        ClientPojo c1 = createClient("101", "A");
        ClientPojo c2 = createClient("102", "B");

        when(clientDao.findExistingClientNames(List.of("A", "B")))
                .thenReturn(List.of(c1, c2));

        Map<String, ClientPojo> result =
                clientApi.fetchExistingClientNames(List.of("A", "B"));

        assertEquals(2, result.size());
        assertEquals(c1, result.get("A"));
        assertEquals(c2, result.get("B"));
    }

    // ---------- search ----------

    @Test
    void testSearchByName() throws ApiException {
        Page<ClientPojo> page = new PageImpl<>(List.of(createClient("101", "A")));
        when(clientDao.searchByName(eq("A"), any(Pageable.class))).thenReturn(page);

        Page<ClientPojo> result = clientApi.search("name", "A", 0, 10);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void testSearchByEmail() throws ApiException {
        Page<ClientPojo> page = new PageImpl<>(List.of(createClient("101", "A")));
        when(clientDao.searchByEmail(eq("a@test.com"), any(Pageable.class))).thenReturn(page);

        Page<ClientPojo> result = clientApi.search("email", "a@test.com", 0, 10);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void testSearchByPhone() throws ApiException {
        Page<ClientPojo> page = new PageImpl<>(List.of(createClient("101", "A")));
        when(clientDao.searchByPhoneNumber(eq("999"), any(Pageable.class))).thenReturn(page);

        Page<ClientPojo> result = clientApi.search("phone", "999", 0, 10);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void testSearchInvalidType() {
        ApiException ex = assertThrows(ApiException.class,
                () -> clientApi.search("invalid", "x", 0, 10));

        assertTrue(ex.getMessage().contains("Invalid search type"));
    }

    @Test
    void testSearchNullOrBlankQuery() {
        assertThrows(ApiException.class,
                () -> clientApi.search(null, "x", 0, 10));

        assertThrows(ApiException.class,
                () -> clientApi.search("name", "   ", 0, 10));
    }

    // ---------- getCheckByClientName ----------

    @Test
    void testGetCheckByClientNameSuccess() throws ApiException {
        ClientPojo pojo = createClient("101", "Client1");
        when(clientDao.findByName("Client1")).thenReturn(pojo);

        ClientPojo result = clientApi.getCheckByClientName("Client1");

        assertEquals(pojo, result);
    }

    @Test
    void testGetCheckByClientNameThrowsException() {
        when(clientDao.findByName("Client1")).thenReturn(null);

        ApiException ex = assertThrows(ApiException.class,
                () -> clientApi.getCheckByClientName("Client1"));

        assertTrue(ex.getMessage().contains("doesn't exist"));
    }
}
