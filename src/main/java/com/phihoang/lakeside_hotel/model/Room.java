package com.phihoang.lakeside_hotel.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;


import java.math.BigDecimal;
import java.sql.Blob;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Entity
@Getter
@Setter
@AllArgsConstructor
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String roomType;
    private BigDecimal roomPrice;
    private boolean isBooked=false;
    @Lob
    private Blob photo;
    @OneToMany(mappedBy = "room",fetch = FetchType.LAZY,cascade = CascadeType.ALL)
    private List<BookedRoom> bookings;

    public Room(){
        this.bookings=new ArrayList<>();
    }

    public void adddBoooking(BookedRoom booking){
        if(bookings==null){
            bookings=new ArrayList<>();
        }
        bookings.add(booking);
        booking.setRoom(this);
        isBooked=true;
        String bookingCode = "BK" + new Random().nextInt(1000);
        booking.setBookingConfirmationCode(bookingCode);
    }

}
