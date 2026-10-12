package com.college.placement_backend.model;

import jakarta.persistence.Entity;

// Entity tells spring boot to create a database for this class
@Entity
public class Company extends User{
    private String industry;
    private String location;

    public Company(){}

    public Company(String name, String email, String industry, String location){
        super(name, email);
        this.industry = industry;
        this.location = location;
    }

    public String getIndustry(){
        return industry;
    }

    public String getLocation(){
        return location;
    }

    public void setIndustry(String industry){
        this.industry = industry;
    }

    public void setLocation(String location){
        this.location = location;
    }
}