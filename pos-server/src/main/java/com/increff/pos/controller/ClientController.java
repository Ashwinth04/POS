package com.increff.pos.controller;

import ch.qos.logback.core.net.server.Client;
import com.increff.pos.dto.ClientDto;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.data.ClientData;
import com.increff.pos.model.form.ClientForm;
import com.increff.pos.model.form.PageForm;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.coyote.Request;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

//@Tag(name = "Client Management", description = "APIs for managing clients")
@RestController
@RequestMapping("/api/clients")
public class ClientController {

    @Autowired
    private ClientDto clientDto;

    @Operation(summary = "Create a new client")
    @RequestMapping(value = "/add",method = RequestMethod.POST)
    public ClientData create(@RequestBody ClientForm clientForm) throws ApiException {
        return clientDto.create(clientForm);
    }

    @Operation(summary = "Get all clients with pagination")
    @RequestMapping(path = "/get-all-paginated", method = RequestMethod.POST)
    public Page<ClientData> getAll(@RequestBody PageForm form) throws ApiException {
        return clientDto.getAll(form);
    }

    @Operation(summary = "Update client name")
    @RequestMapping(path = "/update/{oldName}", method = RequestMethod.PUT)
    public ClientData update(@PathVariable String oldName, @RequestBody ClientForm clientForm) throws ApiException {
        return clientDto.update(oldName, clientForm);
    }
}