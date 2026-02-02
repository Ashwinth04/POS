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

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientApiImplTest {

    @Mock
    private ClientDao clientDao;

    @InjectMocks
    private ClientApiImpl clientApi;

    // ---------- addClient ----------

    @Test
    void testAddClient_success() throws ApiException {
        ClientPojo client = new ClientPojo();
        client.setName("ABC");

        when(clientDao.save(client)).thenReturn(client);

        ClientPojo result = clientApi.addClient(client);

        assertThat(result).isEqualTo(client);
        verify(clientDao).save(client);
    }

    // ---------- getAllClients ----------

    @Test
    void testGetAllClients_success() {
        Page<ClientPojo> page =
                new PageImpl<>(List.of(new ClientPojo()));

        when(clientDao.findAll(any(Pageable.class)))
                .thenReturn(page);

        Page<ClientPojo> result = clientApi.getAllClients(0, 10);

        assertThat(result.getContent()).hasSize(1);
        verify(clientDao).findAll(any(Pageable.class));
    }

    // ---------- updateClient ----------

    @Test
    void testUpdateClient_success() throws ApiException {
        ClientPojo existing = new ClientPojo();
        existing.setId("1");
        existing.setName("ABC");

        ClientPojo update = new ClientPojo();
        update.setName("ABC");

        when(clientDao.findByName("ABC")).thenReturn(existing);
        when(clientDao.save(update)).thenReturn(update);

        ClientPojo result = clientApi.updateClient(update);

        assertThat(result).isEqualTo(update);
        assertThat(update.getId()).isEqualTo("1");
        verify(clientDao).save(update);
    }

    @Test
    void testUpdateClient_notFound() {
        ClientPojo update = new ClientPojo();
        update.setName("ABC");

        when(clientDao.findByName("ABC")).thenReturn(null);

        assertThatThrownBy(() -> clientApi.updateClient(update))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("doesn't exist");
    }

    // ---------- checkNameExists ----------

    @Test
    void testCheckNameExists_exists() {
        when(clientDao.findByName("ABC"))
                .thenReturn(new ClientPojo());

        assertThatThrownBy(() -> clientApi.checkNameExists("ABC"))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void testCheckNameExists_notExists() throws ApiException {
        when(clientDao.findByName("ABC"))
                .thenReturn(null);

        clientApi.checkNameExists("ABC");

        verify(clientDao).findByName("ABC");
    }

    // ---------- fetchExistingClientNames ----------

    @Test
    void testFetchExistingClientNames_success() {
        ClientPojo c1 = new ClientPojo();
        c1.setName("A");

        ClientPojo c2 = new ClientPojo();
        c2.setName("B");

        when(clientDao.findExistingClientNames(List.of("A", "B", "C")))
                .thenReturn(List.of(c1, c2));

        List<String> result =
                clientApi.fetchExistingClientNames(List.of("A", "B", "C"));

        assertThat(result).containsExactly("A", "B");
    }

    // ---------- search ----------

    @Test
    void testSearch_invalidInput() {
        assertThatThrownBy(() ->
                clientApi.search(null, "abc", 0, 10))
                .isInstanceOf(ApiException.class);

        assertThatThrownBy(() ->
                clientApi.search("name", "   ", 0, 10))
                .isInstanceOf(ApiException.class);
    }

    @Test
    void testSearch_byName() throws ApiException {
        Page<ClientPojo> page = new PageImpl<>(List.of(new ClientPojo()));

        when(clientDao.searchByName(eq("abc"), any(Pageable.class)))
                .thenReturn(page);

        Page<ClientPojo> result =
                clientApi.search("name", "abc", 0, 10);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void testSearch_byEmail() throws ApiException {
        Page<ClientPojo> page = new PageImpl<>(List.of(new ClientPojo()));

        when(clientDao.searchByEmail(eq("abc"), any(Pageable.class)))
                .thenReturn(page);

        Page<ClientPojo> result =
                clientApi.search("email", "abc", 0, 10);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void testSearch_byPhone() throws ApiException {
        Page<ClientPojo> page = new PageImpl<>(List.of(new ClientPojo()));

        when(clientDao.searchByPhoneNumber(eq("123"), any(Pageable.class)))
                .thenReturn(page);

        Page<ClientPojo> result =
                clientApi.search("phone", "123", 0, 10);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void testSearch_invalidType() {
        assertThatThrownBy(() ->
                clientApi.search("invalid", "abc", 0, 10))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Invalid search type");
    }

    // ---------- getCheckByClientName ----------

    @Test
    void testGetCheckByClientName_success() throws ApiException {
        ClientPojo client = new ClientPojo();
        client.setName("ABC");

        when(clientDao.findByName("ABC")).thenReturn(client);

        ClientPojo result =
                clientApi.getCheckByClientName("ABC");

        assertThat(result).isEqualTo(client);
    }

    @Test
    void testGetCheckByClientName_notFound() {
        when(clientDao.findByName("ABC")).thenReturn(null);

        assertThatThrownBy(() ->
                clientApi.getCheckByClientName("ABC"))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("doesn't exist");
    }
}
