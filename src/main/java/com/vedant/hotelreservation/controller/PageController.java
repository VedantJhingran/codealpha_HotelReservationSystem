package com.vedant.hotelreservation.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {
	@GetMapping({"/", "/home"})
	public String home() { return "home"; }

	@GetMapping("/rooms")
	public String rooms() { return "rooms"; }

	@GetMapping("/rooms/{id}")
	public String roomDetails() { return "room-details"; }

	@GetMapping("/login")
	public String login() { return "login"; }

	@GetMapping("/register")
	public String register() { return "register"; }

	@GetMapping("/bookings")
	public String bookings() { return "bookings"; }

	@GetMapping("/admin")
	public String admin() { return "admin"; }
}
