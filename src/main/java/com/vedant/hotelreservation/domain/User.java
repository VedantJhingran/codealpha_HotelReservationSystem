package com.vedant.hotelreservation.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "users", uniqueConstraints = @UniqueConstraint(name = "uk_users_email", columnNames = "email"))
public class User {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String fullName;

	@Column(nullable = false, length = 320)
	private String email;

	@Column(nullable = false)
	private String passwordHash;

	private String phoneNumber;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private UserRole role;

	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	protected User() {
	}

	public User(String fullName, String email, String passwordHash, String phoneNumber) {
		this.fullName = fullName;
		this.email = email;
		this.passwordHash = passwordHash;
		this.phoneNumber = phoneNumber;
		this.role = UserRole.USER;
		this.createdAt = Instant.now();
	}

	public User(String fullName, String email, String passwordHash, String phoneNumber, UserRole role) {
		this(fullName, email, passwordHash, phoneNumber);
		this.role = role;
	}

	@PrePersist
	void initializeCreatedAt() {
		if (createdAt == null) {
			createdAt = Instant.now();
		}
	}

	public Long getId() { return id; }
	public String getFullName() { return fullName; }
	public String getEmail() { return email; }
	public String getPasswordHash() { return passwordHash; }
	public String getPhoneNumber() { return phoneNumber; }
	public UserRole getRole() { return role; }
	public Instant getCreatedAt() { return createdAt; }

	public void changePassword(String passwordHash) {
		this.passwordHash = passwordHash;
	}
}
