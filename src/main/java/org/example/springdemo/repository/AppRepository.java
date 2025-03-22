package org.example.springdemo.repository;

import org.example.springdemo.model.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AppRepository extends JpaRepository<UserModel, Integer> {
    Optional<UserModel> findByEmail(String email);
}