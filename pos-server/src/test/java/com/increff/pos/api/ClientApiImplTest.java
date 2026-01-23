package com.increff.pos.api;

import com.increff.pos.dao.ClientDao;
import com.increff.pos.db.ClientPojo;
import com.increff.pos.exception.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientApiImplTest {

    @Mock
    private ClientDao clientDao;

    @InjectMocks
    private ClientApiImpl clientApi;

    private ClientPojo client;

    @BeforeEach
    void setup() {
        client = new ClientPojo();
        client.setId("1");
        client.setName("Test Client");
        client.setEmail("test@example.com");
        client.setPhoneNumber("9999999999");
    }

    // ---------- addClient ----------

    @Test
    void addClient_success() throws ApiException {
        when(clientDao.findByName(client.getName())).thenReturn(null);
        when(clientDao.findByEmail(client.getEmail())).thenReturn(null);
        when(clientDao.findByPhoneNumber(client.getPhoneNumber())).thenReturn(null);
        when(clientDao.save(client)).thenReturn(client);

        ClientPojo result = clientApi.addClient(client);

        assertNotNull(result);
        verify(clientDao).save(client);
    }

    @Test
    void addClient_duplicateEmail_throwsException() {
        when(clientDao.findByEmail(client.getEmail())).thenReturn(client);

        ApiException ex = assertThrows(ApiException.class,
                () -> clientApi.addClient(client));

        assertEquals("Email already exists", ex.getMessage());
    }

    // ---------- getAllClients ----------

    @Test
    void getAllClients_returnsPage() {
        Page<ClientPojo> page = new PageImpl<>(List.of(client));
        when(clientDao.findAll((Pageable) any())).thenReturn(page);

        Page<ClientPojo> result = clientApi.getAllClients(0, 10);

        assertEquals(1, result.getTotalElements());
    }

    // ---------- updateClient ----------

    @Test
    void updateClient_success() throws ApiException {
        when(clientDao.findByName("Old Name")).thenReturn(client);
        when(clientDao.findByName(client.getName())).thenReturn(null);
        when(clientDao.findByEmail(client.getEmail())).thenReturn(null);
        when(clientDao.findByPhoneNumber(client.getPhoneNumber())).thenReturn(null);
        when(clientDao.save(any())).thenReturn(client);

        ClientPojo updated = clientApi.updateClient("Old Name", client);

        assertNotNull(updated);
        verify(clientDao).save(any());
    }

    @Test
    void updateClient_clientNotFound() {
        when(clientDao.findByName("Missing")).thenReturn(null);

        ApiException ex = assertThrows(ApiException.class,
                () -> clientApi.updateClient("Missing", client));

        assertEquals("Client doesn't exist", ex.getMessage());
    }

    // ---------- searchClient ----------

    @Test
    void searchClient_returnsResults() throws ApiException {
        when(clientDao.search("Test")).thenReturn(List.of(client));

        List<ClientPojo> result = clientApi.searchClient("Test");

        assertEquals(1, result.size());
    }
}
