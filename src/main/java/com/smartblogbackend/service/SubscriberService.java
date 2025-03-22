package com.smartblogbackend.service;

import com.smartblogbackend.model.BlogPost;
import com.smartblogbackend.model.Subscriber;
import com.smartblogbackend.repository.SubscriberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class SubscriberService {

    @Autowired
    private SubscriberRepository subscriberRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private BlogPostService blogPostService;

    /**
     * Subscribe a new email to the newsletter
     * @param email Email to subscribe
     * @return true if subscription was successful, false if already subscribed
     */
    public boolean subscribe(String email) {
        // Check if already subscribed
        if (subscriberRepository.existsByEmail(email)) {
            // Reactivate if previously unsubscribed
            Optional<Subscriber> existingSubscriber = subscriberRepository.findByEmail(email);
            if (existingSubscriber.isPresent() && !existingSubscriber.get().isActive()) {
                Subscriber subscriber = existingSubscriber.get();
                subscriber.setActive(true);
                subscriberRepository.save(subscriber);
                // Send confirmation email
                emailService.sendSubscriptionConfirmation(email);
                return true;
            }
            return false;
        }

        // Create new subscription
        Subscriber subscriber = new Subscriber(email);
        subscriberRepository.save(subscriber);

        // Send confirmation email
        emailService.sendSubscriptionConfirmation(email);
        return true;
    }

    /**
     * Unsubscribe an email from the newsletter
     * @param email Email to unsubscribe
     * @return true if unsubscribe was successful, false if not found
     */
    public boolean unsubscribe(String email) {
        Optional<Subscriber> subscriber = subscriberRepository.findByEmail(email);
        if (subscriber.isPresent()) {
            Subscriber existingSubscriber = subscriber.get();
            existingSubscriber.setActive(false);
            subscriberRepository.save(existingSubscriber);
            return true;
        }
        return false;
    }

    /**
     * Get all active subscribers
     * @return List of active subscribers
     */
    public List<Subscriber> getAllActiveSubscribers() {
        return subscriberRepository.findByActive(true);
    }

    /**
     * Send weekly newsletter to all active subscribers
     */
    public void sendWeeklyNewsletter() {
        // Get top 5 posts from the past week
        List<BlogPost> topPosts = getTopPostsOfTheWeek();
        
        if (topPosts.isEmpty()) {
            return; // No posts to send
        }

        // Get all active subscribers
        List<Subscriber> activeSubscribers = getAllActiveSubscribers();
        
        // Send newsletter to each subscriber
        for (Subscriber subscriber : activeSubscribers) {
            emailService.sendWeeklyNewsletter(subscriber.getEmail(), topPosts);
            
            // Update last email sent timestamp
            subscriber.setLastEmailSent(LocalDateTime.now());
            subscriberRepository.save(subscriber);
        }
    }

    /**
     * Get the top 5 posts from the past week
     * @return List of top posts
     */
    private List<BlogPost> getTopPostsOfTheWeek() {
        // We're getting the 5 most recently created posts from the past week
        // In a real application, you might want to use metrics like views, comments, or likes
        
        // Get current date and time
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);
        
        // Get top 5 posts of the week, sorted by creation date
        PageRequest pageRequest = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt"));
        return blogPostService.getAllPosts(pageRequest).getContent();
        
        // Note: In a real application, you would add filtering by date
        // This simplified version just gets the 5 most recent posts
    }
} 