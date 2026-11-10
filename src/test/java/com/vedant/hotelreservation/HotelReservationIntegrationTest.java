package com.vedant.hotelreservation;

import tools.jackson.databind.ObjectMapper;
import com.vedant.hotelreservation.api.*;
import com.vedant.hotelreservation.domain.RoomCategory;
import com.vedant.hotelreservation.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.mock.web.MockHttpSession;
import java.math.BigDecimal;
import java.time.LocalDate;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class HotelReservationIntegrationTest {
	private static final String TEST_ADMIN_EMAIL = "admin@test.example";
	private static final String TEST_ADMIN_PASSWORD = java.util.UUID.randomUUID().toString();
	@Autowired MockMvc mvc;
	@Autowired ObjectMapper objectMapper;
	@Autowired UserRepository users;
	@Autowired PasswordEncoder passwordEncoder;

	@DynamicPropertySource
	static void adminProperties(DynamicPropertyRegistry registry) {
		registry.add("HOTEL_ADMIN_EMAIL", () -> TEST_ADMIN_EMAIL);
		registry.add("HOTEL_ADMIN_PASSWORD", () -> TEST_ADMIN_PASSWORD);
		registry.add("HOTEL_ADMIN_NAME", () -> "Test Admin");
	}

	@Test
	void registersLogsInCreatesRoomAndPreventsDoubleBooking() throws Exception {
		String suffix = String.valueOf(System.nanoTime());
		String email = "guest" + suffix + "@example.com";
		AuthDtos.RegisterRequest registration = new AuthDtos.RegisterRequest("Guest User", email, "password123", null);
		mvc.perform(post("/api/auth/register").with(csrf()).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(registration)))
				.andExpect(status().isCreated());

		MvcResult login = mvc.perform(post("/api/auth/login").with(csrf()).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new AuthDtos.LoginRequest(email, "password123"))))
				.andExpect(status().isOk()).andReturn();
		MockHttpSession session = (MockHttpSession) login.getRequest().getSession(false);

		RoomDtos.CreateRoomRequest room = new RoomDtos.CreateRoomRequest("R" + suffix, RoomCategory.DELUXE, BigDecimal.valueOf(120), 2);
		MockHttpSession adminSession = adminSession();
		String roomJson = mvc.perform(post("/api/rooms").with(csrf()).session(adminSession).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(room)))
				.andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
		long roomId = objectMapper.readTree(roomJson).get("id").asLong();

		BookingDtos.CreateBookingRequest booking = new BookingDtos.CreateBookingRequest(roomId,
				LocalDate.of(2027, 1, 10), LocalDate.of(2027, 1, 12));
		String bookingJson = mvc.perform(post("/api/bookings").with(csrf()).session(session).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(booking))).andExpect(status().isCreated()).andReturn()
				.getResponse().getContentAsString();
		long bookingId = objectMapper.readTree(bookingJson).get("id").asLong();
		mvc.perform(post("/api/bookings").with(csrf()).session(session).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(booking))).andExpect(status().isConflict());
		mvc.perform(post("/api/bookings/" + bookingId + "/cancel").with(csrf()).session(session))
				.andExpect(status().isOk()).andExpect(jsonPath("$.status").value("CANCELLED"));
		mvc.perform(post("/api/bookings").with(csrf()).session(session).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(booking))).andExpect(status().isCreated());
	}

	@Test
	void rejectsInvalidDateRange() throws Exception {
		mvc.perform(post("/api/rooms/availability").with(csrf()).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new BookingDtos.AvailabilityRequest(
						LocalDate.now().plusDays(2), LocalDate.now().plusDays(1), 1))))
				.andExpect(status().isBadRequest());
	}

	@Test
	void hashesPasswordsAndProtectsAuthenticatedEndpoints() throws Exception {
		String email = "hash" + System.nanoTime() + "@example.com";
		mvc.perform(post("/api/auth/register").with(csrf()).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new AuthDtos.RegisterRequest("Hash User", email, "password123", null))))
				.andExpect(status().isCreated());
		mvc.perform(post("/api/auth/register").with(csrf()).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new AuthDtos.RegisterRequest("Hash User", email, "password123", null))))
				.andExpect(status().isConflict());
		var user = users.findByEmailIgnoreCase(email).orElseThrow();
		org.junit.jupiter.api.Assertions.assertEquals(com.vedant.hotelreservation.domain.UserRole.USER, user.getRole());
		org.junit.jupiter.api.Assertions.assertTrue(user.getPasswordHash().startsWith("$2"));
		org.junit.jupiter.api.Assertions.assertTrue(passwordEncoder.matches("password123", user.getPasswordHash()));
		org.junit.jupiter.api.Assertions.assertFalse(user.getPasswordHash().equals("password123"));

		mvc.perform(get("/api/bookings")).andExpect(status().isUnauthorized());
		mvc.perform(post("/api/rooms").with(csrf()).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new RoomDtos.CreateRoomRequest("u" + System.nanoTime(),
						RoomCategory.STANDARD, BigDecimal.TEN, 1))))
				.andExpect(status().isUnauthorized());
		mvc.perform(post("/api/auth/login").with(csrf()).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new AuthDtos.LoginRequest(email, "wrong-password"))))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void csrfIsRequiredForStateChangingRequests() throws Exception {
		mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new AuthDtos.RegisterRequest("CSRF User",
						"csrf" + System.nanoTime() + "@example.com", "password123", null))))
				.andExpect(status().isForbidden());
		mvc.perform(get("/api/auth/csrf")).andExpect(status().isOk())
				.andExpect(jsonPath("$.token").isString());
	}

	@Test
	void verifiesAvailabilityListingBookingsAndLogout() throws Exception {
		mvc.perform(get("/api/rooms")).andExpect(status().isOk());
		mvc.perform(post("/api/rooms/availability").with(csrf()).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new BookingDtos.AvailabilityRequest(
						LocalDate.now().plusDays(10), LocalDate.now().plusDays(12), 1))))
				.andExpect(status().isOk());

		String email = "logout" + System.nanoTime() + "@example.com";
		mvc.perform(post("/api/auth/register").with(csrf()).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new AuthDtos.RegisterRequest("Logout User", email, "password123", null))))
				.andExpect(status().isCreated());
		MvcResult login = mvc.perform(post("/api/auth/login").with(csrf()).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new AuthDtos.LoginRequest(email, "password123"))))
				.andExpect(status().isOk()).andReturn();
		MockHttpSession session = (MockHttpSession) login.getRequest().getSession(false);
		mvc.perform(get("/api/bookings").session(session)).andExpect(status().isOk()).andExpect(content().json("[]"));
		mvc.perform(post("/api/auth/logout").with(csrf()).session(session)).andExpect(status().isNoContent());
		mvc.perform(get("/api/bookings").session(session)).andExpect(status().isUnauthorized());
	}

	@Test
	void onlyAdminCanManageRooms() throws Exception {
		MockHttpSession admin = adminSession();
		String suffix = String.valueOf(System.nanoTime());
		String roomJson = mvc.perform(post("/api/rooms").with(csrf()).session(admin).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new RoomDtos.CreateRoomRequest("A" + suffix,
						RoomCategory.STANDARD, BigDecimal.TEN, 1))))
				.andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
		long roomId = objectMapper.readTree(roomJson).get("id").asLong();

		String email = "room-user" + suffix + "@example.com";
		mvc.perform(post("/api/auth/register").with(csrf()).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new AuthDtos.RegisterRequest("Room User", email, "password123", null))))
				.andExpect(status().isCreated());
		MockHttpSession user = (MockHttpSession) mvc.perform(post("/api/auth/login").with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new AuthDtos.LoginRequest(email, "password123"))))
				.andExpect(status().isOk()).andReturn().getRequest().getSession(false);

		RoomDtos.UpdateRoomRequest update = new RoomDtos.UpdateRoomRequest("B" + suffix,
				RoomCategory.SUITE, BigDecimal.valueOf(20), 2);
		mvc.perform(put("/api/rooms/" + roomId).with(csrf()).session(user).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(update))).andExpect(status().isForbidden());
		mvc.perform(delete("/api/rooms/" + roomId).with(csrf()).session(user))
				.andExpect(status().isForbidden());
		mvc.perform(put("/api/rooms/" + roomId).with(csrf()).session(admin).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(update))).andExpect(status().isOk());
		mvc.perform(delete("/api/rooms/" + roomId).with(csrf()).session(admin))
				.andExpect(status().isNoContent());
		mvc.perform(get("/api/rooms/" + roomId)).andExpect(status().isNotFound());
	}

	@Test
	void usersCanOnlyCancelTheirOwnBookings() throws Exception {
		String suffix = String.valueOf(System.nanoTime());
		MockHttpSession admin = adminSession();
		String roomJson = mvc.perform(post("/api/rooms").with(csrf()).session(admin).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new RoomDtos.CreateRoomRequest("O" + suffix,
						RoomCategory.STANDARD, BigDecimal.TEN, 1))))
				.andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
		long roomId = objectMapper.readTree(roomJson).get("id").asLong();
		MockHttpSession owner = registerAndLogin("owner" + suffix + "@example.com");
		String bookingJson = mvc.perform(post("/api/bookings").with(csrf()).session(owner).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new BookingDtos.CreateBookingRequest(roomId,
						LocalDate.of(2028, 2, 1), LocalDate.of(2028, 2, 3)))))
				.andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
		long bookingId = objectMapper.readTree(bookingJson).get("id").asLong();
		MockHttpSession otherUser = registerAndLogin("other" + suffix + "@example.com");
		mvc.perform(post("/api/bookings/" + bookingId + "/cancel").with(csrf()).session(otherUser))
				.andExpect(status().isForbidden());
	}

	private MockHttpSession registerAndLogin(String email) throws Exception {
		mvc.perform(post("/api/auth/register").with(csrf()).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new AuthDtos.RegisterRequest("Test User", email, "password123", null))))
				.andExpect(status().isCreated());
		return (MockHttpSession) mvc.perform(post("/api/auth/login").with(csrf()).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new AuthDtos.LoginRequest(email, "password123"))))
				.andExpect(status().isOk()).andReturn().getRequest().getSession(false);
	}

	private MockHttpSession adminSession() throws Exception {
		return (MockHttpSession) mvc.perform(post("/api/auth/login").with(csrf()).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new AuthDtos.LoginRequest(TEST_ADMIN_EMAIL, TEST_ADMIN_PASSWORD))))
				.andExpect(status().isOk()).andReturn().getRequest().getSession(false);
	}
}
