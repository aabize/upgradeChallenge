package com.campsite;

import com.campsite.availability.AvailabilityResource;
import com.campsite.availability.AvailabilityService;
import com.campsite.reservation.Reservation;
import com.campsite.reservation.ReservationResource;
import com.campsite.reservation.ReservationResourceAssembler;
import com.campsite.reservation.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping(value = "campsite")
public class CampsiteController {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private AvailabilityService availabilityService;

    @Autowired
    private ReservationResourceAssembler reservationResourceAssembler;

    /**
     * Service that returns the campsite availability in a given time range
     *
     * @param from Range start date
     * @param to Range end date
     * @return Periods of time where the campsite is available
     */
    @RequestMapping(
                method = RequestMethod.GET,
                value = "/availability",
                produces = { "application/campsite-availability-response-v1-hal+json" }
            )
    public ResponseEntity<Object> getAvailability(@RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                                  @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        AvailabilityResource resource = availabilityService.getAvailability(from, to);
        return new ResponseEntity<>(resource, HttpStatus.OK);
    }

    /**
     * Service to make a reservation
     *
     * @param reservationRequest Reservation to create
     * @return The created reservation
     */
    @RequestMapping(
                method = RequestMethod.POST,
                value = "/reservation",
                produces = { "application/campsite-reservation-response-v1-hal+json" },
                consumes = { "application/campsite-reservation-v1-hal+json" }
          )
    public ResponseEntity<Object> makeReservation(@RequestBody ReservationResource reservationRequest) {

        Reservation entity = reservationResourceAssembler.toEntity(reservationRequest);
        Reservation savedEntity = reservationService.add(entity);
        return new ResponseEntity<>(reservationResourceAssembler.toResource(savedEntity), HttpStatus.CREATED);
    }

    /**
     * Service to update a reservation
     * @param id Id of the reservation to be updated
     * @param reservationRequest Reservation with the changes to be made
     * @return The updated reservation
     */
    @RequestMapping(
            method = RequestMethod.PATCH,
            value = "/reservation/{id}",
            produces = { "application/campsite-reservation-response-v1-hal+json" },
            consumes = { "application/campsite-reservation-patch-v1-hal+json" }
    )
    public ResponseEntity<Object> updateReservation(@PathVariable String id,
                                                    @RequestBody ReservationResource reservationRequest) {

        Reservation entity = reservationResourceAssembler.toEntity(reservationRequest);
        entity.setId(id);
        Reservation updatedEntity = reservationService.update(entity);
        return new ResponseEntity<>(reservationResourceAssembler.toResource(updatedEntity), HttpStatus.OK);
    }

    /**
     * Service to get a reservation
     *
     * @param id Id of the reservation
     * @return Reservation with the given id
     */
    @RequestMapping(
            method = RequestMethod.GET,
            value = "/reservation/{id}",
            produces = { "application/campsite-reservation-response-v1-hal+json" }
    )
    public ResponseEntity<Object> getReservation(@PathVariable String id) {

        Reservation entity = reservationService.get(id);
        return new ResponseEntity<>(reservationResourceAssembler.toResource(entity), HttpStatus.OK);
    }

    /**
     * Service to delete a reservation
     *
     * @param id Id of the reservation to delete
     * @return Empty body
     */
    @RequestMapping(
            method = RequestMethod.DELETE,
            value = "/reservation/{id}"
    )
    public ResponseEntity<Object> deleteReservation(@PathVariable String id) {

        reservationService.delete(id);
        return new ResponseEntity<>("", HttpStatus.NO_CONTENT);
    }

}
