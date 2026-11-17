package com.moviebooking.multithreading;

public class BookingReminderTask implements Runnable {

    @Override
    public void run() {

        System.out.println(
            "Checking booking reminder...");

        try {
            Thread.sleep(1000);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            System.out.println(
                "Reminder task interrupted.");
            return;
        }

        System.out.println(
            "Reminder sent to the customer.");
    }
}

