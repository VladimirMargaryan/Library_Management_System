package com.app.library.controller;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.app.library.exception.NotFoundException;
import com.app.library.model.Notification;
import com.app.library.model.Role;
import com.app.library.model.User;
import com.app.library.model.UserStatus;
import com.app.library.service.NotificationService;
import com.app.library.service.RoleService;
import com.app.library.service.UserService;
import com.app.library.util.Util;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping(value = "/admin")
public class AdminController {

	private final UserService userService;

	private final NotificationService notificationService;

	private final RoleService roleService;

	private final Util util;

	public AdminController(UserService userService,
						   NotificationService notificationService,
						   RoleService roleService,
						   Util util) {

		this.userService = userService;
		this.notificationService = notificationService;
		this.roleService = roleService;
		this.util = util;
	}

	@GetMapping
	public String adminHome(Model model) {
		User user = userService.getByEmail(SecurityContextHolder.getContext().getAuthentication().getName());
		List<Notification> notifications = notificationService.getAllByUserId(user.getId());

		model.addAttribute("notifications", notifications);
		model.addAttribute("user", user);

		return "admin/admin-home";
	}

	@GetMapping(value = "/manageaccounts")
	public String manageAuthorities(@RequestParam(required = false) String keyword,
									@RequestParam(required = false) String sortBy,
									@RequestParam(required = false) Optional<Integer> page,
			Model model) {

		Page<User> userPage;

		if (sortBy == null || sortBy.isEmpty())
			sortBy = "id";

		Pageable userPagePageable = PageRequest.of(page.orElse(1) - 1, 10, Sort.by(sortBy));
		if (keyword != null && !keyword.isEmpty())
			userPage = userService.search(keyword, userPagePageable);
		else
			userPage = userService.getAll(userPagePageable);

		User user = userService.getByEmail(SecurityContextHolder.getContext().getAuthentication().getName());
		List<Notification> notifications = notificationService.getAllByUserId(user.getId());

		model.addAttribute("notifications", notifications);
		model.addAttribute("pageNumbers", util.pageNumbers(userPage));
		model.addAttribute("userPage", userPage);
		model.addAttribute("keyword", keyword);
		model.addAttribute("sortBy", sortBy);
		return "admin/admin-manage-accounts";
	}

	@GetMapping(value = "/manageaccount/{userId}")
	public String manageAccount(@PathVariable Long userId,
								Model model) throws NotFoundException {

		User user = userService.getById(userId);
		model.addAttribute("user", user);
		return "admin/admin-manage-account";
	}

	@PutMapping(value = "/confirmaccountsettings/{userId}")
	public String confirmAccountChanges(@RequestParam(required = false) String accStatus,
										@RequestParam(required = false) String role,
										@PathVariable Long userId,
			Model model) throws NotFoundException {
		model.addAttribute("role", role);
		model.addAttribute("accStatus", accStatus);
		model.addAttribute("user", userService.getById(userId));
		return "admin/admin-confirm-account-settings";
	}

	@PutMapping(value = "/saveaccountsettings/{userId}")
	public String saveAccountSettings(@RequestParam(required = false) String accStatus,
									  @RequestParam(required = false) String role,
									  @PathVariable Long userId) throws NotFoundException {
		User user = userService.getById(userId);
		Role foundedRole = null;
		if (role != null)
			foundedRole = roleService.getByName(role);

		if (foundedRole != null) {
			Collection<Role> roles = user.getRoles();
			roles.clear();
			roles.add(foundedRole);
			user.setRoles(roles);
		}
		if (accStatus != null) {
			if (accStatus.equals(UserStatus.BLOCKED.toString()))
				user.setStatus(UserStatus.BLOCKED);
			else if (accStatus.equals(UserStatus.VERIFIED.toString()))
				user.setStatus(UserStatus.VERIFIED);
		}
		userService.update(user);
		return "redirect:/admin/accountsettingssaved";
	}

	@GetMapping(value = "/accountsettingssaved")
	public String accountSettingsSaved() {
		return "admin/admin-account-settings-saved";
	}
}
