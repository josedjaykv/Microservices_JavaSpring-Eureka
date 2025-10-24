package com.josedjaykv.orders_service.controllers;

import com.josedjaykv.orders_service.model.dtos.OrderRequest;
import com.josedjaykv.orders_service.model.dtos.OrderResponse;
import com.josedjaykv.orders_service.services.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public String placeOrder(@RequestBody OrderRequest orderRequest){
        this.orderService.placeOrder(orderRequest);
        return "Order placed successfully";
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<OrderResponse> getOrders(){
        return this.orderService.getAllOrders();
    }
}
