package com.vedant.hotelreservation.service;

import com.vedant.hotelreservation.api.AuthDtos.*;
import com.vedant.hotelreservation.domain.User;
import com.vedant.hotelreservation.exception.ApiException;
import com.vedant.hotelreservation.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
	private final UserRepository users;
	private final PasswordEncoder passwordEncoder;
	public UserService(UserRepository users, PasswordEncoder passwordEncoder) {
		this.users = users; this.passwordEncoder = passwordEncoder;
	}
	@Transactional
	public User register(RegisterRequest request) {
		String email = request.email().trim().toLowerCase();
		if (users.existsByEmailIgnoreCase(email)) throw new ApiException(HttpStatus.CONFLICT, "Email is already registered");
		return users.save(new User(request.fullName().trim(), email,
				passwordEncoder.encode(request.password()), request.phoneNumber()));
	}
	@Transactional(readOnly = true)
	public User findByEmail(String email) {
		return users.findByEmailIgnoreCase(email).orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
	}
	public UserResponse response(User user) {
		return new UserResponse(user.getId(), user.getFullName(), user.getEmail(), user.getPhoneNumber(), user.getRole());
	}
}
