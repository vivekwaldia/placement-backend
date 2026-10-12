// DTO: Data Transfer Objects

package com.college.placement_backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class JobRequestDTO{
    @NotBlank(message = "Job title can't be empty")
    private String title;

    @NotNull(message = "Required CGPA must be provided")
    @DecimalMin(value = "0.0", message = "CGPA cannot be negative")
    @DecimalMax(value = "10.0", message = "CGPA cannot exceed 10")
    private Double requiredCgpa;

    @NotNull(message = "Vacancies must be provided")
    @Min(value = 1, message = "There must be at least 1 vacancy")
    private Integer vacancies;

    @NotNull(message = "Company ID is required")
    private Integer companyId;

    public String getTitle(){
        return title;
    }

    public Double getRequiredCgpa(){
        return requiredCgpa;
    }

    public Integer getVacancies(){
        return vacancies;
    }

    public Integer getCompanyId(){
        return companyId;
    }

    public void setTitle(String title){
        this.title = title;
    }

    public void setRequiredCgpa(Double requiredCgpa){
        this.requiredCgpa = requiredCgpa;
    }

    public void setVacancies(Integer vacancies){
        this.vacancies = vacancies;
    }

    public void setCompanyId(Integer companyId){
        this.companyId = companyId;
    }
}