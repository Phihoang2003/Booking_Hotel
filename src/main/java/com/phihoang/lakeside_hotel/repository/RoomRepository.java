package com.phihoang.lakeside_hotel.repository;

import com.phihoang.lakeside_hotel.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface RoomRepository extends JpaRepository<Room,Long> {
    @Query("Select distinct r.roomType from Room r")
    List<String> findDistinctRoomTypes();
    @Query("Select r from Room r "+"where r.roomType LIKE %:roomType%"+" and r.id not in (Select b.room.id from BookedRoom b where b.checkInDate<=:checkOutDate and b.checkOutDate>=:checkInDate)")
    List<Room> findAvailableRoomsByDatesAndRoomType(LocalDate checkInDate, LocalDate checkOutDate, String roomType);
}
