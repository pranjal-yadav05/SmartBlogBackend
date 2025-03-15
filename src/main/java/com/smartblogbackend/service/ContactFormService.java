package com.smartblogbackend.service;

import com.smartblogbackend.model.ContactFormEntity;
import com.smartblogbackend.repository.ContactFormRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ContactFormService {

    @Autowired
    private ContactFormRepository contactFormRepository;

    public void saveContactForm(ContactFormEntity contactFormEntity) {
        // Here, you can save to the database or process the form data
        contactFormRepository.save(contactFormEntity);
    }
}