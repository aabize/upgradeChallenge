package com.campsite.reservation;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReservationRepository extends CrudRepository<Reservation, String> {

    /**
     * Finds all reservations that overlap with a specific date range
     *
     * Given two date ranges: A and B there is an overlapping if:
     *      - StartA < EndB and EndA > StartB
     * This method, given a range A, find all the reservations that match this condition
     *
     * @param arrivalDate Date range start
     * @param departureDate Date range end
     * @return List of reservations that overlap with the given period
     */
    public List<Reservation> findByDepartureDateGreaterThanAndArrivalDateLessThanOrderByArrivalDate(LocalDate arrivalDate, LocalDate departureDate);
}
