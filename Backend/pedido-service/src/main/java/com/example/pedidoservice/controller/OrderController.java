package com.example.pedidoservice.controller;

import com.example.pedidoservice.dto.OrderDto;
import com.example.pedidoservice.dto.OrderWithUserDto;
import com.example.pedidoservice.dto.OrderStateUpdateDto;
import jakarta.validation.Valid;
import com.example.pedidoservice.model.State;
import com.example.pedidoservice.service.OrderService;
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

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

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
    public ResponseEntity<OrderDto> createOrder(@Valid @RequestBody OrderDto orderDto) {
        OrderDto createdOrder = orderService.createOrder(orderDto);
        return ResponseEntity.created(java.net.URI.create("/orders/" + createdOrder.getId())).body(createdOrder);
    }

    /**
        * Soft-delete an order by ID.
        *
        * Endpoint: DELETE /orders/{id}
        * <p>
        * Performs a soft delete by setting `active=false`. On success returns
        * HTTP 204 No Content. If the order does not exist returns HTTP 404 Not Found.
        *
        * @param id Order ID
        * @return 204 No Content or 404 Not Found
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable("id") Integer id) {
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
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
    public ResponseEntity<OrderDto> showOrderById(@PathVariable("id") Integer id) {
        OrderDto orderDto = orderService.showOrderById(id);
        return ResponseEntity.ok(orderDto);
    }

    /**
     * Get an order by id with enriched user information.
     * Endpoint: GET /orders/{id}/user
     */
    @GetMapping("/{id}/user")
    public ResponseEntity<OrderWithUserDto> showOrderWithUser(@PathVariable("id") Integer id) {
        OrderWithUserDto order = orderService.getOrderWithUserInfo(id);
        return ResponseEntity.ok(order);
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
    public ResponseEntity<List<OrderDto>> listOrders() {
        List<OrderDto> orders = orderService.findAllActiveOrders();
        return ResponseEntity.ok(orders);
    }

    /**
     * List orders for a given user.
     * Endpoint: GET /orders/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderDto>> listOrdersByUser(@PathVariable("userId") Integer userId) {
        List<OrderDto> orders = orderService.listOrdersByIdUser(userId);
        return ResponseEntity.ok(orders);
    }

    /**
     * Administrative endpoint returning all orders (including inactive).
     * Endpoint: GET /orders/all
     */
    @GetMapping("/all")
    public ResponseEntity<List<OrderDto>> listAllOrdersEndpoint() {
        List<OrderDto> orders = orderService.listAllOrders();
        return ResponseEntity.ok(orders);
    }

    /**
     * Change order state.
     *
     * Endpoint: PATCH /orders/{id}
     * <p>
     * Expects an `OrderStateUpdateDto` in the request body containing the
     * `state` to set. Returns the updated `OrderDto` on success (HTTP 200).
     * Validation errors return HTTP 400. If the order does not exist returns
     * HTTP 404.
     *
     * @param id       Order ID
     * @param orderStateUpdateDto DTO containing new state
     * @return Updated order or 404/400
     */
    @PatchMapping("/{id}")
    public ResponseEntity<OrderDto> changeStateOrder(@PathVariable("id") Integer id, @Valid @RequestBody OrderStateUpdateDto orderStateUpdateDto) {
        State newState = orderStateUpdateDto.getState();
        OrderDto updatedOrder = orderService.changeStateOrder(id, newState);
        return ResponseEntity.ok(updatedOrder);
    }

}
