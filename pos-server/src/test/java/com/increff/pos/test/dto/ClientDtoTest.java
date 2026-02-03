package com.increff.pos.test.dto;

import com.increff.pos.api.ClientApiImpl;
import com.increff.pos.db.ClientPojo;
import com.increff.pos.dto.ClientDto;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.data.ClientData;
import com.increff.pos.model.form.ClientForm;
import com.increff.pos.model.form.PageForm;
import com.increff.pos.util.FormValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientDtoTest {

    @Mock
    private ClientApiImpl clientApi;

    @Mock
    private FormValidator formValidator;

    @InjectMocks
    private ClientDto clientDto;

    private ClientForm getSampleClientForm() {
        ClientForm form = new ClientForm();
        form.setName("  Test Client  ");
        form.setLocation("vovdedv");
        form.setEmail("asf@gmail.com");
        form.setPhoneNumber("1234567890");
        return form;
    }

    private ClientPojo getSampleClientPojo() {
        ClientPojo pojo = new ClientPojo();
        pojo.setId("scedcde");
        pojo.setName("test client");
        pojo.setLocation("vovdedv");
        pojo.setEmail("asf@gmail.com");
        pojo.setPhoneNumber("1234567890");
        return pojo;
    }

    @Test
    void testCreateClientSuccess() throws ApiException {
        ClientForm form = getSampleClientForm();
        ClientPojo savedPojo = getSampleClientPojo();

        doNothing().when(clientApi).checkNameExists(anyString());
        when(clientApi.addClient(any(ClientPojo.class))).thenReturn(savedPojo);

        ClientData data = clientDto.createClient(form);

        assertNotNull(data);
        assertEquals(savedPojo.getId(), data.getId());
        assertEquals(savedPojo.getName(), data.getName());

        verify(clientApi).checkNameExists("  Test Client  ");
        verify(clientApi).addClient(any(ClientPojo.class));
    }

    @Test
    void testGetAllClientsSuccess() throws ApiException {
        ClientPojo pojo = getSampleClientPojo();
        Page<ClientPojo> pojoPage = new PageImpl<>(List.of(pojo));

        when(clientApi.getAllClients(0, 10)).thenReturn(pojoPage);

        PageForm pageForm = new PageForm();
        pageForm.setPage(0);
        pageForm.setSize(10);

        Page<ClientData> dataPage = clientDto.getAllClients(pageForm);

        assertEquals(1, dataPage.getTotalElements());
        assertEquals("test client", dataPage.getContent().get(0).getName());

        verify(clientApi).getAllClients(0, 10);
    }

    @Test
    void testUpdateClientDetailsSuccess() throws ApiException {
        ClientForm form = getSampleClientForm();
        ClientPojo updatedPojo = getSampleClientPojo();

        when(clientApi.updateClient(any(ClientPojo.class))).thenReturn(updatedPojo);

        ClientData data = clientDto.updateClientDetails(form);

        assertNotNull(data);
        assertEquals(updatedPojo.getId(), data.getId());
        assertEquals(updatedPojo.getName(), data.getName());

        verify(clientApi).updateClient(any(ClientPojo.class));
    }

    @Test
    void testSearchSuccess() throws ApiException {
        ClientPojo pojo = getSampleClientPojo();
        Page<ClientPojo> pojoPage = new PageImpl<>(List.of(pojo));

        when(clientApi.search("name", "test", 0, 5)).thenReturn(pojoPage);

        PageForm pageForm = new PageForm();
        pageForm.setPage(0);
        pageForm.setSize(5);

        Page<ClientData> result =
                clientDto.search("name", "test", pageForm);

        assertEquals(1, result.getTotalElements());
        assertEquals("test client", result.getContent().get(0).getName());

        verify(clientApi).search("name", "test", 0, 5);
    }

    @Test
    void testSearchInvalidParamsThrowsException() {
        PageForm pageForm = new PageForm();
        pageForm.setPage(0);
        pageForm.setSize(5);

        assertThrows(ApiException.class, () ->
                clientDto.search("invalid", "", pageForm)
        );

        verifyNoInteractions(clientApi);
    }
}
