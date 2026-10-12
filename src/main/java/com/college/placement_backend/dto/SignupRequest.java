package com.college.placement_backend.dto;

import jakarta.validation.constraints.NotBlank;

public class SignupRequest{
    @NotBlank(message = "Email cannot be blank")
    private String email;

    @NotBlank(message = "Password cannot be blank")
    private String password;

    @NotBlank(message = "Name cannot be blank")
    private String name;

    @NotBlank(message = "Role cannot be blank")
    private String role;

    public String getEmail(){
        return email;
    }

    public String getPassword(){
        return password;
    }

    public String getName(){
        return name;
    }

    public String getRole(){
        return role;
    }

    public void setEmail(String email){
        this.email = email;
    }

    public void setPassword(String password){
        this.password = password;
    }

    public void setName(String name){
        this.name = name;
    }

    public void setRole(String role){
        this.role = role;
    }
}