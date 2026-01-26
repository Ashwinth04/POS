package com.increff.pos.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.increff.pos.config.SpringConfig;
import com.increff.pos.dto.ClientDto;
import com.increff.pos.model.data.ClientData;
import com.increff.pos.model.form.ClientForm;
import com.increff.pos.model.form.PageForm;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ClientController.class)
@ContextConfiguration(classes = SpringConfig.class)
class ClientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClientDto clientDto;

    @Autowired
    private ObjectMapper objectMapper;

    // ---------- CREATE CLIENT ----------

    @Test
    void shouldCreateClient() throws Exception {
        ClientData response = buildClientData("Ashwin");

        Mockito.when(clientDto.createClient(any()))
                .thenReturn(response);

        ClientForm form = buildClientForm("Ashwin");

        mockMvc.perform(post("/api/clients/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(form)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Ashwin"))
                .andExpect(jsonPath("$.email").value("ashwin@test.com"));
    }

    // ---------- GET PAGINATED ----------

    @Test
    void shouldGetPaginatedClients() throws Exception {
        ClientData c1 = buildClientData("Ashwin");
        ClientData c2 = buildClientData("Rahul");

        Page<ClientData> page = new PageImpl<>(List.of(c1, c2));

        Mockito.when(clientDto.getAllClients(any()))
                .thenReturn(page);

        PageForm form = new PageForm();
        form.setPage(0);
        form.setSize(10);

        mockMvc.perform(post("/api/clients/get-all-paginated")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(form)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].name").value("Ashwin"));
    }

    // ---------- UPDATE ----------

    @Test
    void shouldUpdateClient() throws Exception {
        ClientData updated = buildClientData("NewName");

        Mockito.when(clientDto.updateClientDetails(eq("OldName"), any()))
                .thenReturn(updated);

        ClientForm form = buildClientForm("NewName");

        mockMvc.perform(put("/api/clients/update/OldName")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(form)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("NewName"));
    }

    // ---------- SEARCH NAME ----------

    @Test
    void shouldSearchByName() throws Exception {
        Mockito.when(clientDto.searchClient("Ash"))
                .thenReturn(List.of(buildClientData("Ashwin"), buildClientData("Asha")));

        mockMvc.perform(get("/api/clients/search/name/Ash"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    // ---------- SEARCH EMAIL ----------

    @Test
    void shouldSearchByEmail() throws Exception {
        Mockito.when(clientDto.searchClientByEmail("ash"))
                .thenReturn(List.of(buildClientData("Ashwin")));

        mockMvc.perform(get("/api/clients/search/email/ash"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    // ---------- HELPERS ----------

    private ClientForm buildClientForm(String name) {
        ClientForm f = new ClientForm();
        f.setName(name);
        f.setEmail(name.toLowerCase() + "@test.com");
        f.setPhoneNumber("9999999999");
        return f;
    }

    private ClientData buildClientData(String name) {
        ClientData d = new ClientData();
        d.setName(name);
        d.setEmail(name.toLowerCase() + "@test.com");
        d.setPhoneNumber("9999999999");
        return d;
    }
}
