package com.college.placement_backend.controller;

import com.college.placement_backend.dto.JobRequestDTO;
import com.college.placement_backend.model.Application;
import com.college.placement_backend.model.Company;
import com.college.placement_backend.model.Job;
import com.college.placement_backend.repository.ApplicationRepository;
import com.college.placement_backend.repository.CompanyRepository;
import com.college.placement_backend.repository.JobRepository;
import com.college.placement_backend.service.PlacementService;
import jakarta.validation.Valid;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
// Bridge between internet and java code, translates HTTP calls into methods and variables
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/jobs")
public class JobController{
    private final JobRepository jobRepository;
    private final CompanyRepository companyRepository;
    private final ApplicationRepository applicationRepository;
    private final PlacementService placementService;

    public JobController(JobRepository jobRepository, CompanyRepository companyRepository, ApplicationRepository applicationRepository, PlacementService placementService){
        this.jobRepository = jobRepository;
        this.companyRepository = companyRepository;
        this.applicationRepository = applicationRepository;
        this.placementService = placementService;
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
    public Job postJob(@Valid @RequestBody JobRequestDTO jobDTO, Authentication authentication){
        Company company = companyRepository.findById(jobDTO.getCompanyId()).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Company of ID: " + jobDTO.getCompanyId() + " not found.")
        );

        if(!company.getEmail().equalsIgnoreCase(authentication.getName())){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only post jobs for your own company.");
        }

        Job newJob = new Job();
        newJob.setTitle(jobDTO.getTitle());
        newJob.setRequiredCgpa(jobDTO.getRequiredCgpa());
        newJob.setVacancies(jobDTO.getVacancies());
        newJob.setCompany(company);

        return jobRepository.save(newJob);
    }

    @PreAuthorize("hasAuthority('COMPANY')")
    @GetMapping("/{jobId}/applications")
    public List<Application> getApplicationsForJob(@PathVariable Integer jobId, Authentication authentication) {
        return placementService.getJobApplications(jobId, authentication.getName());
    }

    @PreAuthorize("hasAuthority('COMPANY')")
    @PutMapping("/applications/{applicationId}/status")
    public Application updateApplicationStatus(@PathVariable Integer applicationId, @RequestParam String status, Authentication authentication) {
        return placementService.updateApplicationStatus(applicationId, status, authentication.getName());
    }

    @Transactional
    @CacheEvict(value = "jobs", allEntries = true)
    @PreAuthorize("hasAuthority('COMPANY')")
    // Listen for HTTP delete requests, this uses dynamic path variable
    @DeleteMapping("/{id}")
    // PathVariable tells Spring Boot to directly capture the value from the URL,
    // and assign it our java variable
    public Map<String, String> deleteJob(@PathVariable Integer id, Authentication authentication) {
        Job job = jobRepository.findById(id).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found.")
        );

        if (!job.getCompany().getEmail().equalsIgnoreCase(authentication.getName())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only delete jobs posted by your own company.");
        }

        applicationRepository.deleteAll(applicationRepository.findByJob(job));
        jobRepository.delete(job);
        return Map.of("message", "Job " + id + " was successfully deleted.");
    }
}