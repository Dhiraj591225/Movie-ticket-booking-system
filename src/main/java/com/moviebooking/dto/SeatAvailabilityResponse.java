package com.moviebooking.dto;

public class SeatAvailabilityResponse {

    private Long seatId;
    private String seatNumber;
    private boolean available;
    private String seatType;

    public SeatAvailabilityResponse() {
    }

    public SeatAvailabilityResponse(Long seatId, String seatNumber, boolean available) {
        this(seatId, seatNumber, available, "REGULAR");
    }

    public SeatAvailabilityResponse(Long seatId, String seatNumber, boolean available, String seatType) {
        this.seatId = seatId;
        this.seatNumber = seatNumber;
        this.available = available;
        this.seatType = seatType != null ? seatType : "REGULAR";
    }

    public Long getSeatId() {
        return seatId;
    }

    public void setSeatId(Long seatId) {
        this.seatId = seatId;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public String getSeatType() {
        return seatType;
    }

    public void setSeatType(String seatType) {
        this.seatType = seatType;
    }
}
