package com.example.pedidoservice.repository;

import com.example.pedidoservice.model.Order;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class OrderRepository {

    private final String FILE_PATH = "data/orders.json";
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<Order> findAll() {

        File file = new File(FILE_PATH);

        if (!file.exists()) {
            return new ArrayList<>();
        }

        return objectMapper.readValue(file, new TypeReference<List<Order>>() {});
    }



    public Order save(Order order) {
        List<Order> orders = findAll();
        // If update
        orders.removeIf(o -> o.getId() == order.getId());
        orders.add(order);
        writeOrders(orders);
        return order;
    }

    public void deleteById(int id) {
        List<Order> orders = findAll();
        boolean removed = orders.removeIf(o -> o.getId() == id);
        if (removed) {
            writeOrders(orders);
        }
    }

    public Optional<Order> findById(int id) {
        return findAll().stream().filter(o -> o.getId() == id).findFirst();
    }

    public List<Order> findByUserId(int userId) {
        List<Order> orders = findAll();
        List<Order> result = new ArrayList<>();
        for (Order order : orders) {
            if (order.getIdUser() == userId) {
                result.add(order);
            }
        }
        return result;
    }

    private void writeOrders(List<Order> orders) {
        objectMapper.writeValue(new File(FILE_PATH), orders);
    }
}
