package com.vedant.hotelreservation.service;

import com.vedant.hotelreservation.domain.*;
import com.vedant.hotelreservation.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminInitializerTest {
	private final UserRepository users = mock(UserRepository.class);
	private final PasswordEncoder encoder = new BCryptPasswordEncoder();
	private final AdminInitializer initializer = new AdminInitializer();

	@Test
	void resetsPasswordOnlyForExistingAdminWhenExplicitlyEnabled() {
		User admin = new User("Admin", "admin@example.com", encoder.encode("old-password"), null, UserRole.ADMIN);
		when(users.findByEmailIgnoreCase("admin@example.com")).thenReturn(Optional.of(admin));

		initializer.initializeAdmin(users, encoder, " ADMIN@example.com ", "new-password",
				"Changed Name", true);

		assertTrue(encoder.matches("new-password", admin.getPasswordHash()));
		assertFalse(encoder.matches("old-password", admin.getPasswordHash()));
		assertEquals("Admin", admin.getFullName());
		assertEquals(UserRole.ADMIN, admin.getRole());
		verify(users).save(admin);
	}

	@Test
	void doesNotChangeExistingAdminWhenResetIsDisabled() {
		String originalHash = encoder.encode("old-password");
		User admin = new User("Admin", "admin@example.com", originalHash, null, UserRole.ADMIN);
		when(users.findByEmailIgnoreCase("admin@example.com")).thenReturn(Optional.of(admin));

		initializer.initializeAdmin(users, encoder, "admin@example.com", "new-password",
				"Admin", false);

		assertEquals(originalHash, admin.getPasswordHash());
		verify(users, never()).save(any());
	}

	@Test
	void refusesToResetNormalUser() {
		User user = new User("User", "admin@example.com", encoder.encode("password"), null, UserRole.USER);
		when(users.findByEmailIgnoreCase("admin@example.com")).thenReturn(Optional.of(user));

		assertThrows(IllegalStateException.class, () -> initializer.initializeAdmin(users, encoder,
				"admin@example.com", "new-password", "Admin", true));
		verify(users, never()).save(any());
		assertTrue(encoder.matches("password", user.getPasswordHash()));
	}

	@Test
	void refusesToCreateAccountDuringReset() {
		when(users.findByEmailIgnoreCase("admin@example.com")).thenReturn(Optional.empty());

		assertThrows(IllegalStateException.class, () -> initializer.initializeAdmin(users, encoder,
				"admin@example.com", "new-password", "Admin", true));
		verify(users, never()).save(any());
	}
}
