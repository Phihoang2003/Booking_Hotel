package com.phihoang.lakeside_hotel.service;

import com.phihoang.lakeside_hotel.exception.ResourceNotFoundException;
import com.phihoang.lakeside_hotel.model.BookedRoom;
import com.phihoang.lakeside_hotel.model.Room;
import com.phihoang.lakeside_hotel.repository.BookedRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
@Service
@RequiredArgsConstructor
public class BookedRoomServiceImpl implements IBookedRoomService{
    private final BookedRoomRepository bookingRepository;
    private final IRoomService roomService;
    @Override
    public void cancelBooking(Long bookingId) {
        bookingRepository.deleteById(bookingId);
    }

    @Override
    public List<BookedRoom> getAllBookingsByRoomId(Long roomId) {
        return bookingRepository.findByRoomId(roomId);
    }

    @Override
    public String saveBooking(Long roomId, BookedRoom bookingRequest) {

       LocalDate checkOutDate = bookingRequest.getCheckOutDate();
        LocalDate checkInDate = bookingRequest.getCheckInDate();

        if( checkOutDate.isBefore(checkInDate)){
            return "Check-out date must be after check-in date";
        }
        System.out.println("Room Id: "+roomId);
        Room room=roomService.getRoomById(roomId).get();
       List<BookedRoom> existingBookings=room.getBookings();
       boolean roomIsAvailable=roomIsAvailable(bookingRequest,existingBookings);
       if(roomIsAvailable){
           room.adddBoooking(bookingRequest);
           bookingRepository.save(bookingRequest);

       }else{
              return "Room is not available for the selected dates";
       }
         return bookingRequest.getBookingConfirmationCode();
    }
    private boolean roomIsAvailable(BookedRoom bookingRequest, List<BookedRoom> existingBookings) {
        return existingBookings.stream()
                .noneMatch(existingBooking ->
                        bookingRequest.getCheckInDate().equals(existingBooking.getCheckInDate())
                                || bookingRequest.getCheckOutDate().isBefore(existingBooking.getCheckOutDate())
                                || (bookingRequest.getCheckInDate().isAfter(existingBooking.getCheckInDate())
                                && bookingRequest.getCheckInDate().isBefore(existingBooking.getCheckOutDate()))
                                || (bookingRequest.getCheckInDate().isBefore(existingBooking.getCheckInDate())

                                && bookingRequest.getCheckOutDate().equals(existingBooking.getCheckOutDate()))
                                || (bookingRequest.getCheckInDate().isBefore(existingBooking.getCheckInDate())

                                && bookingRequest.getCheckOutDate().isAfter(existingBooking.getCheckOutDate()))

                                || (bookingRequest.getCheckInDate().equals(existingBooking.getCheckOutDate())
                                && bookingRequest.getCheckOutDate().equals(existingBooking.getCheckInDate()))

                                || (bookingRequest.getCheckInDate().equals(existingBooking.getCheckOutDate())
                                && bookingRequest.getCheckOutDate().equals(bookingRequest.getCheckInDate()))
                );
    }

    @Override
    public BookedRoom findByBookingConfirmationCode(String confirmationCode) {

        return bookingRepository.findByBookingConfirmationCode(confirmationCode).orElseThrow(()->new ResourceNotFoundException("Booking not found"+confirmationCode));
    }

    @Override
    public List<BookedRoom> getAllBookings() {

        return bookingRepository.findAll();
    }

    @Override
    public List<BookedRoom> getBookingsByUserEmail(String email) {

        return bookingRepository.findByGuestEmail(email);
    }
}
