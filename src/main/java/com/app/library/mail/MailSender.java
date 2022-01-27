package com.app.library.mail;

import com.app.library.config.TemplateEngineConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Component("mailSenderClient")
public class MailSender {

    @Autowired
    private JavaMailSender emailSender;

    @Autowired
    private TemplateEngineConfig templateEngineConfig;

    @Value("${spring.mail.username}")
    private String from;



    @Async
    public void sendSimpleMessage(String to, String subject, String text) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        emailSender.send(message);
    }

    @Async
    public void sendEmailWithAttachments(String to, String subject, String text) throws MessagingException {
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(text, true);
        emailSender.send(message);
    }

    @Async
    public void sendEmailUsingTemplate(String to, String subject, String templateName, Context context) throws MessagingException {
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setTo(to);
        helper.setSubject(subject);
        TemplateEngine templateEngine = templateEngineConfig.emailTemplateEngine();
        String content = templateEngine.process(templateName, context);
        helper.setText(content, true);
        emailSender.send(message);
    }
}
