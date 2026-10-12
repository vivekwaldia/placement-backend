package com.college.placement_backend.dto;

import jakarta.validation.constraints.NotBlank;

public class CompanyRequestDTO{
    @NotBlank(message = "Company name cannot be empty")
    private String name;

    @NotBlank(message = "Email must be provided")
    private String email;

    @NotBlank(message = "Industry must be provided")
    private String industry;

    @NotBlank(message = "Location cannot be empty")
    private String location;

    public String getName(){
        return name;   
    }

    public String getEmail(){
        return email;
    }

    public String getIndustry(){
        return industry;
    }

    public String getLocation(){
        return location;
    }

    public void setName(String name){
        this.name = name;
    }

    public void setEmail(String email){
        this.email = email;
    }

    public void setIndustry(String industry){
        this.industry = industry;
    }

    public void setLocation(String location){
        this.location = location;
    }
}