package com.vedant.hotelreservation.repository;

import com.vedant.hotelreservation.domain.*;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
	@Query("""
		select b from Booking b
		where b.room.id = :roomId
		  and b.status = com.vedant.hotelreservation.domain.BookingStatus.CONFIRMED
		  and b.checkIn < :checkOut and b.checkOut > :checkIn
		""")
	List<Booking> findOverlappingBookings(@Param("roomId") Long roomId,
			@Param("checkIn") LocalDate checkIn, @Param("checkOut") LocalDate checkOut);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("""
		select b from Booking b
		where b.room.id = :roomId
		  and b.status = com.vedant.hotelreservation.domain.BookingStatus.CONFIRMED
		  and b.checkIn < :checkOut and b.checkOut > :checkIn
		""")
	List<Booking> findOverlappingBookingsForUpdate(@Param("roomId") Long roomId,
			@Param("checkIn") LocalDate checkIn, @Param("checkOut") LocalDate checkOut);

	List<Booking> findByUserIdOrderByCreatedAtDesc(Long userId);
}
