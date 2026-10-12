package com.college.placement_backend.service;

import com.college.placement_backend.exception.IneligibleCGPAException;
import com.college.placement_backend.model.Application;
import com.college.placement_backend.model.Job;
import com.college.placement_backend.model.Student;
import com.college.placement_backend.repository.ApplicationRepository;
import com.college.placement_backend.repository.JobRepository;
import com.college.placement_backend.repository.StudentRepository;
import org.springframework.stereotype.Service;

// Notify spring boot that this class handles the core business logic
@Service
public class PlacementService{
    private final ApplicationRepository applicationRepository;
    private final StudentRepository studentRepository;
    private final JobRepository jobRepository;

    public PlacementService(StudentRepository studentRepository, JobRepository jobRepository, ApplicationRepository applicationRepository){
        this.studentRepository = studentRepository;
        this.applicationRepository = applicationRepository;
        this.jobRepository = jobRepository;
    }

    public String applyForJob(Integer studentId, Integer jobId){
        Student student = studentRepository.findById(studentId).orElseThrow(
            () -> new RuntimeException("Student not found.")
        );

        Job job = jobRepository.findById(jobId).orElseThrow(
            () -> new RuntimeException("Job not found.")
        );

        if(applicationRepository.existsByStudentAndJob(student, job)){
            return "Application Denied: You have already applied for " + job.getTitle();
        }

        if(student.getCgpa() < job.getRequiredCgpa()){
            throw new IneligibleCGPAException("Application Denied: Student CGPA (" + student.getCgpa() + ") is lower than required (" + job.getRequiredCgpa() + ")");
        }

        if(job.getVacancies() <= 0){
            return "Application Denied: No vacancies left for " + job.getTitle();
        }

        job.setVacancies(job.getVacancies()-1);
        jobRepository.save(job);

        Application application = new Application();
        application.setStudent(student);
        application.setJob(job);
        application.setStatus("APPLIED");
        applicationRepository.save(application);

        return "Success: " + student.getName() + " applied for " + job.getTitle() + " at " + job.getCompany().getName();
    }
}