package com.college.placement_backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class StudentRequestDTO{
    // NotBlank for Strings
    @NotBlank(message = "Student name is required")
    private String name;

    @NotBlank(message = "Student email is required")
    private String email;

    @NotNull(message = "CGPA is required")
    @DecimalMin(value = "0.0", message = "CGPA cannot be negative")
    @DecimalMax(value = "10.0", message = "CGPA cannot exceed 10")
    private Double cgpa;

    // NotNull for numbers/objects
    @NotNull(message = "Skills cannot be empty")
    private List<String> skills;

    public String getName(){
        return name;
    }

    public String getEmail(){
        return email;
    }

    public Double getCgpa(){
        return cgpa;
    }

    public List<String> getSkills(){
        return skills;
    }

    public void setName(String name){
        this.name = name;
    }

    public void setEmail(String email){
        this.email = email;
    }

    public void setCgpa(Double cgpa){
        this.cgpa = cgpa;
    }

    public void setSkills(List<String> skills){
        this.skills = skills;
    }
}