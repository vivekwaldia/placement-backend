package com.college.placement_backend.dto;

import jakarta.validation.constraints.NotBlank;

public class LoginRequest{
    @NotBlank(message = "Email cannot be blank")
    private String email;

    @NotBlank(message = "Password cannot be blank")
    private String password;

    public String getEmail(){
        return email;
    }

    public String getPassword(){
        return password;
    }

    public void setEmail(String email){
        this.email = email;
    }

    public void setPassword(String password){
        this.password = password;
    }
}