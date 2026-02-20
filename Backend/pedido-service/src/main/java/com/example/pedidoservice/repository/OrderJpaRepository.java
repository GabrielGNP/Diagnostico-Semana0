package com.example.pedidoservice.repository;

import com.example.pedidoservice.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * JPA Repository for Order entity.
 *
 * User Story: HU-ORD-01
 * Database: PostgreSQL
 * Table: orders
 *
 * This replaces the file-based OrderRepository with proper database access.
 */
@Repository
public interface OrderJpaRepository extends JpaRepository<Order, Integer> {

    /**
     * Find all orders by user ID.
     *
     * @param idUser User ID to filter by
     * @return List of orders belonging to the user
     */
    List<Order> findByIdUser(int idUser);

    /**
     * Find all active orders (HU-ORD-01).
     * Only returns orders where active=true (soft-delete pattern).
     *
     * @return List of active orders
     */
    @Query("SELECT o FROM Order o WHERE o.active = true")
    List<Order> findAllActive();

    /**
     * Find active orders by user ID.
     *
     * @param idUser User ID to filter by
     * @return List of active orders belonging to the user
     */
    @Query("SELECT o FROM Order o WHERE o.idUser = :idUser AND o.active = true")
    List<Order> findActiveByIdUser(int idUser);
}
