package com.example.usuarioservice.model;

public class User {
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
