package com.college.placement_backend.service;

import com.college.placement_backend.exception.IneligibleCGPAException;
import com.college.placement_backend.model.Application;
import com.college.placement_backend.model.Job;
import com.college.placement_backend.model.Student;
import com.college.placement_backend.repository.ApplicationRepository;
import com.college.placement_backend.repository.JobRepository;
import com.college.placement_backend.repository.StudentRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.Set;

// Notify spring boot that this class handles the core business logic
@Service
public class PlacementService{
    private static final Set<String> VALID_STATUSES = Set.of("APPLIED", "REVIEWING", "ACCEPTED", "REJECTED");

    private final ApplicationRepository applicationRepository;
    private final StudentRepository studentRepository;
    private final JobRepository jobRepository;

    public PlacementService(StudentRepository studentRepository, JobRepository jobRepository, ApplicationRepository applicationRepository){
        this.studentRepository = studentRepository;
        this.applicationRepository = applicationRepository;
        this.jobRepository = jobRepository;
    }

    @Transactional
    // Vacancies drop when a student applies for a job, if we don't evict the cache,
    // Redis will keep serving old vacancies to new students
    @CacheEvict(value = "jobs", allEntries = true)
    public Application applyForJob(Integer studentId, Integer jobId, String loggedInEmail){
        Student student = studentRepository.findById(studentId).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found.")
        );

        if(!student.getEmail().equalsIgnoreCase(loggedInEmail)){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only apply using your own student account");
        }

        if(student.getCgpa() == null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Please update your profile with your CGPA before applying.");
        }

        Job job = jobRepository.findById(jobId).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found")
        );

        if(applicationRepository.existsByStudentAndJob(student, job)){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "You have already applied for " + job.getTitle() + ".");
        }

        if(student.getCgpa() < job.getRequiredCgpa()){
            throw new IneligibleCGPAException("Application Denied: Student CGPA (" + student.getCgpa() + ") is lower than required (" + job.getRequiredCgpa() + ")");
        }

        if(job.getVacancies() <= 0){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No vacancies left for " + job.getTitle() + ".");
        }

        job.setVacancies(job.getVacancies()-1);
        jobRepository.save(job);

        Application application = new Application();
        application.setStudent(student);
        application.setJob(job);
        application.setStatus("APPLIED");
        return applicationRepository.save(application);
    }

    public List<Application> getStudentApplications(Integer studentId, String loggedInEmail){
        Student student = studentRepository.findById(studentId).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student doesn't exist.")
        );

        if(!student.getEmail().equalsIgnoreCase(loggedInEmail)){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only view your applications.");
        }

        return applicationRepository.findByStudent(student);
    }

    public List<Application> getJobApplications(Integer jobId, String loggedInCompanyEmail){
        Job job = jobRepository.findById(jobId).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found.")
        );

        if(!job.getCompany().getEmail().equalsIgnoreCase(loggedInCompanyEmail)){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only view job applications for your own company's job.");
        }

        return applicationRepository.findByJob(job);
    }

    @Transactional
    public Application updateApplicationStatus(Integer applicationId, String status, String loggedInCompanyEmail){
        Application application = applicationRepository.findById(applicationId).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found.")
        );

        if(!application.getJob().getCompany().getEmail().equalsIgnoreCase(loggedInCompanyEmail)){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only update applications for your own company's jobs.");
        }

        String normalizedStatus = status != null ? status.trim().toUpperCase() : "";
        
        if(!VALID_STATUSES.contains(normalizedStatus)){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid status. Allowed values: APPLIED, REVIEWING, ACCEPTED, REJECTED.");
        }

        application.setStatus(normalizedStatus);
        return applicationRepository.save(application);
    }
}