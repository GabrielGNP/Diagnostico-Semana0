package com.example.usuarioservice.persistence;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.usuarioservice.model.User;
import com.example.usuarioservice.persistence.IUserPersistence;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Repositorio que implementa persistencia en JSON.
 * Implementa IUserPersistence para permitir cambio futuro a base de datos.
 */
@Service
@Slf4j
public class UserRepository implements IUserPersistence {

    private final ObjectMapper mapper = new ObjectMapper();
    private final Map<Integer, User> users = Collections.synchronizedMap(new HashMap<>());
    private final AtomicInteger nextId = new AtomicInteger(1);
    private File jsonFile;

    @Value("${users.persistence.file}")
    private String filePath;

    public void setFilePathForTests(String filePath) {
        this.filePath = filePath;
    }

    /**
     * Implementa el método de la interfaz IUserPersistence.
     * Delega al método init() para mantener compatibilidad.
     */
    @Override
    public void initialize() throws IOException {
        init();
    }

    /**
     * Inicializa el repositorio cargando usuarios desde JSON.
     * @deprecated Usar initialize() para cumplir con IUserPersistence
     */
    public void init() throws IOException {
        log.info("Inicializando persistencia de usuarios desde JSON");
        String usersFileEnv = System.getenv("USERS_FILE");

        if (usersFileEnv != null && !usersFileEnv.isBlank()) {
            jsonFile = new File(usersFileEnv);
        } else if (filePath != null && !filePath.isBlank()) {
            jsonFile = new File(filePath);
        } else {
            throw new IllegalStateException("users.persistence.file no está configurado");
        }

        if (jsonFile.exists()) {
            try {
                log.debug("Cargando usuarios desde archivo: {}", jsonFile.getAbsolutePath());
                Collection<User> fromFile = mapper.readValue(jsonFile, new TypeReference<Collection<User>>() {});
                loadUsers(fromFile);
                log.info("Usuarios cargados exitosamente");
                return;
            } catch (Exception ex) {
                log.warn("Error cargando usuarios desde archivo, creando nuevo", ex);
            }
        }

        log.info("Archivo de usuarios no encontrado, creando nuevo");
        File parent = jsonFile.getParentFile();
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
            if (jsonFile == null) {
                if (filePath == null || filePath.isBlank()) {
                    throw new IllegalStateException("users.persistence.file no está configurado");
                }
                jsonFile = new File(filePath);
            }
            File parent = jsonFile.getParentFile();
            if (parent != null) parent.mkdirs();
            mapper.writerWithDefaultPrettyPrinter().writeValue(jsonFile, users.values());
            log.debug("Usuarios persistidos correctamente en: {}", jsonFile.getAbsolutePath());
        } catch (IOException e) {
            log.error("Error guardando usuarios en archivo", e);
            throw new RuntimeException("Error al persistir usuarios", e);
        }
    }

    @Override
    public Collection<User> findAll() {
        log.debug("Obteniendo todos los usuarios");
        return users.values();
    }

    @Override
    public Collection<User> findAllActive() {
        log.debug("Obteniendo usuarios activos (excluyendo soft-deleted)");
        return users.values().stream()
                .filter(User::isActive)
                .toList();
    }

    @Override
    public User findById(int id) {
        log.debug("Buscando usuario por ID: {}", id);
        return users.get(id);
    }

    @Override
    public User findByEmail(String email) {
        log.debug("Buscando usuario por email: {}", email);
        for (User u : users.values()) {
            if (u.getMail() != null && u.getMail().equalsIgnoreCase(email)) {
                return u;
            }
        }
        return null;
    }

    @Override
    public User save(User user) {
        log.info("Guardando nuevo usuario con email: {}", user.getMail());
        if (user.getId() == null || user.getId() <= 0) {
            user.setId(nextId.getAndIncrement());
        }
        users.put(user.getId(), user);
        nextId.updateAndGet(x -> Math.max(x, user.getId() + 1));
        writeToFile();
        return user;
    }

    @Override
    public boolean deleteById(int id) {
        log.info("Eliminando usuario ID: {}", id);
        User removed = users.remove(id);
        if (removed != null) {
            writeToFile();
            log.info("Usuario {} eliminado exitosamente", id);
            return true;
        }
        log.warn("Usuario no encontrado para eliminar: {}", id);
        return false;
    }

    @Override
    public void deleteAll() {
        users.clear();
        writeToFile();
    }

    @Override
    public User update(int id, User user) {
        log.info("Actualizando usuario ID: {}", id);
        user.setId(id);
        users.put(id, user);
        nextId.updateAndGet(x -> Math.max(x, id + 1));
        writeToFile();
        return user;
    }

    @Override
    public User partialUpdate(int id, Map<String, Object> updates) {
        log.debug("Actualizando parcialmente usuario ID: {}", id);
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
