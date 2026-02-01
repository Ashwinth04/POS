package com.increff.pos.test.api;

import com.increff.pos.api.ClientApiImpl;
import com.increff.pos.dao.ClientDao;
import com.increff.pos.db.ClientPojo;
import com.increff.pos.exception.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ClientApiImplTest {

    @InjectMocks
    private ClientApiImpl clientApi;

    @Mock
    private ClientDao clientDao;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    // ---------- addClient ----------

    @Test
    void shouldAddClient() throws ApiException {
        ClientPojo pojo = new ClientPojo();
        pojo.setName("client1");

        when(clientDao.save(pojo)).thenReturn(pojo);

        ClientPojo saved = clientApi.addClient(pojo);

        assertNotNull(saved);
        verify(clientDao).save(pojo);
    }

    // ---------- getAllClients ----------

    @Test
    void shouldGetAllClients() {
        ClientPojo c1 = new ClientPojo();
        Page<ClientPojo> page =
                new PageImpl<>(List.of(c1), PageRequest.of(0, 10), 1);

        when(clientDao.findAll(any(PageRequest.class))).thenReturn(page);

        Page<ClientPojo> result = clientApi.getAllClients(0, 10);

        assertEquals(1, result.getContent().size());
        verify(clientDao).findAll(any(PageRequest.class));
    }

    // ---------- updateClient ----------

    @Test
    void shouldUpdateClient() throws ApiException {
        ClientPojo existing = new ClientPojo();
        existing.setId("id1");
        existing.setName("client1");

        ClientPojo updated = new ClientPojo();
        updated.setName("client1");

        when(clientDao.findByName("client1")).thenReturn(existing);
        when(clientDao.save(any(ClientPojo.class))).thenAnswer(i -> i.getArgument(0));

        ClientPojo result = clientApi.updateClient(updated);

        assertEquals("id1", result.getId());
        verify(clientDao).findByName("client1");
        verify(clientDao).save(updated);
    }

    @Test
    void shouldThrowExceptionIfClientNotFoundWhileUpdate() {
        ClientPojo pojo = new ClientPojo();
        pojo.setName("missing");

        when(clientDao.findByName("missing")).thenReturn(null);

        ApiException ex = assertThrows(ApiException.class,
                () -> clientApi.updateClient(pojo));

        assertEquals("Client with the given name doesn't exist", ex.getMessage());
    }

    // ---------- checkNameExists ----------

    @Test
    void shouldThrowExceptionIfNameExists() {
        when(clientDao.findByName("client1")).thenReturn(new ClientPojo());

        ApiException ex = assertThrows(ApiException.class,
                () -> clientApi.checkNameExists("client1"));

        assertEquals("Client already exists", ex.getMessage());
    }

    @Test
    void shouldPassIfNameDoesNotExist() throws ApiException {
        when(clientDao.findByName("client1")).thenReturn(null);

        clientApi.checkNameExists("client1");

        verify(clientDao).findByName("client1");
    }

    // ---------- fetchExistingClientNames ----------

    @Test
    void shouldFetchExistingClientNames() {
        ClientPojo c1 = new ClientPojo();
        c1.setName("c1");

        ClientPojo c2 = new ClientPojo();
        c2.setName("c2");

        when(clientDao.findExistingClientNames(List.of("c1", "c2", "c3")))
                .thenReturn(List.of(c1, c2));

        List<String> result =
                clientApi.fetchExistingClientNames(List.of("c1", "c2", "c3"));

        assertEquals(List.of("c1", "c2"), result);
    }

    // ---------- search ----------

    @Test
    void shouldSearchByName() throws ApiException {
        Page<ClientPojo> page = new PageImpl<>(List.of(new ClientPojo()));

        when(clientDao.searchByName(eq("abc"), any()))
                .thenReturn(page);

        Page<ClientPojo> result =
                clientApi.search("name", "abc", 0, 10);

        assertEquals(1, result.getContent().size());
        verify(clientDao).searchByName(eq("abc"), any());
    }

    @Test
    void shouldSearchByEmail() throws ApiException {
        when(clientDao.searchByEmail(eq("a@b.com"), any()))
                .thenReturn(Page.empty());

        clientApi.search("email", "a@b.com", 0, 10);

        verify(clientDao).searchByEmail(eq("a@b.com"), any());
    }

    @Test
    void shouldSearchByPhone() throws ApiException {
        when(clientDao.searchByPhoneNumber(eq("999"), any()))
                .thenReturn(Page.empty());

        clientApi.search("phone", "999", 0, 10);

        verify(clientDao).searchByPhoneNumber(eq("999"), any());
    }

    @Test
    void shouldThrowExceptionForInvalidSearchType() {
        ApiException ex = assertThrows(ApiException.class,
                () -> clientApi.search("invalid", "x", 0, 10));

        assertEquals("Invalid search type: invalid", ex.getMessage());
    }

    @Test
    void shouldThrowExceptionForBlankSearchQuery() {
        ApiException ex = assertThrows(ApiException.class,
                () -> clientApi.search("name", "   ", 0, 10));

        assertEquals("Search type and query must be provided", ex.getMessage());
    }

    // ---------- getCheckByClientName ----------

    @Test
    void shouldGetClientByName() throws ApiException {
        ClientPojo pojo = new ClientPojo();
        pojo.setName("client1");

        when(clientDao.findByName("client1")).thenReturn(pojo);

        ClientPojo result = clientApi.getCheckByClientName("client1");

        assertNotNull(result);
        verify(clientDao).findByName("client1");
    }

    @Test
    void shouldThrowExceptionIfClientNotFound() {
        when(clientDao.findByName("missing")).thenReturn(null);

        ApiException ex = assertThrows(ApiException.class,
                () -> clientApi.getCheckByClientName("missing"));

        assertEquals("Client with the given name doesn't exist", ex.getMessage());
    }
}
