package com.increff.pos.controller;

import com.increff.pos.dto.InventoryDto;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.data.FileData;
import com.increff.pos.model.data.InventoryData;
import com.increff.pos.model.form.FileForm;
import com.increff.pos.model.form.InventoryForm;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Inventory Management", description = "Manage inventories for products")
@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    @Autowired
    private InventoryDto inventoryDto;

    @Operation(summary = "Update inventory for a product")
    @RequestMapping(path = "/update", method = RequestMethod.PUT)
    public InventoryData updateInventory(@RequestBody InventoryForm inventoryForm) throws ApiException {
        return inventoryDto.updateInventory(inventoryForm);
    }

    @Operation(summary = "Update inventory for multiple products at once")
    @RequestMapping(path = "/bulk-update", method = RequestMethod.POST)
    public FileData updateBulkInventory(@RequestBody FileForm fileForm) throws ApiException {
        return inventoryDto.updateInventoryBulk(fileForm);
    }

}
