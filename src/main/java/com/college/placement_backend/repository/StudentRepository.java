// A repository simply acts like a database communicator,
// we get tons of built-in commands by extending JpaRepository

package com.college.placement_backend.repository;

import com.college.placement_backend.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;

// JpaRepo requires <Model Class, Data Type of P.K>
public interface StudentRepository extends JpaRepository<Student, Integer>{}