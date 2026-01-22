package com.increff.pos.controller;

import com.increff.pos.dto.ClientDto;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.data.ClientData;
import com.increff.pos.model.form.ClientForm;
import com.increff.pos.model.form.PageForm;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Client Management", description = "APIs for managing clients")
@RestController
@RequestMapping("/api/clients")
public class ClientController {

    @Autowired
    private ClientDto clientDto;

    @Operation(summary = "Create a new client")
    @RequestMapping(value = "/add",method = RequestMethod.POST)
    public ClientData createNewClient(@RequestBody ClientForm clientForm) throws ApiException {
        return clientDto.createClient(clientForm);
    }

    @Operation(summary = "Get all clients with pagination")
    @RequestMapping(path = "/get-all-paginated", method = RequestMethod.POST)
    public Page<ClientData> getAllClients(@RequestBody PageForm form) throws ApiException {
        return clientDto.getAllClients(form);
    }

    @Operation(summary = "Update client details")
    @RequestMapping(path = "/update/{oldName}", method = RequestMethod.PUT)
    public ClientData updateClient(@PathVariable String oldName, @RequestBody ClientForm clientForm) throws ApiException {
        return clientDto.updateClientDetails(oldName, clientForm);
    }

    @RequestMapping(path = "/search/{name}", method = RequestMethod.GET)
    public List<ClientData> searchByName(@PathVariable String name) throws ApiException {
        return clientDto.searchClient(name);
    }
}