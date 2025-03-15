package com.smartblogbackend.repository;


import com.smartblogbackend.model.ContactFormEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContactFormRepository extends JpaRepository<ContactFormEntity, Long> {
    // You can add custom queries if needed
}
