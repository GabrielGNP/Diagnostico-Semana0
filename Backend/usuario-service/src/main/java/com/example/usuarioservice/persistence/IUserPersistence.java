package com.example.usuarioservice.persistence;

import com.example.usuarioservice.model.User;
import java.io.IOException;
import java.util.Collection;
import java.util.Optional;

public interface IUserPersistence {

    void initialize() throws IOException;

    Collection<User> findAll();

    Collection<User> findAllActive();

    User findById(int id);

    User findByEmail(String email);

    User save(User user);

    User update(int id, User user);

    User partialUpdate(int id, java.util.Map<String, Object> updates);

    boolean deleteById(int id);

    void deleteAll();
}
