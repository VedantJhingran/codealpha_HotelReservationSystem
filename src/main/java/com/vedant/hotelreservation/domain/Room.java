package com.vedant.hotelreservation.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "rooms", uniqueConstraints = @UniqueConstraint(name = "uk_rooms_number", columnNames = "room_number"))
public class Room {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "room_number", nullable = false, length = 20)
	private String roomNumber;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private RoomCategory category;

	@Column(nullable = false, precision = 10, scale = 2)
	private BigDecimal nightlyRate;

	@Column(nullable = false)
	private int capacity;

	@Column(nullable = false)
	private boolean active = true;

	protected Room() {
	}

	public Room(String roomNumber, RoomCategory category, BigDecimal nightlyRate, int capacity) {
		this.roomNumber = roomNumber;
		this.category = category;
		this.nightlyRate = nightlyRate;
		this.capacity = capacity;
	}

	public void update(String roomNumber, RoomCategory category, BigDecimal nightlyRate, int capacity) {
		this.roomNumber = roomNumber;
		this.category = category;
		this.nightlyRate = nightlyRate;
		this.capacity = capacity;
	}

	public void deactivate() { active = false; }

	public Long getId() { return id; }
	public String getRoomNumber() { return roomNumber; }
	public RoomCategory getCategory() { return category; }
	public BigDecimal getNightlyRate() { return nightlyRate; }
	public int getCapacity() { return capacity; }
	public boolean isActive() { return active; }
}
