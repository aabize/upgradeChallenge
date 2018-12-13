package com.campsite.availability;

import com.campsite.CampsiteController;
import com.campsite.exception.ErrorMessages;
import com.campsite.exception.InvalidDateException;
import com.campsite.reservation.Reservation;
import com.campsite.reservation.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@Service
public class AvailabilityService {

    @Autowired
    private ReservationRepository reservationRepository;

    /**
     * Gets availability of the campsite in the given period
     * @param from Period start
     * @param to Period end
     * @return List of periods within the limits when the campsite is available
     * @throws InvalidDateException if dates are null or from date is after to
     */
    public AvailabilityResource getAvailability(LocalDate from, LocalDate to) {
        //If both are null override with default values
        if ((from == null) && (to == null)) {
            from = LocalDate.now();
            to = from.plusMonths(1);
        } else if ((from == null) || (to == null)) {
            //Fail if only one is null
            throw new InvalidDateException(ErrorMessages.AVAILABILITY_MISSING_PARAMETER.getMessage());
        }

        if ((from.isAfter(to)) || from.isEqual(to)) {
            throw new InvalidDateException(ErrorMessages.INVALID_RANGE.getMessage());
        }

        AvailabilityResource availability = new AvailabilityResource();
        availability.setRequestedRange(new TimeRangeResource(from, to));

        List<TimeRangeResource> availableRanges = new ArrayList<>();

        //Find all reservations overlapping the requested range ordered
        List<Reservation> reservationsInRange = reservationRepository.findByDepartureDateGreaterThanAndArrivalDateLessThanOrderByArrivalDate(from, to);

        //First assume the campsite is available for the whole range
        availableRanges.add(new TimeRangeResource(from, to));

        for (Reservation reservation : reservationsInRange) {
            //For each reservation in the period, remove the last available range saved in the list
            TimeRangeResource lastAvailableRange = availableRanges.remove(availableRanges.size() - 1);

            //Compare the range with the current reservation to check whether to insert 1, 2 or none new available ranges
            //There is an available range from the start of the last available range to the start of the current reservation
            if (lastAvailableRange.getFrom().isBefore(reservation.getArrivalDate())) {
                availableRanges.add(new TimeRangeResource(lastAvailableRange.getFrom(), reservation.getArrivalDate()));
            }
            //There is an available range from the end of the current reservation to the end of the last available range
            if (reservation.getDepartureDate().isBefore(lastAvailableRange.getTo())) {
                availableRanges.add(new TimeRangeResource(reservation.getDepartureDate(), lastAvailableRange.getTo()));
            }
        }

        availability.setAvailableRanges(availableRanges);
        availability.add();

        //Add link to response
        Link link = linkTo(methodOn(CampsiteController.class).getAvailability(from, to)).withSelfRel();
        availability.add(link);

        return availability;
    }
}
