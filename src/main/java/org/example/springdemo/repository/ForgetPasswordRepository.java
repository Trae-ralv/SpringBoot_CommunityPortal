package org.example.springdemo.repository;

import org.example.springdemo.model.ForgetPasswordModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ForgetPasswordRepository extends JpaRepository<ForgetPasswordModel, Integer> {
    ForgetPasswordModel findTopByFpUserIdOrderByFpExpiryDesc(Integer fpUserId);
}