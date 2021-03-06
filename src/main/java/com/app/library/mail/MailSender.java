package com.app.library.mail;

import com.app.library.config.TemplateEngineConfig;
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
import java.io.File;
import java.util.List;

@Component("mailSenderClient")
public class MailSender {

    private final JavaMailSender emailSender;

    private final TemplateEngineConfig templateEngineConfig;

    @Value("${spring.mail.username}")
    private String from;

    public MailSender(JavaMailSender emailSender, TemplateEngineConfig templateEngineConfig) {
        this.emailSender = emailSender;
        this.templateEngineConfig = templateEngineConfig;
    }


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
    public void sendEmailWithAttachments(String to, String subject, String text, List<File> files) throws MessagingException {
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(to);
        helper.setSubject(subject);
        files.forEach(file -> {
            try {
                helper.addAttachment(file.getName(), file);
            } catch (MessagingException e) {
                e.printStackTrace();
            }
        });
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
