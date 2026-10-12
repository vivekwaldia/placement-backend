package com.college.placement_backend.repository;

import com.college.placement_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer>{
    // Optional returns empty container if email is not found, instead of returning null (null might cause crashes)
    // Spring data JPA automatically writes the query to search the user with the specified email
    Optional<User> findByEmail(String email);
}