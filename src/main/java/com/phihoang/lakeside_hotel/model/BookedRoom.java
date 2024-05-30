package com.phihoang.lakeside_hotel.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BookedRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookingId;
    @Column(name="check_in")
    private LocalDate checkInDate;
    @Column(name="check_out")
    private LocalDate checkOutDate;
    @Column(name="guest_full_name")
    private String guestFullName;
    @Column(name="guest_email")
    private String guestEmail;
    @Column(name="adults")
    private int numOfAdults;
    @Column(name="children")
    private int numOfChildren;
    @Column(name="total_guests")
    private int totalNumOfGuests;
    @Column(name="booking_confirmation_code")
    private String bookingConfirmationCode;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="room_id")
    private Room room;

    public void calculateTotalGuests(){
        this.totalNumOfGuests=this.numOfAdults+this.numOfChildren;
    }
    public void setNumOfAdults(int numOfAdults) {
        this.numOfAdults = numOfAdults;
        calculateTotalGuests();
    }
    public void setNumOfChildren(int numOfChildren) {
        this.numOfChildren = numOfChildren;
        calculateTotalGuests();
    }
    public void setBookingConfirmationCode(String bookingConfirmationCode) {
        this.bookingConfirmationCode = bookingConfirmationCode;
    }
}
