package com.phihoang.lakeside_hotel.controller;

import com.phihoang.lakeside_hotel.exception.InvalidBookingRequestException;
import com.phihoang.lakeside_hotel.exception.ResourceNotFoundException;
import com.phihoang.lakeside_hotel.model.BookedRoom;
import com.phihoang.lakeside_hotel.model.Room;
import com.phihoang.lakeside_hotel.response.BookingResponse;
import com.phihoang.lakeside_hotel.response.RoomResponse;
import com.phihoang.lakeside_hotel.service.IBookedRoomService;
import com.phihoang.lakeside_hotel.service.IRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/bookings")
public class BookingController {
    private final IBookedRoomService bookingService;
    private final IRoomService roomService;

    @PostMapping("/room/{roomId}/booking")
    public ResponseEntity<?> saveBooking(@PathVariable Long roomId , @RequestBody BookedRoom bookingRequest){
        try {
            System.out.println("Booking: "+bookingRequest);
            String confirmationCode = bookingService.saveBooking(roomId, bookingRequest);
            return ResponseEntity.ok(
                    "Room booked successfully, Your booking confirmation code is :"+confirmationCode);
        }catch(InvalidBookingRequestException ex){
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }
    @GetMapping("/all-bookings")
    public ResponseEntity<List<BookingResponse>> getAllBookings(){
        List<BookedRoom> bookings=bookingService.getAllBookings();
        List<BookingResponse> responses=new ArrayList<>();
        for(BookedRoom booking:bookings){
            responses.add(getBookingResponse(booking));
        }
        return ResponseEntity.ok(responses);
    }
    @GetMapping("/user/{email}/bookings")
    public ResponseEntity<List<BookingResponse>> getBookingsByUserEmail(@PathVariable String email) {
        List<BookedRoom> bookings = bookingService.getBookingsByUserEmail(email);
        List<BookingResponse> bookingResponses = new ArrayList<>();
        for (BookedRoom booking : bookings) {
            BookingResponse bookingResponse = getBookingResponse(booking);
            bookingResponses.add(bookingResponse);
        }
        return ResponseEntity.ok(bookingResponses);
    }
    @DeleteMapping("/booking/{bookingId}/delete")
    public void cancelBooking(@PathVariable Long bookingId){
        bookingService.cancelBooking(bookingId);
    }
    @GetMapping("/confirmation/{confirmationCode}")
    public ResponseEntity<?> getBookingByConfirmationCode(@PathVariable String confirmationCode){
        try{
            BookedRoom booking = bookingService.findByBookingConfirmationCode(confirmationCode);
            BookingResponse bookingResponse = getBookingResponse(booking);
            return ResponseEntity.ok(bookingResponse);
        }catch (ResourceNotFoundException ex){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        }
    }
    private BookingResponse getBookingResponse(BookedRoom booking){
        Room theRoom=roomService.getRoomById(booking.getRoom().getId()).get();
        RoomResponse room=new RoomResponse(theRoom.getId(),theRoom.getRoomType(),theRoom.getRoomPrice());
        return new BookingResponse(booking.getBookingId(),booking.getCheckInDate(),booking.getCheckOutDate(),
                booking.getGuestFullName(),booking.getGuestEmail(),booking.getNumOfAdults(),booking.getNumOfChildren(),
                booking.getTotalNumOfGuests(),booking.getBookingConfirmationCode(),room);


    }
}
