package com.example.usuarioservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import com.example.usuarioservice.model.User;
import com.example.usuarioservice.service.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;

@SpringBootApplication
@RestController
@CrossOrigin(origins = "http://localhost:3001")
public class UsuarioServiceApplication {

	@Autowired
	private UserRepository userRepository;

	public static void main(String[] args) {
		SpringApplication.run(UsuarioServiceApplication.class, args);
	}

	// CORS configured via CorsConfig class in package config

	@PostConstruct
	public void init() throws IOException {
		userRepository.init();
	}

	// GET all users
	@GetMapping("/users")
	public Collection<User> getAllUsers() {
		return userRepository.findAll();
	}

	// GET /user/{identifier} - if identifier contains '@' search by email, otherwise by id
	@GetMapping("/user/{identifier}")
	public ResponseEntity<User> getUser(@PathVariable String identifier) {
		if (identifier == null) return ResponseEntity.badRequest().build();

		// if looks like an email, search by mail field
		if (identifier.contains("@")) {
			User user = userRepository.findByEmail(identifier);
			if (user != null) {
				return ResponseEntity.ok(user);
			}
			return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
		}

		// otherwise try parse id
		try {
			int id = Integer.parseInt(identifier);
			User user = userRepository.findById(id);
			if (user == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
			return ResponseEntity.ok(user);
		} catch (NumberFormatException ex) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
		}
	}

	// DELETE /user/{id}
	@DeleteMapping("/user/{id}")
	public ResponseEntity<Void> deleteUser(@PathVariable int id) {
		if (userRepository.deleteById(id)) {
			return ResponseEntity.noContent().build();
		}
		return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
	}

	// POST /user/add (create)
	@PostMapping("/user/add")
	public ResponseEntity<User> addUser(@RequestBody User incoming) {
		if (incoming == null) return ResponseEntity.badRequest().build();
		User saved = userRepository.save(incoming);
		return ResponseEntity.status(HttpStatus.CREATED).body(saved);
	}

	// PUT /user/{id} (full replace)
	@PutMapping("/user/{id}")
	public ResponseEntity<User> replaceUser(@PathVariable int id, @RequestBody User incoming) {
		if (incoming == null) return ResponseEntity.badRequest().build();
		User updated = userRepository.update(id, incoming);
		return ResponseEntity.ok(updated);
	}

	// PATCH /user/{id} - partial update (only provided fields will be changed)
	@PatchMapping("/user/{id}")
	public ResponseEntity<User> patchUser(@PathVariable int id, @RequestBody Map<String, Object> updates) {
		User existing = userRepository.partialUpdate(id, updates);
		if (existing == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
		return ResponseEntity.ok(existing);
	}
}
