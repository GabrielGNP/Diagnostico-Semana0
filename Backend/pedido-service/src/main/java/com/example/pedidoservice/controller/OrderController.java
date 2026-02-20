package com.example.pedidoservice.controller;

import com.example.pedidoservice.dto.OrderDto;
import com.example.pedidoservice.dto.OrderWithUserDto;
import com.example.pedidoservice.model.State;
import com.example.pedidoservice.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/order")
public class OrderController {

    /**
     * REST controller that exposes order-related operations.
     *
     * Endpoints:
     * - POST /order/add : create an order
     * - DELETE /order/{id} : delete an order (soft-delete: sets active=false)
     * - GET /order/{id} : get order by id
     * - GET /order/{id}/with-user-info : get order with enriched user info
     * - GET /order/user/{idUser} : list orders by user id
     * - GET /order/all : list all ACTIVE orders (HU-ORD-01: only returns orders with active=true)
     * - PATCH /order/{id} : change order state
     *
     * The controller delegates business logic to `OrderService` and converts
     * results into appropriate HTTP responses.
     */

    @Autowired
    private OrderService orderService;

    /**
     * Create a new order (HU-ORD-05).
     *
     * @param orderDto Order data (name, description, idUser required)
     * @return Created order with ID or 400 Bad Request if validation fails
     */
    @PostMapping("/add")
    public ResponseEntity<?> createOrder(@RequestBody OrderDto orderDto) {
        try {
            OrderDto createdOrder = orderService.createOrder(orderDto);
            return ResponseEntity.ok(createdOrder);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Soft-delete an order by ID.
     *
     * @param id Order ID
     * @return 200 OK or 404 Not Found
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteOrder(@PathVariable("id") Integer id) {
        try {
            orderService.deleteOrder(id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDto> showOrderById(@PathVariable("id") Integer id) {
        OrderDto orderDto = orderService.showOrderById(id);
        if (orderDto != null) {
            return ResponseEntity.ok(orderDto);
        } else {
            return ResponseEntity.notFound().build();
        }
    }


    @GetMapping("/{id}/with-user-info")
    public ResponseEntity<OrderWithUserDto> getOrderWithUserInfo(@PathVariable("id") Integer id) {
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


    @GetMapping("/user/{idUser}")
    public ResponseEntity<List<OrderDto>> listOrdersByIdUser(@PathVariable("idUser") Integer idUser) {
        List<OrderDto> orders = orderService.listOrdersByIdUser(idUser);
        return ResponseEntity.ok(orders);
    }

    /**
     * Lists all active orders.
     *
     * User Story: HU-ORD-01
     *
     * Functional Requirements:
     * - FR-ORD-01-01: Retrieves all orders from PostgreSQL orders table
     * - FR-ORD-01-02: Returns fields: id, name, description, idUser, state, active
     * - FR-ORD-01-03: Returns HTTP 200 OK
     *
     * Business Rules:
     * - Only returns orders where active=true (soft-delete pattern)
     * - Returns empty list if no active orders exist
     *
     * @return ResponseEntity with list of active orders and HTTP 200 OK
     */
    @GetMapping("/all")
    public ResponseEntity<List<OrderDto>> listAllOrders() {
        List<OrderDto> orders = orderService.findAllActiveOrders();
        return ResponseEntity.ok(orders);
    }

    /**
     * Change order state.
     *
     * @param id Order ID
     * @param orderDto DTO containing new state
     * @return Updated order or 404/400
     */
    @PatchMapping("/{id}")
    public ResponseEntity<?> changeStateOrder(@PathVariable("id") Integer id, @RequestBody OrderDto orderDto) {
        State newState = orderDto.getState();
        if (newState == null) {
            return ResponseEntity.badRequest().body("El campo 'state' es requerido");
        }
        try {
            OrderDto updatedOrder = orderService.changeStateOrder(id, newState);
            return ResponseEntity.ok(updatedOrder);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
