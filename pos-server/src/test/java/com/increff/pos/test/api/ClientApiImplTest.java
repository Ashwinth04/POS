package com.increff.pos.test.api;

import com.increff.pos.api.ClientApiImpl;
import com.increff.pos.dao.ClientDao;
import com.increff.pos.db.documents.ClientPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.constants.ClientSearchType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

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
        client.setName("ABC");
        client.setEmail("abc@test.com");
        client.setPhoneNumber("9999999999");
    }

    @Test
    void testAddClient() {
        when(clientDao.save(client)).thenReturn(client);

        ClientPojo result = clientApi.addClient(client);

        assertEquals("ABC", result.getName());
        verify(clientDao).save(client);
    }

    @Test
    void testGetAllClients() {
        Page<ClientPojo> page =
                new PageImpl<>(List.of(client));

        when(clientDao.findAll(any(Pageable.class))).thenReturn(page);

        Page<ClientPojo> result = clientApi.getAllClients(0, 10);

        assertEquals(1, result.getContent().size());
    }

    @Test
    void testUpdateClient() throws ApiException {
        when(clientDao.findByName("ABC")).thenReturn(client);
        when(clientDao.save(any())).thenReturn(client);

        ClientPojo updated = new ClientPojo();
        updated.setName("ABC");

        ClientPojo result = clientApi.updateClient(updated);

        assertEquals("1", result.getId());
        verify(clientDao).save(updated);
    }

    @Test
    void testCheckNameExistsThrows() {
        when(clientDao.findByName("ABC")).thenReturn(client);

        assertThrows(ApiException.class,
                () -> clientApi.checkNameExists("ABC"));
    }

    @Test
    void testSearchByName() throws ApiException {
        Page<ClientPojo> page =
                new PageImpl<>(List.of(client));

        when(clientDao.searchByName(eq("ABC"), any(Pageable.class)))
                .thenReturn(page);

        Page<ClientPojo> result =
                clientApi.search(ClientSearchType.NAME, "ABC", 0, 10);

        assertEquals(1, result.getContent().size());
    }

    @Test
    void testGetCheckByClientNameNotFound() {
        when(clientDao.findByName("XYZ")).thenReturn(null);

        assertThrows(ApiException.class,
                () -> clientApi.getCheckByClientName("XYZ"));
    }
}
