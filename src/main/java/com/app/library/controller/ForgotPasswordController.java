package com.app.library.controller;

import com.app.library.exception.BadRequestException;
import com.app.library.exception.NotFoundException;
import com.app.library.model.User;
import com.app.library.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.mail.MessagingException;

import static com.app.library.model.UserStatus.UNVERIFIED;

@Controller
public class ForgotPasswordController {

    @Autowired
    private UserService userService;

    @GetMapping("/forgot_password")
    public String showForgotPasswordForm() {
        return "security/forgot_password_form";
    }

    @PostMapping("/forgot_password")
    public String processForgotPassword(@RequestParam String email,
                                        Model model) {
        try {
            userService.upToResetPassword(email);
            model.addAttribute("message", "We have sent a reset password link to your email. Please check.");

        } catch (MessagingException e) {
            model.addAttribute("error", "Error while sending email");
        } catch (NotFoundException e) {
            model.addAttribute("error", e.getMessage());
        }
        return "security/forgot_password_form";
    }


    @GetMapping("/reset_password")
    public String showResetPasswordForm(@Param(value = "token") String token, Model model) {
        User user = userService.getByResetPasswordToken(token);
        model.addAttribute("token", token);

        if (user == null || user.getStatus().equals(UNVERIFIED)) {
            model.addAttribute("title", "Reset Password");
            model.addAttribute("message", "Invalid Token");
            model.addAttribute("h2", "Error!");
            return "message";
        }

        return "security/reset_password_form";
    }

    @PostMapping("/reset_password")
    public String processResetPassword(@RequestParam String token,
                                       @RequestParam String password,
                                       Model model) {

        User user = userService.getByResetPasswordToken(token);
        model.addAttribute("title", "Reset your password");

        if (user == null || user.getStatus().equals(UNVERIFIED)) {
            model.addAttribute("title", "Reset Password");
            model.addAttribute("message", "Invalid Token");
            model.addAttribute("h2", "Error!");
            return "message";
        } else {
            try {
                userService.resetPassword(user, password);
            } catch (NotFoundException | BadRequestException e) {
                model.addAttribute("error", e.getMessage());
            }
            model.addAttribute("title", "Reset Password");
            model.addAttribute("message", "You have successfully changed your password.");
            model.addAttribute("h2", "Success!");
        }
        return "message";
    }
}
