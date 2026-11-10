package com.vedant.hotelreservation.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "bookings", indexes = {
		@Index(name = "idx_bookings_room_dates", columnList = "room_id,check_in,check_out,status")
})
public class Booking {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "room_id", nullable = false)
	private Room room;

	@Column(name = "check_in", nullable = false)
	private LocalDate checkIn;

	@Column(name = "check_out", nullable = false)
	private LocalDate checkOut;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private BookingStatus status = BookingStatus.CONFIRMED;

	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	protected Booking() {
	}

	public Booking(User user, Room room, LocalDate checkIn, LocalDate checkOut) {
		this.user = user;
		this.room = room;
		this.checkIn = checkIn;
		this.checkOut = checkOut;
		this.createdAt = Instant.now();
	}

	@PrePersist
	void initializeCreatedAt() {
		if (createdAt == null) {
			createdAt = Instant.now();
		}
	}

	public void cancel() { status = BookingStatus.CANCELLED; }
	public Long getId() { return id; }
	public User getUser() { return user; }
	public Room getRoom() { return room; }
	public LocalDate getCheckIn() { return checkIn; }
	public LocalDate getCheckOut() { return checkOut; }
	public BookingStatus getStatus() { return status; }
	public Instant getCreatedAt() { return createdAt; }
}
