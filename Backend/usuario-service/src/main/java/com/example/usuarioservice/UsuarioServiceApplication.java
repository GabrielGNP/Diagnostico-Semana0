package com.example.usuarioservice;

<<<<<<< HEAD
<<<<<<< HEAD
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.ClassPathResource;
=======
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
>>>>>>> ab6c4255fa1a3e189b475473b95fdd5cdd95b37a
=======
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
>>>>>>> 20046a0775b26f20b6321e01ab7470e09dc3970d
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.PostConstruct;
<<<<<<< HEAD
<<<<<<< HEAD
import java.io.File;
=======
>>>>>>> 20046a0775b26f20b6321e01ab7470e09dc3970d
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
<<<<<<< HEAD
import java.util.concurrent.atomic.AtomicInteger;
=======
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
=======
>>>>>>> 20046a0775b26f20b6321e01ab7470e09dc3970d

import com.example.usuarioservice.model.User;
import com.example.usuarioservice.service.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
<<<<<<< HEAD
>>>>>>> ab6c4255fa1a3e189b475473b95fdd5cdd95b37a
=======
>>>>>>> 20046a0775b26f20b6321e01ab7470e09dc3970d

@SpringBootApplication
@RestController
@CrossOrigin(origins = "http://localhost:3001")
public class UsuarioServiceApplication {

<<<<<<< HEAD
<<<<<<< HEAD
	private final ObjectMapper mapper = new ObjectMapper();
	private final Map<Integer, User> users = Collections.synchronizedMap(new HashMap<>());
	private final AtomicInteger nextId = new AtomicInteger(1);
	private File jsonFile;
=======
	@Autowired
	private UserRepository userRepository;
>>>>>>> ab6c4255fa1a3e189b475473b95fdd5cdd95b37a
=======
	@Autowired
	private UserRepository userRepository;
>>>>>>> 20046a0775b26f20b6321e01ab7470e09dc3970d

	public static void main(String[] args) {
		SpringApplication.run(UsuarioServiceApplication.class, args);
	}

	// CORS configured via CorsConfig class in package config

	@PostConstruct
	public void init() throws IOException {
<<<<<<< HEAD
<<<<<<< HEAD
		// Support external USERS_FILE env var (useful for Docker volume mounting)
		String usersFileEnv = System.getenv("USERS_FILE");
		File external = null;
		if (usersFileEnv != null && !usersFileEnv.isBlank()) {
			external = new File(usersFileEnv);
		}

		File resourceFile = new File("src/main/resources/users.json");
		File targetFile = new File("target/classes/users.json");

		// Priority: external USERS_FILE -> resourceFile -> targetFile -> create resourceFile
		if (external != null) {
			jsonFile = external;
			if (jsonFile.exists()) {
				try {
					Collection<User> fromFile = mapper.readValue(jsonFile, new TypeReference<Collection<User>>() {});
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
				return;
			} catch (Exception ex) {
				// ignore parse errors and fallthrough
			}
		}

		if (targetFile.exists()) {
			jsonFile = targetFile;
			try {
				Collection<User> fromFile = mapper.readValue(targetFile, new TypeReference<Collection<User>>() {});
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

	private synchronized void writeToFile() {
		try {
			if (jsonFile == null) jsonFile = new File("users.json");
			File parent = jsonFile.getParentFile();
			if (parent != null) parent.mkdirs();
			mapper.writerWithDefaultPrettyPrinter().writeValue(jsonFile, users.values());
		} catch (IOException e) {
			e.printStackTrace();
		}
=======
		userRepository.init();
>>>>>>> ab6c4255fa1a3e189b475473b95fdd5cdd95b37a
=======
		userRepository.init();
>>>>>>> 20046a0775b26f20b6321e01ab7470e09dc3970d
	}

	// GET all users
	@GetMapping("/users")
	public Collection<User> getAllUsers() {
<<<<<<< HEAD
<<<<<<< HEAD
		return users.values();
=======
		return userRepository.findAll();
>>>>>>> ab6c4255fa1a3e189b475473b95fdd5cdd95b37a
=======
		return userRepository.findAll();
>>>>>>> 20046a0775b26f20b6321e01ab7470e09dc3970d
	}

	// GET /user/{identifier} - if identifier contains '@' search by email, otherwise by id
	@GetMapping("/user/{identifier}")
	public ResponseEntity<User> getUser(@PathVariable String identifier) {
		if (identifier == null) return ResponseEntity.badRequest().build();

		// if looks like an email, search by mail field
		if (identifier.contains("@")) {
<<<<<<< HEAD
<<<<<<< HEAD
			for (User u : users.values()) {
				if (u.getMail() != null && u.getMail().equalsIgnoreCase(identifier)) {
					return ResponseEntity.ok(u);
				}
=======
			User user = userRepository.findByEmail(identifier);
			if (user != null) {
				return ResponseEntity.ok(user);
>>>>>>> ab6c4255fa1a3e189b475473b95fdd5cdd95b37a
=======
			User user = userRepository.findByEmail(identifier);
			if (user != null) {
				return ResponseEntity.ok(user);
>>>>>>> 20046a0775b26f20b6321e01ab7470e09dc3970d
			}
			return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
		}

		// otherwise try parse id
		try {
			int id = Integer.parseInt(identifier);
<<<<<<< HEAD
<<<<<<< HEAD
			User u = users.get(id);
			if (u == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
			return ResponseEntity.ok(u);
=======
			User user = userRepository.findById(id);
			if (user == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
			return ResponseEntity.ok(user);
>>>>>>> ab6c4255fa1a3e189b475473b95fdd5cdd95b37a
=======
			User user = userRepository.findById(id);
			if (user == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
			return ResponseEntity.ok(user);
>>>>>>> 20046a0775b26f20b6321e01ab7470e09dc3970d
		} catch (NumberFormatException ex) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
		}
	}

	// DELETE /user/{id}
	@DeleteMapping("/user/{id}")
	public ResponseEntity<Void> deleteUser(@PathVariable int id) {
<<<<<<< HEAD
<<<<<<< HEAD
		User removed = users.remove(id);
		if (removed == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
		writeToFile();
		return ResponseEntity.noContent().build();
=======
=======
>>>>>>> 20046a0775b26f20b6321e01ab7470e09dc3970d
		if (userRepository.deleteById(id)) {
			return ResponseEntity.noContent().build();
		}
		return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
<<<<<<< HEAD
>>>>>>> ab6c4255fa1a3e189b475473b95fdd5cdd95b37a
=======
>>>>>>> 20046a0775b26f20b6321e01ab7470e09dc3970d
	}

	// POST /user/add (create)
	@PostMapping("/user/add")
	public ResponseEntity<User> addUser(@RequestBody User incoming) {
		if (incoming == null) return ResponseEntity.badRequest().build();
<<<<<<< HEAD
<<<<<<< HEAD
		Integer id = incoming.getId();
		if (id == null || id <= 0) id = nextId.getAndIncrement();
		incoming.setId(id);
		users.put(id, incoming);
		writeToFile();
		return ResponseEntity.status(HttpStatus.CREATED).body(incoming);
=======
		User saved = userRepository.save(incoming);
		return ResponseEntity.status(HttpStatus.CREATED).body(saved);
>>>>>>> ab6c4255fa1a3e189b475473b95fdd5cdd95b37a
=======
		User saved = userRepository.save(incoming);
		return ResponseEntity.status(HttpStatus.CREATED).body(saved);
>>>>>>> 20046a0775b26f20b6321e01ab7470e09dc3970d
	}

	// PUT /user/{id} (full replace)
	@PutMapping("/user/{id}")
	public ResponseEntity<User> replaceUser(@PathVariable int id, @RequestBody User incoming) {
		if (incoming == null) return ResponseEntity.badRequest().build();
<<<<<<< HEAD
<<<<<<< HEAD
		incoming.setId(id);
		users.put(id, incoming);
		nextId.updateAndGet(x -> Math.max(x, id + 1));
		writeToFile();
		return ResponseEntity.ok(incoming);
=======
		User updated = userRepository.update(id, incoming);
		return ResponseEntity.ok(updated);
>>>>>>> ab6c4255fa1a3e189b475473b95fdd5cdd95b37a
=======
		User updated = userRepository.update(id, incoming);
		return ResponseEntity.ok(updated);
>>>>>>> 20046a0775b26f20b6321e01ab7470e09dc3970d
	}

	// PATCH /user/{id} - partial update (only provided fields will be changed)
	@PatchMapping("/user/{id}")
	public ResponseEntity<User> patchUser(@PathVariable int id, @RequestBody Map<String, Object> updates) {
<<<<<<< HEAD
<<<<<<< HEAD
		User existing = users.get(id);
=======
		User existing = userRepository.partialUpdate(id, updates);
>>>>>>> 20046a0775b26f20b6321e01ab7470e09dc3970d
		if (existing == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
		return ResponseEntity.ok(existing);
	}
<<<<<<< HEAD

	// Simple model class
	public static class User {
		private Integer id;
		private String name;
		private String password;
		private String mail;
		private boolean active;

		public User() {}

		public User(Integer id, String name, String password, String mail, boolean active) {
			this.id = id;
			this.name = name;
			this.password = password;
			this.mail = mail;
			this.active = active;
		}

		public Integer getId() { return id; }
		public void setId(Integer id) { this.id = id; }

		public String getName() { return name; }
		public void setName(String name) { this.name = name; }

		public String getPassword() { return password; }
		public void setPassword(String password) { this.password = password; }

		public String getMail() { return mail; }
		public void setMail(String mail) { this.mail = mail; }

		public boolean isActive() { return active; }
		public void setActive(boolean active) { this.active = active; }
	}

=======
		User existing = userRepository.partialUpdate(id, updates);
		if (existing == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
		return ResponseEntity.ok(existing);
	}
>>>>>>> ab6c4255fa1a3e189b475473b95fdd5cdd95b37a
=======
>>>>>>> 20046a0775b26f20b6321e01ab7470e09dc3970d
}
