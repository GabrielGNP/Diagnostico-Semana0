package com.example.usuarioservice.service;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import com.example.usuarioservice.model.User;

@Service
public class UserRepository {

    private final ObjectMapper mapper = new ObjectMapper();
    private final Map<Integer, User> users = Collections.synchronizedMap(new HashMap<>());
    private final AtomicInteger nextId = new AtomicInteger(1);
    private File jsonFile;

    public void init() throws IOException {
        String usersFileEnv = System.getenv("USERS_FILE");
        File external = null;
        if (usersFileEnv != null && !usersFileEnv.isBlank()) {
            external = new File(usersFileEnv);
        }

        File resourceFile = new File("src/main/resources/users.json");
        File targetFile = new File("target/classes/users.json");

        if (external != null) {
            jsonFile = external;
            if (jsonFile.exists()) {
                try {
                    Collection<User> fromFile = mapper.readValue(jsonFile, new TypeReference<Collection<User>>() {});
                    loadUsers(fromFile);
                    return;
                } catch (Exception ex) {
                    // ignore parse errors and continue to other fallbacks
                }
            }
        }

        if (resourceFile.exists()) {
            jsonFile = resourceFile;
            try {
                Collection<User> fromFile = mapper.readValue(resourceFile, new TypeReference<Collection<User>>() {});
                loadUsers(fromFile);
                return;
            } catch (Exception ex) {
                // ignore parse errors and fallthrough
            }
        }

        if (targetFile.exists()) {
            jsonFile = targetFile;
            try {
                Collection<User> fromFile = mapper.readValue(targetFile, new TypeReference<Collection<User>>() {});
                loadUsers(fromFile);
                return;
            } catch (Exception ex) {
                // ignore and start empty
            }
        }

        // fallback: create resourceFile
        jsonFile = resourceFile;
        File parent = resourceFile.getParentFile();
        if (parent != null) parent.mkdirs();
        writeToFile();
    }

    private void loadUsers(Collection<User> fromFile) {
        for (User u : fromFile) {
            Integer uid = u.getId();
            int assigned;
            if (uid == null || uid <= 0) {
                assigned = nextId.getAndIncrement();
                u.setId(assigned);
            } else {
                assigned = uid;
            }
            users.put(assigned, u);
            nextId.updateAndGet(x -> Math.max(x, assigned + 1));
        }
    }

    public synchronized void writeToFile() {
        try {
            if (jsonFile == null) jsonFile = new File("users.json");
            File parent = jsonFile.getParentFile();
            if (parent != null) parent.mkdirs();
            mapper.writerWithDefaultPrettyPrinter().writeValue(jsonFile, users.values());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Collection<User> findAll() {
        return users.values();
    }

    public User findById(int id) {
        return users.get(id);
    }

    public User findByEmail(String email) {
        for (User u : users.values()) {
            if (u.getMail() != null && u.getMail().equalsIgnoreCase(email)) {
                return u;
            }
        }
        return null;
    }

    public User save(User user) {
        if (user.getId() == null || user.getId() <= 0) {
            user.setId(nextId.getAndIncrement());
        }
        users.put(user.getId(), user);
        nextId.updateAndGet(x -> Math.max(x, user.getId() + 1));
        writeToFile();
        return user;
    }

    public boolean deleteById(int id) {
        User removed = users.remove(id);
        if (removed != null) {
            writeToFile();
            return true;
        }
        return false;
    }

    public User update(int id, User user) {
        user.setId(id);
        users.put(id, user);
        nextId.updateAndGet(x -> Math.max(x, id + 1));
        writeToFile();
        return user;
    }

    public User partialUpdate(int id, Map<String, Object> updates) {
        User existing = users.get(id);
        if (existing == null) return null;

        if (updates.containsKey("name")) existing.setName((String) updates.get("name"));
        if (updates.containsKey("password")) existing.setPassword((String) updates.get("password"));
        if (updates.containsKey("mail")) existing.setMail((String) updates.get("mail"));
        if (updates.containsKey("active")) existing.setActive(Boolean.parseBoolean(String.valueOf(updates.get("active"))));

        users.put(id, existing);
        writeToFile();
        return existing;
    }
}
