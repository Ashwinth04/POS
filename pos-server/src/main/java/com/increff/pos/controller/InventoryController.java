package com.increff.pos.controller;

import com.increff.pos.dto.InventoryDto;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.data.FileData;
import com.increff.pos.model.data.InventoryData;
import com.increff.pos.model.form.FileForm;
import com.increff.pos.model.form.InventoryForm;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Inventory Management", description = "Manage inventories for products")
@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryDto inventoryDto;

    public InventoryController(InventoryDto inventoryDto) {
        this.inventoryDto = inventoryDto;
    }

    @Operation(summary = "Update inventory for a product")
    @RequestMapping(path = "/update/{barcode}", method = RequestMethod.PUT)
    public InventoryData updateInventory(@PathVariable String barcode, @RequestBody InventoryForm inventoryForm) throws ApiException {
        return inventoryDto.updateInventory(barcode, inventoryForm);
    }

    @Operation(summary = "Update inventory for multiple products at once")
    @RequestMapping(path = "/bulkUpdate", method = RequestMethod.POST)
    public FileData updateBulkInventory(@RequestBody FileForm base64file) throws ApiException {
        return inventoryDto.addProductsInventory(base64file);
    }

}
