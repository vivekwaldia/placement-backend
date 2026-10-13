package com.college.placement_backend.controller;

import com.college.placement_backend.model.Job;
import com.college.placement_backend.repository.JobRepository;
import com.college.placement_backend.dto.JobRequestDTO;
import com.college.placement_backend.model.Company;
import com.college.placement_backend.repository.CompanyRepository;
import jakarta.validation.Valid;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.prepost.PreAuthorize;
// Bridge between internet and java code, translates HTTP calls into methods and variables
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/jobs")
public class JobController{
    private final JobRepository jobRepository;
    private final CompanyRepository companyRepository;

    public JobController(JobRepository jobRepository, CompanyRepository companyRepository){
        this.jobRepository = jobRepository;
        this.companyRepository = companyRepository;
    }

    @GetMapping
    @Cacheable(value = "jobs")
    public List<Job> getAllJobs(){
        return jobRepository.findAll();
    }

    @PostMapping
    @PreAuthorize("hasAuthority('COMPANY')")
    // Automatically clear the old cache when a new job is posted
    @CacheEvict(value = "jobs", allEntries = true)
    public Job postJob(@Valid @RequestBody JobRequestDTO jobDTO){
        Company company = companyRepository.findById(jobDTO.getCompanyId()).orElseThrow(
            () -> new RuntimeException("Company of ID: " + jobDTO.getCompanyId() + " not found.")
        );

        Job newJob = new Job();
        newJob.setTitle(jobDTO.getTitle());
        newJob.setRequiredCgpa(jobDTO.getRequiredCgpa());
        newJob.setVacancies(jobDTO.getVacancies());
        newJob.setCompany(company);

        return jobRepository.save(newJob);
    }

    @CacheEvict(value = "jobs", allEntries = true)
    @PreAuthorize("hasAuthority('COMPANY')")
    // Listen for HTTP delete requests, this uses dynamic path variable
    @DeleteMapping("/{id}")
    // PathVariable tells Spring Boot to directly capture the value from the URL,
    // and assign it our java variable
    public String deleteJob(@PathVariable Integer id){
        jobRepository.deleteById(id);
        return "Job " + id + " was successfully deleted.";
    }
}