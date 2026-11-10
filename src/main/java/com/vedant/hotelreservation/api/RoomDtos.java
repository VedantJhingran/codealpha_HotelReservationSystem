package com.vedant.hotelreservation.api;

import com.vedant.hotelreservation.domain.RoomCategory;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public final class RoomDtos {
	private RoomDtos() {}
	public record CreateRoomRequest(
			@NotBlank @Size(max = 20) String roomNumber,
			@NotNull RoomCategory category,
			@NotNull @Positive BigDecimal nightlyRate,
			@Positive int capacity) {}
	public record UpdateRoomRequest(
			@NotBlank @Size(max = 20) String roomNumber,
			@NotNull RoomCategory category,
			@NotNull @Positive BigDecimal nightlyRate,
			@Positive int capacity) {}
	public record RoomResponse(Long id, String roomNumber, RoomCategory category,
			BigDecimal nightlyRate, int capacity, boolean active) {}
}
