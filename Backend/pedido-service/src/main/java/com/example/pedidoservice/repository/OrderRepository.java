package com.example.pedidoservice.repository;

import com.example.pedidoservice.model.Order;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class OrderRepository {

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Try multiple locations for orders.json so the service works both from IDE and from Docker
    private File resolveOrdersFile() {
        // 1) environment override
        String env = System.getenv("ORDERS_FILE");
        if (env != null && !env.isBlank()) {
            File f = new File(env);
            if (f.exists()) return f;
        }

        // 2) current module data folder
        File f1 = new File("data/orders.json");
        if (f1.exists()) return f1;

        // 3) parent folder (project-level Backend/data)
        File f2 = new File("../data/orders.json");
        if (f2.exists()) return f2;

        // 4) target/classes (when packaged)
        File f3 = new File("target/classes/orders.json");
        if (f3.exists()) return f3;

        // fallback to parent data path for writing later
        return f2;
    }

    public List<Order> findAll() {
        File file = resolveOrdersFile();

        if (file == null || !file.exists()) {
            return new ArrayList<>();
        }

        try {
            return objectMapper.readValue(file, new TypeReference<List<Order>>() {});
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
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
        try {
            File out = resolveOrdersFile();
            if (out == null) {
                out = new File("../data/orders.json");
            }
            File parent = out.getParentFile();
            if (parent != null) parent.mkdirs();
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(out, orders);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
