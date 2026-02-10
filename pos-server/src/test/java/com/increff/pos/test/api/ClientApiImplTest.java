package com.increff.pos.test.api;

import com.increff.pos.api.ClientApiImpl;
import com.increff.pos.dao.ClientDao;
import com.increff.pos.db.documents.ClientPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.constants.ClientSearchType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;

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
    void testAddClient() {
        ClientPojo pojo = new ClientPojo();
        when(clientDao.save(pojo)).thenReturn(pojo);

        ClientPojo result = clientApi.addClient(pojo);

        assertEquals(pojo, result);
        verify(clientDao).save(pojo);
    }

    // ---------- getAllClients ----------
    @Test
    void testGetAllClients() {
        Page<ClientPojo> page = new PageImpl<>(List.of(new ClientPojo()));
        when(clientDao.findAll(any(Pageable.class))).thenReturn(page);

        Page<ClientPojo> result = clientApi.getAllClients(0, 10);

        assertEquals(1, result.getContent().size());
        verify(clientDao).findAll(any(Pageable.class));
    }

    // ---------- updateClient ----------
    @Test
    void testUpdateClient() throws Exception {
        ClientPojo existing = new ClientPojo();
        existing.setName("test");

        ClientPojo update = new ClientPojo();
        update.setName("test");
        update.setEmail("new@email.com");
        update.setLocation("blr");
        update.setPhoneNumber("123");

        when(clientDao.findByName("test")).thenReturn(existing);
        when(clientDao.save(existing)).thenReturn(existing);

        ClientPojo result = clientApi.updateClient(update);

        assertEquals("new@email.com", existing.getEmail());
        assertEquals("blr", existing.getLocation());
        assertEquals("123", existing.getPhoneNumber());
        assertEquals(existing, result);
    }

    // ---------- checkNameExists ----------
    @Test
    void testCheckNameExistsThrows() {
        when(clientDao.findByName("abc")).thenReturn(new ClientPojo());

        assertThrows(ApiException.class,
                () -> clientApi.checkNameExists("abc"));
    }

    @Test
    void testCheckNameExistsPass() throws Exception {
        when(clientDao.findByName("abc")).thenReturn(null);

        clientApi.checkNameExists("abc");

        verify(clientDao).findByName("abc");
    }

    // ---------- fetchExistingClientNames ----------
    @Test
    void testFetchExistingClientNames() {
        List<ClientPojo> list = List.of(new ClientPojo());
        when(clientDao.findExistingClientNames(anyList())).thenReturn(list);

        List<ClientPojo> result =
                clientApi.fetchExistingClientNames(List.of("A"));

        assertEquals(1, result.size());
    }

    // ---------- search ----------
    @Test
    void testSearchByName() throws Exception {
        Page<ClientPojo> page = new PageImpl<>(List.of(new ClientPojo()));
        when(clientDao.searchByName(eq("a"), any())).thenReturn(page);

        Page<ClientPojo> result =
                clientApi.search(ClientSearchType.NAME, "a", 0, 10);

        assertEquals(page, result);
    }

    @Test
    void testSearchByEmail() throws Exception {
        Page<ClientPojo> page = new PageImpl<>(List.of(new ClientPojo()));
        when(clientDao.searchByEmail(eq("a"), any())).thenReturn(page);

        Page<ClientPojo> result =
                clientApi.search(ClientSearchType.EMAIL, "a", 0, 10);

        assertEquals(page, result);
    }

    @Test
    void testSearchByPhone() throws Exception {
        Page<ClientPojo> page = new PageImpl<>(List.of(new ClientPojo()));
        when(clientDao.searchByPhoneNumber(eq("a"), any())).thenReturn(page);

        Page<ClientPojo> result =
                clientApi.search(ClientSearchType.PHONE, "a", 0, 10);

        assertEquals(page, result);
    }

    // ---------- getCheckByClientName ----------
    @Test
    void testGetCheckByClientNameSuccess() throws Exception {
        ClientPojo pojo = new ClientPojo();
        when(clientDao.findByName("abc")).thenReturn(pojo);

        ClientPojo result = clientApi.getCheckByClientName("abc");

        assertEquals(pojo, result);
    }

    @Test
    void testGetCheckByClientNameThrows() {
        when(clientDao.findByName("abc")).thenReturn(null);

        assertThrows(ApiException.class,
                () -> clientApi.getCheckByClientName("abc"));
    }
}
