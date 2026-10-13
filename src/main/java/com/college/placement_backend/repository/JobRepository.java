package com.college.placement_backend.repository;

import com.college.placement_backend.model.Company;
import com.college.placement_backend.model.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface JobRepository extends JpaRepository<Job, Integer>{
    // Deleting a company should delete its applications and jobs automatically,
    // without F.K errors, so added a findByCompany method
    List<Job> findByCompany(Company company);
}