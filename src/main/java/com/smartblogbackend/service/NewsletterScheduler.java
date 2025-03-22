package com.smartblogbackend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Logger;

@Service
@EnableScheduling
public class NewsletterScheduler {

    private static final Logger logger = Logger.getLogger(NewsletterScheduler.class.getName());
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private SubscriberService subscriberService;

    /**
     * Scheduled method to send weekly newsletter
     * Runs every Sunday at 9:00 AM
     */
    @Scheduled(cron = "0 0 9 * * SUN")
    public void sendWeeklyNewsletter() {
        logger.info("Starting weekly newsletter distribution at " + LocalDateTime.now().format(formatter));
        
        try {
            subscriberService.sendWeeklyNewsletter();
            logger.info("Weekly newsletter distribution completed successfully at " + LocalDateTime.now().format(formatter));
        } catch (Exception e) {
            logger.severe("Error sending weekly newsletter: " + e.getMessage());
        }
    }
} 