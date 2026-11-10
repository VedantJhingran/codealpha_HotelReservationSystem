package com.vedant.hotelreservation;

import com.vedant.hotelreservation.api.BookingDtos.CreateBookingRequest;
import com.vedant.hotelreservation.domain.*;
import com.vedant.hotelreservation.exception.ApiException;
import com.vedant.hotelreservation.repository.*;
import com.vedant.hotelreservation.service.BookingService;
import org.junit.jupiter.api.*;
import org.mockito.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookingServiceTest {
	@Mock BookingRepository bookings;
	@Mock RoomRepository rooms;
	@Mock UserRepository users;
	@InjectMocks BookingService service;

	@BeforeEach void setUp() { MockitoAnnotations.openMocks(this); }

	@Test
	void rejectsOverlappingBooking() {
		User user = new User("Test User", "test@example.com", "hash", null);
		Room room = mock(Room.class);
		when(room.getId()).thenReturn(1L);
		when(room.isActive()).thenReturn(true);
		when(users.findByEmailIgnoreCase("test@example.com")).thenReturn(Optional.of(user));
		when(rooms.findByIdForUpdate(1L)).thenReturn(Optional.of(room));
		when(bookings.findOverlappingBookingsForUpdate(anyLong(), any(), any())).thenReturn(List.of(mock(Booking.class)));

		ApiException exception = assertThrows(ApiException.class, () -> service.create("test@example.com",
				new CreateBookingRequest(1L, LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 3))));
		assertEquals(409, exception.getStatus().value());
		verify(bookings, never()).save(any());
	}

	@Test
	void rejectsInvalidDateRange() {
		assertThrows(ApiException.class, () -> service.create("test@example.com",
				new CreateBookingRequest(1L, LocalDate.of(2026, 12, 3), LocalDate.of(2026, 12, 1))));
		verifyNoInteractions(users, rooms, bookings);
	}
}
