package com.college.placement_backend.repository;

import com.college.placement_backend.model.Student;
import com.college.placement_backend.model.Job;
import com.college.placement_backend.model.Application;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationRepository extends JpaRepository<Application, Integer>{
    boolean existsByStudentAndJob(Student student, Job job);
}