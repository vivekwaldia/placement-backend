package com.college.placement_backend.model;

import jakarta.persistence.*;

@Entity
public class Application{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer applicationId;

    @ManyToOne
    // Link to the P.K which is inherited from the user class
    @JoinColumn(name = "user_id")
    private Student student;

    @ManyToOne
    @JoinColumn(name = "job_id")
    private Job job;

    // APPLIED, REVIEWING, ACCEPTED
    private String status;

    public Application() {}

    public Integer getApplicationId(){
        return applicationId;
    }

    public Student getStudent(){
        return student;
    }

    public Job getJob(){
        return job;
    }

    public String getStatus(){
        return status;
    }

    public void setApplicationId(Integer applicationId){
        this.applicationId = applicationId;
    }

    public void setStudent(Student student){
        this.student = student;
    }

    public void setJob(Job job){
        this.job = job;
    }

    public void setStatus(String status){
        this.status = status;
    }
}