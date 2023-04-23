package com.example.mchatserver.models;

import java.io.Serializable;
import java.time.LocalDate;


public class User implements Serializable {
    private int id;
    private String username, password, email, status, avatar;
    private LocalDate accountCreatedDate, lastLogin;


    public User(int id, String username, String password, String email, String status, LocalDate accountCreatedDate, LocalDate lastLogin, String avatar) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.status = status;
        this.accountCreatedDate = accountCreatedDate;
        this.lastLogin = lastLogin;
        this.avatar = avatar;
    }

    public User() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getAccountCreatedDate() {
        return accountCreatedDate;
    }

    public void setAccountCreatedDate(LocalDate accountCreatedDate) {
        this.accountCreatedDate = accountCreatedDate;
    }

    public LocalDate getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(LocalDate lastLogin) {
        this.lastLogin = lastLogin;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", email='" + email + '\'' +
                ", status='" + status + '\'' +
                ", avatar='" + avatar + '\'' +
                ", accountCreatedDate=" + accountCreatedDate +
                ", lastLogin=" + lastLogin +
                '}';
    }
}
