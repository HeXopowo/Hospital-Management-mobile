package com.hospital.management.model;

import java.io.Serializable;

public class User implements Serializable {
    private int userId;
    private String username;
    private String password;
    private String role;
    private int roleId;

    // Конструкторы
    public User() {}

    public User(String username, String password, String role, int roleId) {
        setUsername(username);
        setPassword(password);
        setRole(role);
        setRoleId(roleId);
    }

    // Геттеры и сеттеры
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        if (userId < 0) {
            throw new IllegalArgumentException("ID пользователя не может быть отрицательным");
        }
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Имя пользователя не может быть пустым");
        }
        this.username = username.trim();
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Пароль не может быть пустым");
        }
        this.password = password.trim();
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        if (!"DOCTOR".equals(role) && !"PATIENT".equals(role) && !"ADMIN".equals(role)) {
            throw new IllegalArgumentException("Недопустимая роль пользователя");
        }
        this.role = role;
    }

    public int getRoleId() {
        return roleId;
    }

    public void setRoleId(int roleId) {
        if (roleId < 0) {
            throw new IllegalArgumentException("ID роли должно быть неотрицательным числом");
        }
        this.roleId = roleId;
    }

    @Override
    public String toString() {
        return String.format("User{id=%d, username='%s', role='%s', roleId=%d}",
                userId, username, role, roleId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return userId == user.userId &&
                roleId == user.roleId &&
                username.equals(user.username) &&
                password.equals(user.password) &&
                role.equals(user.role);
    }

    @Override
    public int hashCode() {
        int result = userId;
        result = 31 * result + username.hashCode();
        result = 31 * result + password.hashCode();
        result = 31 * result + role.hashCode();
        result = 31 * result + roleId;
        return result;
    }
}