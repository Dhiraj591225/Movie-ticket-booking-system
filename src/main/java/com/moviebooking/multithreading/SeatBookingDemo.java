package com.moviebooking.multithreading;

public class SeatBookingDemo {

    private boolean seatAvailable = true;

    // Only one thread at a time can execute
    // this synchronized method on this object.
    public synchronized void bookSeat(String customerName) {

        System.out.println(
            customerName + " is checking the seat.");

        if (seatAvailable) {

            System.out.println(
                customerName + " is booking the seat.");

            seatAvailable = false;

            System.out.println(
                "Seat successfully booked by " + customerName);

        } else {

            System.out.println(
                "Sorry " + customerName
                + ", seat is already booked.");
        }
    }

    public void startBooking() {

        Thread customer1 = new Thread(() ->
            bookSeat("Customer 1"));

        Thread customer2 = new Thread(() ->
            bookSeat("Customer 2"));

        customer1.start();
        customer2.start();
    }
}

