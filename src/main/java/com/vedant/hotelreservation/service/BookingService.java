package com.vedant.hotelreservation.service;

import com.vedant.hotelreservation.api.BookingDtos.*;
import com.vedant.hotelreservation.domain.*;
import com.vedant.hotelreservation.exception.ApiException;
import com.vedant.hotelreservation.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BookingService {
	private final BookingRepository bookings;
	private final RoomRepository rooms;
	private final UserRepository users;
	public BookingService(BookingRepository bookings, RoomRepository rooms, UserRepository users) {
		this.bookings = bookings; this.rooms = rooms; this.users = users;
	}
	@Transactional(readOnly = true)
	public List<Room> available(AvailabilityRequest request) {
		validateDates(request.checkIn(), request.checkOut());
		return rooms.findByActiveTrueOrderByRoomNumber().stream()
				.filter(room -> room.getCapacity() >= request.guests())
				.filter(room -> bookings.findOverlappingBookings(room.getId(), request.checkIn(), request.checkOut()).isEmpty())
				.collect(Collectors.toList());
	}
	@Transactional
	public Booking create(String email, CreateBookingRequest request) {
		validateDates(request.checkIn(), request.checkOut());
		User user = users.findByEmailIgnoreCase(email).orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "User not found"));
		Room room = rooms.findByIdForUpdate(request.roomId()).filter(Room::isActive)
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Room not found"));
		if (!bookings.findOverlappingBookingsForUpdate(room.getId(), request.checkIn(), request.checkOut()).isEmpty())
			throw new ApiException(HttpStatus.CONFLICT, "Room is not available for the selected dates");
		return bookings.save(new Booking(user, room, request.checkIn(), request.checkOut()));
	}
	@Transactional(readOnly = true)
	public List<Booking> forUser(String email) {
		User user = users.findByEmailIgnoreCase(email).orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "User not found"));
		return bookings.findByUserIdOrderByCreatedAtDesc(user.getId());
	}
	@Transactional
	public Booking cancel(String email, Long id) {
		Booking booking = bookings.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Booking not found"));
		if (!booking.getUser().getEmail().equalsIgnoreCase(email)) throw new ApiException(HttpStatus.FORBIDDEN, "You cannot cancel this booking");
		if (booking.getStatus() == BookingStatus.CANCELLED) throw new ApiException(HttpStatus.CONFLICT, "Booking is already cancelled");
		booking.cancel();
		return booking;
	}
	private void validateDates(LocalDate checkIn, LocalDate checkOut) {
		if (!checkOut.isAfter(checkIn)) throw new ApiException(HttpStatus.BAD_REQUEST, "Check-out must be after check-in");
	}
	public BookingResponse response(Booking booking) {
		return new BookingResponse(booking.getId(), booking.getRoom().getId(), booking.getRoom().getRoomNumber(),
				booking.getCheckIn(), booking.getCheckOut(), booking.getStatus(), booking.getUser().getId());
	}
}
