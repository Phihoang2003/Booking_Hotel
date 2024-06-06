package com.phihoang.lakeside_hotel.controller;

import com.phihoang.lakeside_hotel.exception.NoAvailableRoomException;
import com.phihoang.lakeside_hotel.exception.PhotoRetrievalException;
import com.phihoang.lakeside_hotel.exception.ResourceNotFoundException;
import com.phihoang.lakeside_hotel.model.BookedRoom;
import com.phihoang.lakeside_hotel.model.Room;
import com.phihoang.lakeside_hotel.response.BookingResponse;
import com.phihoang.lakeside_hotel.response.RoomResponse;
import com.phihoang.lakeside_hotel.service.IBookedRoomService;
import com.phihoang.lakeside_hotel.service.IRoomService;
import lombok.RequiredArgsConstructor;

import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import javax.sql.rowset.serial.SerialBlob;
import java.io.IOException;
import java.math.BigDecimal;

import java.sql.Blob;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/rooms")
public class RoomController {
    private final IRoomService roomService;
    private final IBookedRoomService bookingService;

    @PostMapping("/add/new-room")
    public ResponseEntity<RoomResponse> addNewRoom(
            @RequestParam("photo") MultipartFile photo,
            @RequestParam("roomType") String roomType,
            @RequestParam("roomPrice") BigDecimal roomPrice) throws SQLException, IOException {

        Room savedRoom = roomService.addNewRoom(photo, roomType, roomPrice);
        RoomResponse response = new RoomResponse(savedRoom.getId(), savedRoom.getRoomType(),
                savedRoom.getRoomPrice());
        return ResponseEntity.ok(response);
    }
    @GetMapping("/all-rooms")
    public ResponseEntity<List<RoomResponse>> getAllRooms() throws Exception{
        List<Room> rooms=roomService.getAllRooms();
        List<RoomResponse> responses=new ArrayList<>();
        for(Room room:rooms){
            byte[] photoBytes=roomService.getRoomPhotoById(room.getId());
            if(photoBytes!=null && photoBytes.length>0){
                String base64Photo= Base64.encodeBase64String(photoBytes);
                RoomResponse roomResponse=getRoomResponse(room);
                roomResponse.setPhoto(base64Photo);
                responses.add(roomResponse);

            }
        }
        return ResponseEntity.ok(responses);


    }
    @PutMapping("/update/{roomId}")
    public ResponseEntity<RoomResponse> updateRoom(@PathVariable Long roomId,@RequestParam(required = false) String roomType,@RequestParam(required = false) BigDecimal roomPrice,
                                                   @RequestParam(required = false) MultipartFile photo) throws SQLException, IOException {
        byte[] photoBytes=photo!=null && !photo.isEmpty()? photo.getBytes() : roomService.getRoomPhotoById(roomId);
        Blob photoBlob=photoBytes!=null&& photoBytes.length>0?new SerialBlob(photoBytes):null;
        Room updatedRoom=roomService.updateRoom(roomId,roomType,roomPrice,photoBytes);
        updatedRoom.setPhoto(photoBlob);
        RoomResponse roomResponse = getRoomResponse(updatedRoom);
        return ResponseEntity.ok(roomResponse);

    }
    private RoomResponse getRoomResponse(Room room ){
        List<BookedRoom> bookings=getAllBookingsByRoomId(room.getId());
        List<BookingResponse> bookingInfo=bookings.stream().map(booking->new BookingResponse(booking.getBookingId(),
                booking.getCheckInDate(),booking.getCheckOutDate(),booking.getGuestFullName(),booking.getGuestEmail(),booking.getNumOfAdults(),booking.getNumOfChildren(),booking.getBookingConfirmationCode())).toList();
        byte[] photoBytes=null;
        Blob photoBlob=room.getPhoto();
        if (photoBlob != null) {
            try {
                photoBytes = photoBlob.getBytes(1, (int) photoBlob.length());
            } catch (SQLException e) {
                throw new PhotoRetrievalException("Error retrieving photo");
            }
        }
        return new RoomResponse(room.getId(),
                room.getRoomType(), room.getRoomPrice(),
                room.isBooked(), photoBytes, bookingInfo);
    }

    private List<BookedRoom> getAllBookingsByRoomId(Long roomId) {
        return bookingService.getAllBookingsByRoomId(roomId);

    }

    @GetMapping("/room-types")
    public List<String> getAllRoomTypes() {
        return roomService.getAllRoomTypes();
    }

    @GetMapping("/room/{roomId}")
    public ResponseEntity<Optional<RoomResponse>> getRoomById(@PathVariable Long roomId){
        Optional<Room> theRoom=roomService.getRoomById(roomId);
        return theRoom.map(room->{
            RoomResponse roomResponse=getRoomResponse(room);
            return ResponseEntity.ok(Optional.of(roomResponse));
        }).orElseThrow(() -> new ResourceNotFoundException("Sorry, Room not found!"));
    }

    @GetMapping("/available-rooms")
    public ResponseEntity<List<RoomResponse>> getAvailableRooms(@RequestParam("checkInDate")  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkInDate,
                                                                @RequestParam("checkOutDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOutDate,
                                                               @RequestParam("roomType") String roomType) throws SQLException {
        List<Room> availableRooms = roomService.getAvailableRooms(checkInDate, checkOutDate, roomType);
        List<RoomResponse> responses = new ArrayList<>();
        for (Room room : availableRooms) {
            byte[] photoBytes = roomService.getRoomPhotoById(room.getId());
            if (photoBytes != null && photoBytes.length > 0) {
                String base64Photo = Base64.encodeBase64String(photoBytes);
                RoomResponse roomResponse = getRoomResponse(room);
                roomResponse.setPhoto(base64Photo);
                responses.add(roomResponse);
            }
        }
        if(responses.isEmpty()){
            throw new NoAvailableRoomException("No rooms available for the selected dates and room type. Please try again with different dates or room type.");
        }else{
                return ResponseEntity.ok(responses);
        }
    }
    @DeleteMapping("/delete/room/{roomId}")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long roomId){
        roomService.deleteRoomById(roomId);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
