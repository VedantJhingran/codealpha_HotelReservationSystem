package com.vedant.hotelreservation.repository;

import com.vedant.hotelreservation.domain.Room;
import com.vedant.hotelreservation.domain.RoomCategory;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long> {
	List<Room> findByActiveTrueAndCategoryOrderByRoomNumber(RoomCategory category);
	List<Room> findByActiveTrueOrderByRoomNumber();
	boolean existsByRoomNumberIgnoreCase(String roomNumber);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select r from Room r where r.id = :id")
	java.util.Optional<Room> findByIdForUpdate(@Param("id") Long id);
}
