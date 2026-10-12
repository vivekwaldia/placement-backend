package com.college.placement_backend.controller;

import com.college.placement_backend.model.Company;
import com.college.placement_backend.repository.CompanyRepository;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.college.placement_backend.dto.CompanyRequestDTO;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/companies")
public class CompanyController{
    private final CompanyRepository companyRepository;

    public CompanyController(CompanyRepository companyRepository){
        this.companyRepository = companyRepository;
    }

    @PreAuthorize("hasAuthority('STUDENT')")
    @GetMapping
    public List<Company> getCompanies(){
        return companyRepository.findAll();
    }

    @PreAuthorize("hasAuthority('COMPANY')")
    @PutMapping("/{id}/profile")
    public Company updateCompanyProfile(@PathVariable Integer id, @Valid @RequestBody CompanyRequestDTO companyDTO){
        Company company = companyRepository.findById(id).orElseThrow(
            () -> new RuntimeException("Company not found")
        );
        company.setIndustry(companyDTO.getIndustry());
        company.setLocation(companyDTO.getLocation());
        return companyRepository.save(company);
    }

    @PreAuthorize("hasAuthority('COMPANY')")
    @DeleteMapping("/{id}")
    public String deleteCompany(@PathVariable Integer id){
        companyRepository.deleteById(id);
        return "Company " + id + " was successfully deleted.";
    }
}