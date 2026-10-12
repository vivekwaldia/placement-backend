package com.college.placement_backend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class Job{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer jobId;

    private String title;
    private Double requiredCgpa;
    private Integer vacancies;

    // Has-a relationship means many to one, one company can have many job postings
    // This automatically created company_id foreign key in Job table
    @ManyToOne
    @JoinColumn(name = "company_id")
    private Company company;

    public Job(){}

    public Job(Integer jobId, String title, Double requiredCgpa, Integer vacancies, Company company){
        this.jobId = jobId;
        this.title = title;
        this.requiredCgpa = requiredCgpa;
        this.vacancies = vacancies;
        this.company = company;
    }

    public Integer getJobId(){
        return jobId;
    }

    public String getTitle(){
        return title;
    }

    public Double getRequiredCgpa(){
        return requiredCgpa;
    }

    public Integer getVacancies(){
        return vacancies;
    }

    public Company getCompany(){
        return company;
    }

    public void setCompany(Company company){
        this.company = company;
    }

    public void setJobId(Integer jobId){
        this.jobId = jobId;
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
}