package com.example.pedidoservice.controller;

import com.example.pedidoservice.dto.OrderDto;
<<<<<<< HEAD
<<<<<<< HEAD
=======
import com.example.pedidoservice.dto.OrderWithUserDto;
>>>>>>> ab6c4255fa1a3e189b475473b95fdd5cdd95b37a
=======
import com.example.pedidoservice.dto.OrderWithUserDto;
>>>>>>> 20046a0775b26f20b6321e01ab7470e09dc3970d
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

<<<<<<< HEAD
<<<<<<< HEAD
=======
=======
>>>>>>> 20046a0775b26f20b6321e01ab7470e09dc3970d
    @GetMapping("/{id}/with-user-info")
    public ResponseEntity<OrderWithUserDto> getOrderWithUserInfo(@PathVariable("id") int id) {
        try{
            OrderWithUserDto order = orderService.getOrderWithUserInfo(id);
            if (order != null) {
                return ResponseEntity.ok(order);
            } else {
                return ResponseEntity.notFound().build();
            }
        }catch(Exception e){
            System.err.println("Error fetching order with user info: " + e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

<<<<<<< HEAD
>>>>>>> ab6c4255fa1a3e189b475473b95fdd5cdd95b37a
=======
>>>>>>> 20046a0775b26f20b6321e01ab7470e09dc3970d
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
