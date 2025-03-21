package com.smartblogbackend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;


@Service
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;

    public void sendWelcomeEmail(String toEmail, String name) {
        String subject = "🎉 Welcome to SmartBlog!";
        String logoUrl = "https://res.cloudinary.com/dupcvl7np/image/upload/v1742236573/logo-black_khkfxd.png"; // Replace with actual SmartBlog logo URL
        String body = getHtmlEmailContent(name, logoUrl);

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

    private String getHtmlEmailContent(String name, String logoUrl) {
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

}
