package com.app.library.controller;

import com.app.library.exception.NotFoundException;
import com.app.library.model.User;
import com.app.library.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.mail.MessagingException;

@Controller
public class SecurityController {

	@Autowired
	PasswordEncoder pwEncoder;

	@Autowired
	UserService userService;

	@GetMapping(value="/login")
	public String login() {
		return "security/login";
	}

	@GetMapping(value="/logout")
	public String logout() {
		return "security/logout";
	}

	@GetMapping(value="/register")
	public String register(Model model) {
		model.addAttribute("newAccount", new User());
		return "security/register";
	}

	@PostMapping(value="/register/save")
	public String saveNewAccount(User account, Model model) throws NotFoundException, MessagingException {
		User user = userService.getByEmail(account.getEmail());
		if (user == null) {
			userService.save(account);
			return "redirect:/register/accountcreated";
		}else {
			model.addAttribute("title", "Register");
			model.addAttribute("h2", "Email registered");
			model.addAttribute("message", "Email already registered in our system!");
			return "message";
		}
	}

	@GetMapping(value="/register/accountcreated")
	public String accountCreated() {
		return "security/account-created";
	}

	@GetMapping("/verify")
	public String verifyUser(@RequestParam String email, Model model) throws NotFoundException {
		model.addAttribute("title", "Activation");
		model.addAttribute("message", "Your account activated!");
		model.addAttribute("h2", "Success!");
		userService.verifyUserEmail(email);
		return "verifyMessage";
	}

}
