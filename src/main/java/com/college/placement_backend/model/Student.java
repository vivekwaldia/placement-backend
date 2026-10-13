package com.college.placement_backend.model;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.FetchType;
import jakarta.persistence.Entity;
import java.util.List;

@Entity
public class Student extends User{
    private Double cgpa;

    // We cannot store a java array in a single column,
    // ElementCollection tag creates a secondary hidden table,
    // to hold the skills and link them back to the student
    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> skills;

    public Student(){}

    public Student(String name, String email, Double cgpa, List<String> skills){
        super(name, email);
        this.cgpa = cgpa;
        this.skills = skills;
    }

    public Double getCgpa(){
        return cgpa;
    }

    public List<String> getSkills(){
        return skills;
    }

    public void setCgpa(Double cgpa){
        this.cgpa = cgpa;
    }

    public void setSkills(List<String> skills){
        this.skills = skills;
    }
}