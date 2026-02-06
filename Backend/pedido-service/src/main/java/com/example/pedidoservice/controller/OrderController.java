package com.example.pedidoservice.controller;

import com.example.pedidoservice.dto.OrderDto;
import com.example.pedidoservice.model.State;
import com.example.pedidoservice.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/add")
    public ResponseEntity<OrderDto> createOrder(@RequestBody OrderDto orderDto) {
        OrderDto createdOrder = orderService.createOrder(orderDto);
        return ResponseEntity.ok(createdOrder);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable("id") int id) {
        orderService.deleteOrder(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDto> showOrderById(@PathVariable("id") int id) {
        OrderDto orderDto = orderService.showOrderById(id);
        if (orderDto != null) {
            return ResponseEntity.ok(orderDto);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/user/{idUser}")
    public ResponseEntity<List<OrderDto>> listOrdersByIdUser(@PathVariable("idUser") int idUser) {
        List<OrderDto> orders = orderService.listOrdersByIdUser(idUser);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/all")
    public ResponseEntity<List<OrderDto>> listAllOrders() {
        List<OrderDto> orders = orderService.listAllOrders();
        return ResponseEntity.ok(orders);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<OrderDto> changeStateOrder(@PathVariable("id") int id, @RequestBody OrderDto orderDto) {
        State newState = orderDto.getState();
        if (newState == null) {
            return ResponseEntity.badRequest().build();
        }
        OrderDto updatedOrder = orderService.changeStateOrder(id, newState);
        if (updatedOrder != null) {
            return ResponseEntity.ok(updatedOrder);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
