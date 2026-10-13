// Controller = URL Endpoint
// The frontend communicates with the backend using a controller

package com.college.placement_backend.controller;

import com.college.placement_backend.dto.StudentRequestDTO;
import com.college.placement_backend.model.Application;
import com.college.placement_backend.model.Student;
import com.college.placement_backend.repository.StudentRepository;
import com.college.placement_backend.service.PlacementService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

// Notify spring boot that this class handles HTTP Requests
@RestController
// Base URL for the waiter
@RequestMapping("/api/students")
public class StudentController{
    // Connect the controller to the repository
    private final StudentRepository studentRepository;
    private final PlacementService placementService;

    public StudentController(StudentRepository studentRepository, PlacementService placementService){
        this.studentRepository = studentRepository;
        this.placementService = placementService;
    }

    // When someones visits the URL, return data
    @PreAuthorize("hasAuthority('COMPANY')")
    @GetMapping
    public List<Student> getAllStudents(){
        // Fetch all students from MySQL table
        return studentRepository.findAll();
    }

    // Application trigger
    @PreAuthorize("hasAuthority('STUDENT')")
    @PostMapping("/{studentId}/apply/{jobId}") // Triggering an action
    public Application apply(@PathVariable Integer studentId, @PathVariable Integer jobId, Authentication authentication){
        return placementService.applyForJob(studentId, jobId, authentication.getName());
    }

    @PreAuthorize("hasAuthority('STUDENT')")
    @GetMapping("/{studentId}/applications")
    public List<Application> getMyApplications(@PathVariable Integer studentId, Authentication authentication){
        return placementService.getStudentApplications(studentId, authentication.getName());
    }

    // @Valid -> Impose safety rules defined like DecimalMax
    @PreAuthorize("hasAuthority('STUDENT')")
    @PutMapping("/{id}/profile")
    public Student updateStudentProfile(@PathVariable Integer id, @Valid @RequestBody StudentRequestDTO studentDTO, Authentication authentication){
        Student student = studentRepository.findById(id).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found")
        );

        if(!student.getEmail().equalsIgnoreCase(authentication.getName())){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only update your own profile.");
        }

        student.setCgpa(studentDTO.getCgpa());
        student.setSkills(studentDTO.getSkills());
        return studentRepository.save(student);
    }
}