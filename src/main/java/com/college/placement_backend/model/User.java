package com.college.placement_backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

// Persistence library contains rules for translating
// java objects to database records
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.validation.constraints.NotBlank;

@Entity
// Create user table for shared data (name, email),
// and separate Company table for specific details
// P.K - F.K relation
@Inheritance(strategy = InheritanceType.JOINED)

public abstract class User{
    @Id
    // Auto increment id everytime with GenerationType tag
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer userId;
    
    private String name;
    private String email;

    @JsonIgnore
    @NotBlank(message = "Password cannot be empty")
    private String password;

    @NotBlank(message = "Role must be defined")
    private String role;

    public User(){}

    public User(String name, String email){
        this.name = name;
        this.email = email;
    }

    public Integer getUserId(){
        return userId;
    }

    public String getName(){
        return name;
    }

    public String getEmail(){
        return email;
    }

    public String getPassword(){
        return password;
    }

    public String getRole(){
        return role;
    }

    public void setUserId(Integer userId){
        this.userId = userId;
    }

    public void setName(String name){
        this.name = name;
    }

    public void setEmail(String email){
        this.email = email;
    }

    public void setPassword(String password){
        this.password = password;
    }

    public void setRole(String role){
        this.role = role;
    }
}