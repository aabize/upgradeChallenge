package com.campsite.reservation;

import com.campsite.exception.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.time.temporal.ChronoUnit.DAYS;

@Service
public class ReservationService {

    @Autowired
    private ReservationRepository reservationRepository;

    /**
     * Adds a new Reservation
     * It is synchronized to avoid multiple saves at the same time and allow to check for overlapping
     *
     * @param entity Reservation to add
     * @return Added reservation
     * @throws InvalidReservationException if the reservation has missing fields
     * @throws NotAvailableException if the campsite is not available in the requested period
     * @throws InvalidDateException if the date period is not valid or violates the constraints
     */
    public synchronized Reservation add(Reservation entity) {
        //Check that all the fields are present
        if ((entity.getArrivalDate() == null) || (entity.getDepartureDate() == null)) {
            throw new InvalidReservationException(ErrorMessages.PROVIDE_DATES.getMessage());
        }
        if (entity.getGuestName() == null) {
            throw new InvalidReservationException(ErrorMessages.PROVIDE_NAME.getMessage());
        }
        if (entity.getGuestMail() == null) {
            throw new InvalidReservationException(ErrorMessages.PROVIDE_MAIL.getMessage());
        }

        validateReservationDates(entity.getArrivalDate(), entity.getDepartureDate());

        //Check if there is an overlapping reservation
        List<Reservation> overlappingReservations = reservationRepository.findByDepartureDateGreaterThanAndArrivalDateLessThanOrderByArrivalDate(entity.getArrivalDate(), entity.getDepartureDate());

        if (overlappingReservations.size() > 0) {
            throw new NotAvailableException(ErrorMessages.NOT_AVAILABLE.getMessage());
        }

        //Set reservation ID. Use UUID for simplicity but it maybe it could be replaced with something shorter and more human readable
        UUID uuid = UUID.randomUUID();
        entity.setId(uuid.toString());

        return reservationRepository.save(entity);
    }

    /**
     * Updates a reservation
     * Only the non-null fields are used to update the reservation
     * It is synchronized to avoid multiple saves at the same time and allow to check for overlapping
     *
     * @param entity Reservation to update
     * @return Updated reservation
     * @throws ReservationNotFoundException If the reservation to update does not exist
     * @throws InvalidReservationException If only one date of the range is specified
     * @throws NotAvailableException if the campsite is not available in the requested period
     * @throws InvalidDateException if the date period is not valid or violates the constraints
     */
    public synchronized Reservation update(Reservation entity) {

        //Check if reservation exists
        Optional<Reservation> optionalReservation = reservationRepository.findById(entity.getId());
        if (!optionalReservation.isPresent()) {
            throw new ReservationNotFoundException(ErrorMessages.RESERVATION_NOT_FOUND.getMessage());
        }

        //Check that if a date is being updated the other is too
        if ((entity.getArrivalDate() != null && entity.getDepartureDate() == null) ||
                (entity.getArrivalDate() == null) && (entity.getDepartureDate() != null)) {
            throw new InvalidReservationException(ErrorMessages.UPDATE_ONE_DATE.getMessage());
        }

        if ((entity.getArrivalDate() != null) && (entity.getDepartureDate() != null)) {
            validateReservationDates(entity.getArrivalDate(), entity.getDepartureDate());
        }

        //Check if all fields are null
        if ((entity.getArrivalDate() == null) && (entity.getDepartureDate() == null) && (entity.getGuestMail() == null) && (entity.getGuestName() == null)) {
            throw new InvalidReservationException(ErrorMessages.EMPTY_UPDATE.getMessage());
        }

        //Update fields
        Reservation reservationToUpdate = optionalReservation.get();
        if (entity.getGuestName() != null) {
            reservationToUpdate.setGuestName(entity.getGuestName());
        }
        if (entity.getGuestMail() != null) {
            reservationToUpdate.setGuestMail(entity.getGuestMail());
        }
        if (entity.getArrivalDate() != null) { //If it's not null then both dates are not
            reservationToUpdate.setArrivalDate(entity.getArrivalDate());
            reservationToUpdate.setDepartureDate(entity.getDepartureDate());
        }

        //Check if there is an overlapping reservation
        List<Reservation> overlappingReservations = reservationRepository.findByDepartureDateGreaterThanAndArrivalDateLessThanOrderByArrivalDate(reservationToUpdate.getArrivalDate(), reservationToUpdate.getDepartureDate());

        if (overlappingReservations.size() > 0) {
            //If there is only one overlapping reservation, it can be the one being updated. In this case, it is updated.
            if ((overlappingReservations.size() == 1) &&
                    (overlappingReservations.get(0).getId() == reservationToUpdate.getId())) {
                return reservationRepository.save(reservationToUpdate);
            } else {
                throw new NotAvailableException(ErrorMessages.NOT_AVAILABLE.getMessage());
            }
        }

        return reservationRepository.save(reservationToUpdate);
    }

    /**
     * Gets a reservation given its Id
     * @param id Id of the reservation
     * @return Reservation with the given Id
     * @throws ReservationNotFoundException If the reservation does not exist
     */
    public Reservation get(String id) {
        Optional<Reservation> reservation = reservationRepository.findById(id);

        if (!reservation.isPresent()) {
            throw new ReservationNotFoundException(ErrorMessages.RESERVATION_NOT_FOUND.getMessage());
        }

        return reservation.get();
    }

    /**
     * Deletes a reservation given its Id
     * @param id Id of the reservation to delete
     * @throws ReservationNotFoundException If the reservation to delete does not exist
     */
    public void delete(String id) {
        if (!reservationRepository.findById(id).isPresent()) {
            throw new ReservationNotFoundException(ErrorMessages.RESERVATION_NOT_FOUND.getMessage());
        }
        reservationRepository.deleteById(id);
    }

    /**
     * Validates that a time range is valid and match the constraints
     * @param arrivalDate Begin of the time range
     * @param departureDate End of the time range
     * @throws InvalidDateException if range is invalid or violates the specified constraints
     *   (max reservation of 3 days, reservation minimum 1 day ahead of arrival and up to one month in advance)
     *
     */
    private void validateReservationDates(LocalDate arrivalDate, LocalDate departureDate) {
        //Validate range
        if (arrivalDate.isAfter(departureDate) ||
                arrivalDate.isEqual(departureDate)) {
            throw new InvalidDateException(ErrorMessages.INVALID_RANGE.getMessage());
        }

        //Validate reservation days constraints
        long reservationDays = DAYS.between(arrivalDate, departureDate);
        if (reservationDays > 3) {
            throw new InvalidDateException(ErrorMessages.MAX_RANGE_EXCEEDED.getMessage());
        }

        long daysToReservationStart = DAYS.between(LocalDate.now(), arrivalDate);
        if (daysToReservationStart < 1) {
            throw new InvalidDateException(ErrorMessages.ARRIVAL_DATE_TOO_EARLY.getMessage());
        }
        if (daysToReservationStart > 30) {
            throw new InvalidDateException(ErrorMessages.ARRIVAL_DATE_TOO_LATE.getMessage());
        }
    }

}
