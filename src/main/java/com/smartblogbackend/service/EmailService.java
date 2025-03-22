package com.smartblogbackend.service;

import com.smartblogbackend.model.BlogPost;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import java.time.format.DateTimeFormatter;
import java.util.List;


@Service
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;

    private static final String LOGO_URL = "https://res.cloudinary.com/dupcvl7np/image/upload/v1742236573/logo-black_khkfxd.png";

    public void sendWelcomeEmail(String toEmail, String name) {
        String subject = "🎉 Welcome to SmartBlog!";
        String body = getWelcomeEmailContent(name, LOGO_URL);

        sendHtmlEmail(toEmail, subject, body);
    }

    public void sendSubscriptionConfirmation(String toEmail) {
        String subject = "✅ You're subscribed to SmartBlog Weekly!";
        String body = getSubscriptionConfirmationEmailContent(LOGO_URL);

        sendHtmlEmail(toEmail, subject, body);
    }

    public void sendWeeklyNewsletter(String toEmail, List<BlogPost> topPosts) {
        String subject = "📚 This Week's Top 5 Posts from SmartBlog";
        String body = getWeeklyNewsletterContent(topPosts, LOGO_URL);

        sendHtmlEmail(toEmail, subject, body);
    }

    private void sendHtmlEmail(String toEmail, String subject, String body) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(body, true); // Enable HTML

            mailSender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace(); // Handle error properly in production
        }
    }

    private String getWelcomeEmailContent(String name, String logoUrl) {
        return "<div style='font-family: Arial, sans-serif; text-align: center; padding: 20px; background-color: #f4f4f4;'>"
                + "<div style='max-width: 600px; margin: auto; background: white; padding: 30px; border-radius: 10px; box-shadow: 0 4px 12px rgba(0,0,0,0.1);'>"
                + "<img src='" + logoUrl + "' alt='SmartBlog Logo' style='max-width: 180px; margin-bottom: 20px;' />"

                + "<h2 style='color: #333;'>Welcome to <span style='color: #007bff;'>SmartBlog</span>, " + name + "!</h2>"

                + "<p style='color: #555; font-size: 16px;'>We're excited to have you on board. 🌟<br>"
                + "Start sharing your thoughts, engaging with the community, and exploring amazing content.</p>"

                + "<a href='https://smart-blog-one.vercel.app' style='display: inline-block; background-color: #007bff; color: white; "
                + "padding: 12px 25px; text-decoration: none; font-size: 16px; border-radius: 5px; margin-top: 20px;'>Start Writing</a>"

                + "<p style='color: #777; font-size: 14px; margin-top: 20px;'>If you did not sign up for SmartBlog, please ignore this email or contact our support.</p>"

                + "<hr style='border: none; border-top: 1px solid #ddd; margin: 20px 0;' />"
                + "<p style='color: #999; font-size: 12px;'>Need help? Contact me at <a href='mailto:yadavpranjal2105@gmail.com' style='color: #007bff; text-decoration: none;'>dev support</a></p>"

                + "</div></div>";
    }

    private String getSubscriptionConfirmationEmailContent(String logoUrl) {
        return "<div style='font-family: Arial, sans-serif; text-align: center; padding: 20px; background-color: #f4f4f4;'>"
                + "<div style='max-width: 600px; margin: auto; background: white; padding: 30px; border-radius: 10px; box-shadow: 0 4px 12px rgba(0,0,0,0.1);'>"
                + "<img src='" + logoUrl + "' alt='SmartBlog Logo' style='max-width: 180px; margin-bottom: 20px;' />"

                + "<h2 style='color: #333;'>You're Subscribed to <span style='color: #007bff;'>SmartBlog Weekly</span>!</h2>"

                + "<p style='color: #555; font-size: 16px;'>Thank you for subscribing to our weekly newsletter. 🌟<br>"
                + "Every week, we'll send you the top 5 most engaging posts from our community.</p>"

                + "<p style='color: #555; font-size: 16px;'>Your first newsletter will arrive next week!</p>"

                + "<a href='https://smart-blog-one.vercel.app' style='display: inline-block; background-color: #007bff; color: white; "
                + "padding: 12px 25px; text-decoration: none; font-size: 16px; border-radius: 5px; margin-top: 20px;'>Visit SmartBlog</a>"

                + "<p style='color: #777; font-size: 14px; margin-top: 20px;'>If you did not subscribe to SmartBlog Weekly, "
                + "you can unsubscribe by clicking <a href='https://smart-blog-one.vercel.app/unsubscribe' style='color: #007bff; text-decoration: none;'>here</a>.</p>"

                + "<hr style='border: none; border-top: 1px solid #ddd; margin: 20px 0;' />"
                + "<p style='color: #999; font-size: 12px;'>Need help? Contact me at <a href='mailto:yadavpranjal2105@gmail.com' style='color: #007bff; text-decoration: none;'>dev support</a></p>"

                + "</div></div>";
    }

    private String getWeeklyNewsletterContent(List<BlogPost> topPosts, String logoUrl) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy");
        StringBuilder postsHtml = new StringBuilder();

        for (int i = 0; i < Math.min(topPosts.size(), 5); i++) {
            BlogPost post = topPosts.get(i);
            String formattedDate = post.getCreatedAt().format(formatter);
            String excerpt = post.getContent().length() > 150 
                ? post.getContent().substring(0, 150) + "..." 
                : post.getContent();
            
            postsHtml.append("<div style='margin-bottom: 25px; border-bottom: 1px solid #eee; padding-bottom: 20px;'>");
            
            // Post title with link
            postsHtml.append("<h3 style='margin-bottom: 10px;'><a href='https://smart-blog-one.vercel.app/blog/")
                   .append(post.getId())
                   .append("' style='color: #0066cc; text-decoration: none;'>")
                   .append(post.getTitle())
                   .append("</a></h3>");
            
            // Post image if available
            if (post.getImageUrl() != null && !post.getImageUrl().isEmpty()) {
                postsHtml.append("<img src='")
                       .append(post.getImageUrl())
                       .append("' alt='")
                       .append(post.getTitle())
                       .append("' style='width: 100%; max-height: 200px; object-fit: cover; border-radius: 5px; margin-bottom: 15px;' />");
            }
            
            // Post excerpt
            postsHtml.append("<p style='color: #444; margin-bottom: 10px;'>")
                   .append(excerpt)
                   .append("</p>");
            
            // Author and date
            postsHtml.append("<p style='color: #777; font-size: 13px;'>By: ")
                   .append(post.getAuthor().getName())
                   .append(" • ")
                   .append(formattedDate)
                   .append("</p>");
            
            // Read more link
            postsHtml.append("<a href='https://smart-blog-one.vercel.app/blog/")
                   .append(post.getId())
                   .append("' style='color: #0066cc; text-decoration: none; font-weight: bold;'>Read more →</a>");
            
            postsHtml.append("</div>");
        }

        return "<div style='font-family: Arial, sans-serif; padding: 20px; background-color: #f4f4f4;'>"
                + "<div style='max-width: 600px; margin: auto; background: white; padding: 30px; border-radius: 10px; box-shadow: 0 4px 12px rgba(0,0,0,0.1);'>"
                + "<div style='text-align: center; margin-bottom: 20px;'>"
                + "<img src='" + logoUrl + "' alt='SmartBlog Logo' style='max-width: 180px;' />"
                + "</div>"

                + "<h1 style='color: #333; text-align: center; margin-bottom: 30px;'>This Week's Top Posts</h1>"

                + postsHtml.toString()

                + "<div style='background-color: #f9f9f9; padding: 20px; border-radius: 5px; margin-top: 30px;'>"
                + "<h3 style='color: #333; margin-top: 0;'>Want to contribute?</h3>"
                + "<p style='color: #555;'>Share your knowledge and insights with our growing community!</p>"
                + "<a href='https://smart-blog-one.vercel.app/create' style='display: inline-block; background-color: #007bff; color: white; "
                + "padding: 10px 20px; text-decoration: none; font-size: 16px; border-radius: 5px; margin-top: 10px;'>Write a Post</a>"
                + "</div>"

                + "<hr style='border: none; border-top: 1px solid #ddd; margin: 30px 0 20px;' />"
                
                + "<p style='color: #777; font-size: 14px; text-align: center;'>"
                + "You're receiving this email because you subscribed to SmartBlog Weekly. "
                + "<a href='https://smart-blog-one.vercel.app/unsubscribe' style='color: #007bff; text-decoration: none;'>Unsubscribe</a>"
                + "</p>"

                + "<p style='color: #999; font-size: 12px; text-align: center;'>"
                + "© 2024 SmartBlog • <a href='mailto:yadavpranjal2105@gmail.com' style='color: #007bff; text-decoration: none;'>Contact</a>"
                + "</p>"

                + "</div></div>";
    }
}
