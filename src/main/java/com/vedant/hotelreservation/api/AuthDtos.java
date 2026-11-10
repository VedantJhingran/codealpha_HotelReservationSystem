package com.vedant.hotelreservation.api;

import jakarta.validation.constraints.*;
import com.vedant.hotelreservation.domain.UserRole;

public final class AuthDtos {
	private AuthDtos() {}
	public record RegisterRequest(
			@NotBlank @Size(max = 120) String fullName,
			@NotBlank @Email @Size(max = 320) String email,
			@NotBlank @Size(min = 8, max = 72) String password,
			@Size(max = 30) String phoneNumber) {}
	public record LoginRequest(@NotBlank @Email String email, @NotBlank String password) {}
	public record UserResponse(Long id, String fullName, String email, String phoneNumber, UserRole role) {}
}
