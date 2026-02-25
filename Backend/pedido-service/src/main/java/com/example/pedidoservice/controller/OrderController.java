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
@RequestMapping("/orders")
public class OrderController {

    /**
     * REST controller that exposes order-related operations.
     *
     * Endpoints:
     * - POST /orders : create an order (returns 201 Created with Location header)
     * - GET /orders : list active orders; supports optional query `userId` to filter results
     * - GET /orders/{id} : get order by id
     * - GET /orders/{id}?expand=user : get order with enriched user info
     * - DELETE /orders/{id} : delete an order (soft-delete: sets active=false)
     * - PATCH /orders/{id} : change order state
     *
     * The controller delegates business logic to `OrderService` and converts
     * results into appropriate HTTP responses.
     */

    @Autowired
    private OrderService orderService;

    /**
        * Create a new order.
        * <p>
        * Endpoint: POST /orders
        * <p>
        * On success returns HTTP 201 Created and sets the `Location` header to
        * `/orders/{id}` when the created DTO contains an id. If validation fails
        * returns HTTP 400 Bad Request with the validation message.
        *
        * @param orderDto Order data (name, description, idUser required)
        * @return Created order DTO or error message
     */
    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody OrderDto orderDto) {
        try {
            OrderDto createdOrder = orderService.createOrder(orderDto);
            if (createdOrder != null && createdOrder.getId() != null) {
                return ResponseEntity.created(java.net.URI.create("/orders/" + createdOrder.getId())).body(createdOrder);
            } else {
                return ResponseEntity.status(201).body(createdOrder);
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
        * Soft-delete an order by ID.
        *
        * Endpoint: DELETE /orders/{id}
        * <p>
        * Performs a soft delete by setting `active=false`. On success returns
        * HTTP 200 OK. If the order does not exist returns HTTP 404 Not Found.
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

    /**
     * Get an order by id.
     *
     * Endpoint: GET /orders/{id}
     * <p>
     * Optional query parameter: `expand=user` — when present the response will
     * include user details and use the enriched DTO. Without `expand` returns
     * the standard `OrderDto`.
     *
     * @param id     Order ID
     * @param expand optional expansion parameter, expects value `user`
     * @return Order DTO (enriched if `expand=user`) or 404 Not Found
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> showOrderById(@PathVariable("id") Integer id, @RequestParam(value = "expand", required = false) String expand) {
        if ("user".equals(expand)) {
            try {
                OrderWithUserDto order = orderService.getOrderWithUserInfo(id);
                if (order != null) {
                    return ResponseEntity.ok(order);
                } else {
                    return ResponseEntity.notFound().build();
                }
            } catch (Exception e) {
                return ResponseEntity.status(500).build();
            }
        } else {
            OrderDto orderDto = orderService.showOrderById(id);
            if (orderDto != null) {
                return ResponseEntity.ok(orderDto);
            } else {
                return ResponseEntity.notFound().build();
            }
        }
    }


    /**
     * List orders.
     *
     * Endpoint: GET /orders
     * <p>
     * Optional query parameter `userId` filters orders by the given user. If no
     * `userId` is provided the endpoint returns all active orders (soft-deleted
     * orders are excluded).
     *
     * @param userId optional user id to filter results
     * @return list of orders (HTTP 200)
     */
    @GetMapping
    public ResponseEntity<List<OrderDto>> listOrders(@RequestParam(value = "userId", required = false) Integer userId) {
        if (userId != null) {
            List<OrderDto> orders = orderService.listOrdersByIdUser(userId);
            return ResponseEntity.ok(orders);
        } else {
            List<OrderDto> orders = orderService.findAllActiveOrders();
            return ResponseEntity.ok(orders);
        }
    }

    /**
     * Change order state.
     *
     * Endpoint: PATCH /orders/{id}
     * <p>
     * Expects an `OrderDto` in the request body where the `state` field is set
     * to the desired new value. Returns the updated `OrderDto` on success
     * (HTTP 200). If `state` is missing returns HTTP 400. If the order does
     * not exist returns HTTP 404.
     *
     * @param id       Order ID
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
