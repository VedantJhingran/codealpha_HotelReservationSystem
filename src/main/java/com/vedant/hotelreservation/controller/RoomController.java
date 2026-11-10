package com.vedant.hotelreservation.controller;

import com.vedant.hotelreservation.api.BookingDtos.AvailabilityRequest;
import com.vedant.hotelreservation.api.RoomDtos.*;
import com.vedant.hotelreservation.domain.RoomCategory;
import com.vedant.hotelreservation.service.*;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import java.util.List;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {
	private final RoomService rooms;
	private final BookingService bookings;
	public RoomController(RoomService rooms, BookingService bookings) { this.rooms = rooms; this.bookings = bookings; }
	@GetMapping
	public List<RoomResponse> list(@RequestParam(required = false) RoomCategory category) {
		return rooms.findActive(category).stream().map(rooms::response).toList();
	}
	@GetMapping("/{id}")
	public RoomResponse get(@PathVariable Long id) { return rooms.response(rooms.getActive(id)); }
	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	@ResponseStatus(HttpStatus.CREATED)
	public RoomResponse create(@Valid @RequestBody CreateRoomRequest request) { return rooms.response(rooms.create(request)); }
	@PostMapping("/availability")
	public List<RoomResponse> availability(@Valid @RequestBody AvailabilityRequest request) {
		return bookings.available(request).stream().map(rooms::response).toList();
	}
	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public RoomResponse update(@PathVariable Long id, @Valid @RequestBody UpdateRoomRequest request) {
		return rooms.response(rooms.update(id, request));
	}
	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long id) { rooms.delete(id); }
}
