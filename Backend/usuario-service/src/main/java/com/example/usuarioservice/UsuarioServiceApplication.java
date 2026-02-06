package com.example.usuarioservice;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@SpringBootApplication
@RestController
public class UsuarioServiceApplication {

	private final ObjectMapper mapper = new ObjectMapper();
	private final Map<Integer, User> users = Collections.synchronizedMap(new HashMap<>());
	private final AtomicInteger nextId = new AtomicInteger(1);
	private File jsonFile;

	public static void main(String[] args) {
		SpringApplication.run(UsuarioServiceApplication.class, args);
	}

	@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				registry.addMapping("/**").allowedOrigins("*").allowedMethods("*");
			}
		};
	}

	@PostConstruct
	public void init() throws IOException {
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
	}

	// GET all users
	@GetMapping("/users")
	public Collection<User> getAllUsers() {
		return users.values();
	}

	// GET /user/{id}
	@GetMapping("/user/{id}")
	public ResponseEntity<User> getUser(@PathVariable int id) {
		User u = users.get(id);
		if (u == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
		return ResponseEntity.ok(u);
	}

	// DELETE /user/{id}
	@DeleteMapping("/user/{id}")
	public ResponseEntity<Void> deleteUser(@PathVariable int id) {
		User removed = users.remove(id);
		if (removed == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
		writeToFile();
		return ResponseEntity.noContent().build();
	}

	// POST /user/add (create)
	@PostMapping("/user/add")
	public ResponseEntity<User> addUser(@RequestBody User incoming) {
		if (incoming == null) return ResponseEntity.badRequest().build();
		Integer id = incoming.getId();
		if (id == null || id <= 0) id = nextId.getAndIncrement();
		incoming.setId(id);
		users.put(id, incoming);
		writeToFile();
		return ResponseEntity.status(HttpStatus.CREATED).body(incoming);
	}

	// PUT /user/{id} (full replace)
	@PutMapping("/user/{id}")
	public ResponseEntity<User> replaceUser(@PathVariable int id, @RequestBody User incoming) {
		if (incoming == null) return ResponseEntity.badRequest().build();
		incoming.setId(id);
		users.put(id, incoming);
		nextId.updateAndGet(x -> Math.max(x, id + 1));
		writeToFile();
		return ResponseEntity.ok(incoming);
	}

	// PATCH /user/{id} - partial update (only provided fields will be changed)
	@PatchMapping("/user/{id}")
	public ResponseEntity<User> patchUser(@PathVariable int id, @RequestBody Map<String, Object> updates) {
		User existing = users.get(id);
		if (existing == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

		if (updates.containsKey("name")) existing.setName((String) updates.get("name"));
		if (updates.containsKey("password")) existing.setPassword((String) updates.get("password"));
		if (updates.containsKey("mail")) existing.setMail((String) updates.get("mail"));
		if (updates.containsKey("active")) existing.setActive(Boolean.parseBoolean(String.valueOf(updates.get("active"))));

		users.put(id, existing);
		writeToFile();
		return ResponseEntity.ok(existing);
	}

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

}
