package com.josedjaykv.inventory_service.controllers;

import com.josedjaykv.inventory_service.model.dtos.BaseResponse;
import com.josedjaykv.inventory_service.model.dtos.OrderItemRequest;
import com.josedjaykv.inventory_service.model.entities.Inventory;
import com.josedjaykv.inventory_service.sevices.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor

public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/{sku}")
    @ResponseStatus(HttpStatus.OK)
    public boolean isInStock(@PathVariable("sku") String sku) {
        return inventoryService.isInStock(sku);
    }

    @PostMapping("/in-stock")
    @ResponseStatus(HttpStatus.OK)
    public BaseResponse areInStock(@RequestBody List<OrderItemRequest> orederItems){
        return inventoryService.areInStock(orederItems);
    }
}
