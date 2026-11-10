package com.vedant.hotelreservation.controller;

import com.vedant.hotelreservation.api.AuthDtos.*;
import com.vedant.hotelreservation.domain.User;
import com.vedant.hotelreservation.service.UserService;
import jakarta.servlet.http.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.*;
import org.springframework.security.core.context.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
	private final UserService users;
	private final AuthenticationManager authenticationManager;
	private final SecurityContextRepository securityContextRepository;
	public AuthController(UserService users, AuthenticationManager authenticationManager,
			SecurityContextRepository securityContextRepository) {
		this.users = users; this.authenticationManager = authenticationManager; this.securityContextRepository = securityContextRepository;
	}
	@PostMapping("/register")
	@ResponseStatus(HttpStatus.CREATED)
	public UserResponse register(@Valid @RequestBody RegisterRequest request) {
		return users.response(users.register(request));
	}
	@GetMapping("/csrf")
	public CsrfToken csrfToken(CsrfToken token) {
		return token;
	}
	@PostMapping("/login")
	public UserResponse login(@Valid @RequestBody LoginRequest request, HttpServletRequest servletRequest,
			HttpServletResponse servletResponse) {
		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(request.email().trim().toLowerCase(), request.password()));
		SecurityContext context = SecurityContextHolder.createEmptyContext();
		context.setAuthentication(authentication);
		SecurityContextHolder.setContext(context);
		servletRequest.getSession(true);
		servletRequest.changeSessionId();
		securityContextRepository.saveContext(context, servletRequest, servletResponse);
		return users.response(users.findByEmail(request.email()));
	}
	@PostMapping("/logout")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void logout(HttpServletRequest request) {
		SecurityContextHolder.clearContext();
		HttpSession session = request.getSession(false);
		if (session != null) session.invalidate();
	}
}
