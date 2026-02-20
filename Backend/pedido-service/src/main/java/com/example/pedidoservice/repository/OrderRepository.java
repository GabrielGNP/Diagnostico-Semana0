package com.example.pedidoservice.repository;

import com.example.pedidoservice.model.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Order Repository - PostgreSQL implementation using JPA.
 *
 * User Story: HU-ORD-01
 * Migration: JSON files → PostgreSQL database
 *
 * This repository now delegates to OrderJpaRepository for database access.
 * The old file-based implementation has been replaced with JPA.
 *
 * Performance:
 * - Database queries optimized with indexes
 * - Connection pooling via HikariCP
 * - Native PostgreSQL support
 */
@Repository
public class OrderRepository {

    @Autowired
    private OrderJpaRepository jpaRepository;

    /**
     * Find all orders from PostgreSQL database.
     *
     * @return List of all orders (active and inactive)
     */
    public List<Order> findAll() {
        return jpaRepository.findAll();
    }

    /**
     * Save or update an order in PostgreSQL.
     *
     * @param order Order to save
     * @return Saved order with generated ID if new
     */
    public Order save(Order order) {
        return jpaRepository.save(order);
    }

    /**
     * Soft-delete an order by ID.
     * Sets active=false instead of physically deleting the record.
     *
     * @param id Order ID to delete
     */
    public void deleteById(int id) {
        Optional<Order> orderOpt = jpaRepository.findById(id);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            order.setActive(false);
            jpaRepository.save(order);
        }
    }

    /**
     * Find an order by ID.
     *
     * @param id Order ID
     * @return Optional containing the order if found
     */
    public Optional<Order> findById(int id) {
        return jpaRepository.findById(id);
    }

    /**
     * Find all orders by user ID.
     *
     * @param userId User ID to filter by
     * @return List of orders belonging to the user
     */
    public List<Order> findByUserId(int userId) {
        return jpaRepository.findByIdUser(userId);
    }

    /**
     * Find all active orders (HU-ORD-01).
     * Only returns orders where active=true.
     *
     * @return List of active orders
     */
    public List<Order> findAllActive() {
        return jpaRepository.findAllActive();
    }
}
