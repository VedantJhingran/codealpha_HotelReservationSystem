package com.vedant.hotelreservation.service;

import com.vedant.hotelreservation.api.RoomDtos.*;
import com.vedant.hotelreservation.domain.*;
import com.vedant.hotelreservation.repository.RoomRepository;
import com.vedant.hotelreservation.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class RoomService {
	private final RoomRepository rooms;
	public RoomService(RoomRepository rooms) { this.rooms = rooms; }
	@Transactional
	public Room create(CreateRoomRequest request) {
		if (rooms.existsByRoomNumberIgnoreCase(request.roomNumber().trim()))
			throw new ApiException(HttpStatus.CONFLICT, "Room number is already registered");
		return rooms.save(new Room(request.roomNumber().trim(), request.category(), request.nightlyRate(), request.capacity()));
	}
	@Transactional
	public Room update(Long id, UpdateRoomRequest request) {
		Room room = rooms.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Room not found"));
		if (!room.getRoomNumber().equalsIgnoreCase(request.roomNumber().trim())
				&& rooms.existsByRoomNumberIgnoreCase(request.roomNumber().trim()))
			throw new ApiException(HttpStatus.CONFLICT, "Room number is already registered");
		room.update(request.roomNumber().trim(), request.category(), request.nightlyRate(), request.capacity());
		return room;
	}
	@Transactional
	public void delete(Long id) {
		Room room = rooms.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Room not found"));
		room.deactivate();
	}
	@Transactional(readOnly = true)
	public List<Room> findActive(RoomCategory category) {
		return category == null ? rooms.findByActiveTrueOrderByRoomNumber() : rooms.findByActiveTrueAndCategoryOrderByRoomNumber(category);
	}
	@Transactional(readOnly = true)
	public Room getActive(Long id) {
		return rooms.findById(id).filter(Room::isActive)
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Room not found"));
	}
	public RoomResponse response(Room room) {
		return new RoomResponse(room.getId(), room.getRoomNumber(), room.getCategory(), room.getNightlyRate(), room.getCapacity(), room.isActive());
	}
}
