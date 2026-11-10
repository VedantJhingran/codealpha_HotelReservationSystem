package com.vedant.hotelreservation.api;

import com.vedant.hotelreservation.domain.BookingStatus;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

public final class BookingDtos {
	private BookingDtos() {}
	public record AvailabilityRequest(@NotNull @FutureOrPresent LocalDate checkIn,
			@NotNull LocalDate checkOut, @Positive int guests) {}
	public record CreateBookingRequest(@NotNull Long roomId,
			@NotNull @FutureOrPresent LocalDate checkIn, @NotNull LocalDate checkOut) {}
	public record BookingResponse(Long id, Long roomId, String roomNumber, LocalDate checkIn,
			LocalDate checkOut, BookingStatus status, Long userId) {}
}
