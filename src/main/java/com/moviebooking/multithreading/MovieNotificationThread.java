
package com.moviebooking.multithreading;

public class MovieNotificationThread extends Thread {

    @Override
    public void run() {

        System.out.println(
            "Sending booking confirmation notification...");

        try {
            Thread.sleep(2000);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            System.out.println(
                "Notification thread was interrupted.");
            return;
        }

        System.out.println(
            "Booking notification sent successfully.");
    }
}

