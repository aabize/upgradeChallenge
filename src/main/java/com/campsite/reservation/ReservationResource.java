package com.campsite.reservation;

import org.springframework.hateoas.ResourceSupport;

import java.time.LocalDate;
import java.util.Objects;

public class ReservationResource extends ResourceSupport {

    private String reservationId;

    private String guestName;

    private String guestMail;

    private LocalDate arrivalDate;

    private LocalDate departureDate;

    public String getReservationId() {
        return reservationId;
    }

    public void setReservationId(String reservationId) {
        this.reservationId = reservationId;
    }

    public String getGuestName() {
        return guestName;
    }

    public void setGuestName(String guestName) {
        this.guestName = guestName;
    }

    public String getGuestMail() {
        return guestMail;
    }

    public void setGuestMail(String guestMail) {
        this.guestMail = guestMail;
    }

    public LocalDate getArrivalDate() {
        return arrivalDate;
    }

    public void setArrivalDate(LocalDate arrivalDate) {
        this.arrivalDate = arrivalDate;
    }

    public LocalDate getDepartureDate() {
        return departureDate;
    }

    public void setDepartureDate(LocalDate departureDate) {
        this.departureDate = departureDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ReservationResource that = (ReservationResource) o;
        return guestName.equals(that.guestName) &&
                guestMail.equals(that.guestMail) &&
                arrivalDate.equals(that.arrivalDate) &&
                departureDate.equals(that.departureDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), guestName, guestMail, arrivalDate, departureDate);
    }

    @Override
    public String toString() {
        return "ReservationResource{" +
                "guestName='" + guestName + '\'' +
                ", guestMail='" + guestMail + '\'' +
                ", arrivalDate=" + arrivalDate +
                ", departureDate=" + departureDate +
                '}';
    }
}
