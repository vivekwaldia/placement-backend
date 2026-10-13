package com.college.placement_backend.controller;

import com.college.placement_backend.dto.CompanyRequestDTO;
import com.college.placement_backend.model.Company;
import com.college.placement_backend.model.Job;
import com.college.placement_backend.repository.ApplicationRepository;
import com.college.placement_backend.repository.CompanyRepository;
import com.college.placement_backend.repository.JobRepository;
import jakarta.validation.Valid;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/companies")
public class CompanyController{
    private final CompanyRepository companyRepository;
    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;

    public CompanyController(CompanyRepository companyRepository, JobRepository jobRepository, ApplicationRepository applicationRepository){
        this.companyRepository = companyRepository;
        this.jobRepository = jobRepository;
        this.applicationRepository = applicationRepository;
    }

    @PreAuthorize("hasAuthority('STUDENT')")
    @GetMapping
    public List<Company> getCompanies(){
        return companyRepository.findAll();
    }

    @PreAuthorize("hasAuthority('COMPANY')")
    @CacheEvict(value = "jobs", allEntries = true)
    @PutMapping("/{id}/profile")
    public Company updateCompanyProfile(@PathVariable Integer id, @Valid @RequestBody CompanyRequestDTO companyDTO, Authentication authentication){
        Company company = companyRepository.findById(id).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Company not found")
        );

        if(!company.getEmail().equalsIgnoreCase(authentication.getName())){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only update your own company profile");
        }

        company.setIndustry(companyDTO.getIndustry());
        company.setLocation(companyDTO.getLocation());
        return companyRepository.save(company);
    }

    @Transactional
    @PreAuthorize("hasAuthority('COMPANY')")
    @CacheEvict(value = "jobs", allEntries = true)
    @DeleteMapping("/{id}")
    public Map<String, String> deleteCompany(@PathVariable Integer id, Authentication authentication){
        Company company = companyRepository.findById(id).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Company not found")
        );

        if(!company.getEmail().equalsIgnoreCase(authentication.getName())){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only delete your company account");
        }

        List<Job> companyJobs = jobRepository.findByCompany(company);
        for(Job job : companyJobs){
            applicationRepository.deleteAll(applicationRepository.findByJob(job));
        }
        jobRepository.deleteAll(companyJobs);
        companyRepository.delete(company);

        return Map.of("message", "Company " + id + " and its associated job listings were successfully deleted");
    }
}