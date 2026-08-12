package com.aishwarya.ers.model;

public class User {

    private int id;
    private String username;
    private String password;
    private Role role;
    private String department;


    public User() {
    }

    public User(int id, String username, String password, Role role, String department) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.department = department;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return password;
    }

    public Role getRole() {
        return role;
    }

    public String getDepartment() {
        return department;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPasswordHash(String passwordHash) {
        this.password = passwordHash;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", role=" + role +
                ", department='" + department + '\'' +
                '}';
    }
}