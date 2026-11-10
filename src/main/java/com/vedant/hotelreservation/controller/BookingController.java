package com.vedant.hotelreservation.controller;

import com.vedant.hotelreservation.api.BookingDtos.*;
import com.vedant.hotelreservation.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {
	private final BookingService bookings;
	public BookingController(BookingService bookings) { this.bookings = bookings; }
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public BookingResponse create(Authentication authentication, @Valid @RequestBody CreateBookingRequest request) {
		return bookings.response(bookings.create(authentication.getName(), request));
	}
	@GetMapping
	public List<BookingResponse> mine(Authentication authentication) {
		return bookings.forUser(authentication.getName()).stream().map(bookings::response).toList();
	}
	@PostMapping("/{id}/cancel")
	public BookingResponse cancel(Authentication authentication, @PathVariable Long id) {
		return bookings.response(bookings.cancel(authentication.getName(), id));
	}
}
