package com.college.placement_backend.dto;

import jakarta.validation.constraints.NotBlank;

public class CompanyRequestDTO{
    @NotBlank(message = "Industry must be provided")
    private String industry;

    @NotBlank(message = "Location cannot be empty")
    private String location;

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