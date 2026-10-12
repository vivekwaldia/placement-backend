package com.college.placement_backend.repository;

import com.college.placement_backend.model.Job;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobRepository extends JpaRepository<Job, Integer>{}