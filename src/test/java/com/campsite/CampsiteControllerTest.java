package com.campsite;

import com.campsite.exception.ErrorMessages;
import com.campsite.reservation.Reservation;
import com.campsite.reservation.ReservationResource;
import com.campsite.reservation.ReservationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.junit.Assert;

import java.time.LocalDate;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = UpgradeApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class CampsiteControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testGetReservation() throws Exception {
        mvc.perform(get("/campsite/reservation/{id}", "5b4107f3-438c-4b89-a3ba-27b7044220bd"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reservationId").value("5b4107f3-438c-4b89-a3ba-27b7044220bd"))
                .andExpect(jsonPath("$.guestName").value("Ali Bize"))
                .andExpect(jsonPath("$.guestMail").value("aaa@gmail.com"))
                .andExpect(jsonPath("$.arrivalDate").value("2018-12-01"))
                .andExpect(jsonPath("$.departureDate").value("2018-12-03"));
    }

    @Test
    public void testGetReservationNotFound() throws Exception {
        mvc.perform(get("/campsite/reservation/{id}", "aaaaaa"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(ErrorMessages.RESERVATION_NOT_FOUND.getMessage()));
    }

    @Test
    public void testDeleteReservation() throws Exception {
        //Check that reservation exists
        mvc.perform(get("/campsite/reservation/{id}", "5b4107f3-438c-4b89-a3ba-27b7044220be"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reservationId").value("5b4107f3-438c-4b89-a3ba-27b7044220be"));

        //Delete reservation
        mvc.perform(delete("/campsite/reservation/{id}", "5b4107f3-438c-4b89-a3ba-27b7044220be"))
                .andExpect(status().isNoContent());

        //Check that is deleted
        mvc.perform(get("/campsite/reservation/{id}", "5b4107f3-438c-4b89-a3ba-27b7044220be"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(ErrorMessages.RESERVATION_NOT_FOUND.getMessage()));
    }

    @Test
    public void testDeleteReservationNotFound() throws Exception {
        mvc.perform(delete("/campsite/reservation/{id}", "aaaaaa"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(ErrorMessages.RESERVATION_NOT_FOUND.getMessage()));
    }

    @Test
    public void testPostReservation() throws Exception {
        ReservationResource reservation = new ReservationResource();
        reservation.setGuestName("Diego Rivera");
        reservation.setGuestMail("diegor@gmail.com");
        reservation.setArrivalDate(LocalDate.now().plusDays(1));
        reservation.setDepartureDate(LocalDate.now().plusDays(3));

        String responseString = mvc.perform(post("/campsite/reservation")
                        .accept("application/campsite-reservation-response-v1-hal+json")
                        .contentType("application/campsite-reservation-v1-hal+json")
                        .content(objectMapper.writeValueAsString(reservation)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reservationId").isNotEmpty())
                .andExpect(jsonPath("$.guestName").value("Diego Rivera"))
                .andExpect(jsonPath("$.guestMail").value("diegor@gmail.com"))
                .andExpect(jsonPath("$.arrivalDate").value(LocalDate.now().plusDays(1).toString()))
                .andExpect(jsonPath("$.departureDate").value(LocalDate.now().plusDays(3).toString()))
                .andReturn()
                .getResponse()
                .getContentAsString();

        ReservationResource returnedReservation = objectMapper.readValue(responseString, ReservationResource.class);
        Reservation reservationSaved = reservationService.get(returnedReservation.getReservationId());
        Assert.assertEquals("Diego Rivera", reservationSaved.getGuestName());
        Assert.assertEquals("diegor@gmail.com", reservationSaved.getGuestMail());
        Assert.assertEquals(LocalDate.now().plusDays(1), reservationSaved.getArrivalDate());
        Assert.assertEquals(LocalDate.now().plusDays(3), reservationSaved.getDepartureDate());
    }

    @Test
    public void testOverlappingReservations() throws Exception {
        //Make first reservation 6 days from now
        ReservationResource reservation = new ReservationResource();
        reservation.setGuestName("Diego Rivera");
        reservation.setGuestMail("diegor@gmail.com");
        reservation.setArrivalDate(LocalDate.now().plusDays(6));
        reservation.setDepartureDate(LocalDate.now().plusDays(7));

        mvc.perform(post("/campsite/reservation")
                .accept("application/campsite-reservation-response-v1-hal+json")
                .contentType("application/campsite-reservation-v1-hal+json")
                .content(objectMapper.writeValueAsString(reservation)))
                .andExpect(status().isCreated());

        //Make another reservation that ends when the previous starts
        reservation.setArrivalDate(LocalDate.now().plusDays(5));
        reservation.setDepartureDate(LocalDate.now().plusDays(6));

        mvc.perform(post("/campsite/reservation")
                .accept("application/campsite-reservation-response-v1-hal+json")
                .contentType("application/campsite-reservation-v1-hal+json")
                .content(objectMapper.writeValueAsString(reservation)))
                .andExpect(status().isCreated());

        //Make another reservation after the first one
        reservation.setArrivalDate(LocalDate.now().plusDays(7));
        reservation.setDepartureDate(LocalDate.now().plusDays(10));

        mvc.perform(post("/campsite/reservation")
                .accept("application/campsite-reservation-response-v1-hal+json")
                .contentType("application/campsite-reservation-v1-hal+json")
                .content(objectMapper.writeValueAsString(reservation)))
                .andExpect(status().isCreated());

        //Make a reservation that overlaps with the previous one in the end
        reservation.setArrivalDate(LocalDate.now().plusDays(8));
        reservation.setDepartureDate(LocalDate.now().plusDays(11));

        mvc.perform(post("/campsite/reservation")
                .accept("application/campsite-reservation-response-v1-hal+json")
                .contentType("application/campsite-reservation-v1-hal+json")
                .content(objectMapper.writeValueAsString(reservation)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorMessages.NOT_AVAILABLE.getMessage()));

        //Make a reservation that overlaps with the previous one in the beginning
        reservation.setArrivalDate(LocalDate.now().plusDays(7));
        reservation.setDepartureDate(LocalDate.now().plusDays(8));

        mvc.perform(post("/campsite/reservation")
                .accept("application/campsite-reservation-response-v1-hal+json")
                .contentType("application/campsite-reservation-v1-hal+json")
                .content(objectMapper.writeValueAsString(reservation)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorMessages.NOT_AVAILABLE.getMessage()));
    }

    @Test
    public void testPostReservationNoName() throws Exception {
        ReservationResource reservation = new ReservationResource();
        reservation.setGuestMail("diegor@gmail.com");
        reservation.setArrivalDate(LocalDate.now().plusDays(1));
        reservation.setDepartureDate(LocalDate.now().plusDays(3));

        mvc.perform(post("/campsite/reservation")
                .accept("application/campsite-reservation-response-v1-hal+json")
                .contentType("application/campsite-reservation-v1-hal+json")
                .content(objectMapper.writeValueAsString(reservation)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorMessages.PROVIDE_NAME.getMessage()));
    }

    @Test
    public void testPostReservationNoMail() throws Exception {
        ReservationResource reservation = new ReservationResource();
        reservation.setGuestName("Diego Rivera");
        reservation.setArrivalDate(LocalDate.now().plusDays(1));
        reservation.setDepartureDate(LocalDate.now().plusDays(3));

        mvc.perform(post("/campsite/reservation")
                .accept("application/campsite-reservation-response-v1-hal+json")
                .contentType("application/campsite-reservation-v1-hal+json")
                .content(objectMapper.writeValueAsString(reservation)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorMessages.PROVIDE_MAIL.getMessage()));
    }

    @Test
    public void testPostReservationNoArrivalDate() throws Exception {
        ReservationResource reservation = new ReservationResource();
        reservation.setGuestName("Diego Rivera");
        reservation.setGuestMail("diegor@gmail.com");
        reservation.setDepartureDate(LocalDate.now().plusDays(3));

        mvc.perform(post("/campsite/reservation")
                .accept("application/campsite-reservation-response-v1-hal+json")
                .contentType("application/campsite-reservation-v1-hal+json")
                .content(objectMapper.writeValueAsString(reservation)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorMessages.PROVIDE_DATES.getMessage()));
    }

    @Test
    public void testPostReservationNoDepartureDate() throws Exception {
        ReservationResource reservation = new ReservationResource();
        reservation.setGuestName("Diego Rivera");
        reservation.setGuestMail("diegor@gmail.com");
        reservation.setDepartureDate(LocalDate.now().plusDays(3));

        mvc.perform(post("/campsite/reservation")
                .accept("application/campsite-reservation-response-v1-hal+json")
                .contentType("application/campsite-reservation-v1-hal+json")
                .content(objectMapper.writeValueAsString(reservation)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorMessages.PROVIDE_DATES.getMessage()));
    }

    @Test
    public void testPostReservationInvalidRange() throws Exception {
        ReservationResource reservation = new ReservationResource();
        reservation.setGuestName("Diego Rivera");
        reservation.setGuestMail("diegor@gmail.com");
        reservation.setArrivalDate(LocalDate.now().plusDays(4));  //Arrival date is after departure date
        reservation.setDepartureDate(LocalDate.now().plusDays(3));

        mvc.perform(post("/campsite/reservation")
                .accept("application/campsite-reservation-response-v1-hal+json")
                .contentType("application/campsite-reservation-v1-hal+json")
                .content(objectMapper.writeValueAsString(reservation)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorMessages.INVALID_RANGE.getMessage()));
    }

    @Test
    public void testPostReservationMaxDaysExceeded() throws Exception {
        ReservationResource reservation = new ReservationResource();
        reservation.setGuestName("Diego Rivera");
        reservation.setGuestMail("diegor@gmail.com");
        reservation.setArrivalDate(LocalDate.now().plusDays(1));  //Max reservation days is 3
        reservation.setDepartureDate(LocalDate.now().plusDays(10));

        mvc.perform(post("/campsite/reservation")
                .accept("application/campsite-reservation-response-v1-hal+json")
                .contentType("application/campsite-reservation-v1-hal+json")
                .content(objectMapper.writeValueAsString(reservation)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorMessages.MAX_RANGE_EXCEEDED.getMessage()));
    }

    @Test
    public void testPostReservationLessThanOneDayAhead() throws Exception {
        ReservationResource reservation = new ReservationResource();
        reservation.setGuestName("Diego Rivera");
        reservation.setGuestMail("diegor@gmail.com");
        reservation.setArrivalDate(LocalDate.now());
        reservation.setDepartureDate(LocalDate.now().plusDays(3));

        mvc.perform(post("/campsite/reservation")
                .accept("application/campsite-reservation-response-v1-hal+json")
                .contentType("application/campsite-reservation-v1-hal+json")
                .content(objectMapper.writeValueAsString(reservation)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorMessages.ARRIVAL_DATE_TOO_EARLY.getMessage()));
    }

    @Test
    public void testPostReservationMoreThanOneMonthInAdvance() throws Exception {
        ReservationResource reservation = new ReservationResource();
        reservation.setGuestName("Diego Rivera");
        reservation.setGuestMail("diegor@gmail.com");
        reservation.setArrivalDate(LocalDate.now().plusDays(35));
        reservation.setDepartureDate(LocalDate.now().plusDays(37));

        mvc.perform(post("/campsite/reservation")
                .accept("application/campsite-reservation-response-v1-hal+json")
                .contentType("application/campsite-reservation-v1-hal+json")
                .content(objectMapper.writeValueAsString(reservation)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorMessages.ARRIVAL_DATE_TOO_LATE.getMessage()));
    }

    @Test
    public void testPatchReservation() throws Exception {
        ReservationResource reservation = new ReservationResource();
        reservation.setGuestName("Diego Rivera");
        reservation.setGuestMail("diegor@gmail.com");
        reservation.setArrivalDate(LocalDate.now().plusDays(15));
        reservation.setDepartureDate(LocalDate.now().plusDays(17));

        String responseString = mvc.perform(post("/campsite/reservation")
                .accept("application/campsite-reservation-response-v1-hal+json")
                .contentType("application/campsite-reservation-v1-hal+json")
                .content(objectMapper.writeValueAsString(reservation)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ReservationResource returnedReservation = objectMapper.readValue(responseString, ReservationResource.class);
        String reservationId = returnedReservation.getReservationId();

        //Update name
        ReservationResource reservation2 = new ReservationResource();
        reservation2.setGuestName("Diego A. Rivera");

        mvc.perform(patch("/campsite/reservation/{id}", reservationId)
                .accept("application/campsite-reservation-response-v1-hal+json")
                .contentType("application/campsite-reservation-patch-v1-hal+json")
                .content(objectMapper.writeValueAsString(reservation2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reservationId").value(reservationId))
                .andExpect(jsonPath("$.guestName").value("Diego A. Rivera")) //Only name changed
                .andExpect(jsonPath("$.guestMail").value("diegor@gmail.com"))
                .andExpect(jsonPath("$.arrivalDate").value(LocalDate.now().plusDays(15).toString()))
                .andExpect(jsonPath("$.departureDate").value(LocalDate.now().plusDays(17).toString()));

        //Update multiple fields
        ReservationResource reservation3 = new ReservationResource();
        reservation3.setGuestMail("diego222@gmail.com");
        reservation3.setArrivalDate(LocalDate.now().plusDays(19));
        reservation3.setDepartureDate(LocalDate.now().plusDays(21));

        mvc.perform(patch("/campsite/reservation/{id}", reservationId)
                .accept("application/campsite-reservation-response-v1-hal+json")
                .contentType("application/campsite-reservation-patch-v1-hal+json")
                .content(objectMapper.writeValueAsString(reservation3)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reservationId").value(reservationId))
                .andExpect(jsonPath("$.guestName").value("Diego A. Rivera")) //Only name changed
                .andExpect(jsonPath("$.guestMail").value("diego222@gmail.com"))
                .andExpect(jsonPath("$.arrivalDate").value(LocalDate.now().plusDays(19).toString()))
                .andExpect(jsonPath("$.departureDate").value(LocalDate.now().plusDays(21).toString()));

        //Update only one date (must fail)
        ReservationResource reservation4 = new ReservationResource();
        reservation4.setArrivalDate(LocalDate.now().plusDays(19));
        mvc.perform(patch("/campsite/reservation/{id}", reservationId)
                .accept("application/campsite-reservation-response-v1-hal+json")
                .contentType("application/campsite-reservation-patch-v1-hal+json")
                .content(objectMapper.writeValueAsString(reservation4)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorMessages.UPDATE_ONE_DATE.getMessage()));

        //Update dates to an invalid range
        ReservationResource reservation5 = new ReservationResource();
        reservation5.setArrivalDate(LocalDate.now().plusDays(45));
        reservation5.setDepartureDate(LocalDate.now().plusDays(48));
        mvc.perform(patch("/campsite/reservation/{id}", reservationId)
                .accept("application/campsite-reservation-response-v1-hal+json")
                .contentType("application/campsite-reservation-patch-v1-hal+json")
                .content(objectMapper.writeValueAsString(reservation5)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorMessages.ARRIVAL_DATE_TOO_LATE.getMessage()));
    }

    @Test
    public void testPatchReservationNotFound() throws Exception {
        ReservationResource reservation = new ReservationResource();
        reservation.setGuestName("Diego A. Rivera");

        mvc.perform(patch("/campsite/reservation/{id}", "abcd")
                .accept("application/campsite-reservation-response-v1-hal+json")
                .contentType("application/campsite-reservation-patch-v1-hal+json")
                .content(objectMapper.writeValueAsString(reservation)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(ErrorMessages.RESERVATION_NOT_FOUND.getMessage()));
    }

    @Test
    public void testAvailability() throws Exception {
        /* In 2019-01 there are 4 reservations loaded in DB:
           - '2019-01-05', '2019-01-08'
           - '2019-01-09', '2019-01-11'
           - '2019-01-15', '2019-01-18'
           - '2019-01-18', '2019-01-22'

           Availability will be checked from 01-06 to 01-31.
           There must be three available intervals: [01-08 - 01-09], [01-11 - 01-15], [01-22 - 01-31]
        */
        mvc.perform(get("/campsite/availability/?from={from}&to={to}", "2019-01-06", "2019-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestedRange.from").value("2019-01-06"))
                .andExpect(jsonPath("$.requestedRange.to").value("2019-01-31"))
                .andExpect(jsonPath("$.availableRanges", hasSize(3)))
                .andExpect(jsonPath("$.availableRanges[0].from").value("2019-01-08"))
                .andExpect(jsonPath("$.availableRanges[0].to").value("2019-01-09"))
                .andExpect(jsonPath("$.availableRanges[1].from").value("2019-01-11"))
                .andExpect(jsonPath("$.availableRanges[1].to").value("2019-01-15"))
                .andExpect(jsonPath("$.availableRanges[2].from").value("2019-01-22"))
                .andExpect(jsonPath("$.availableRanges[2].to").value("2019-01-31"));
    }

    @Test
    public void testAvailabilityDefaultValues() throws Exception {
        mvc.perform(get("/campsite/availability"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestedRange.from").value(LocalDate.now().toString()))
                .andExpect(jsonPath("$.requestedRange.to").value(LocalDate.now().plusMonths(1).toString()));
    }

    @Test
    public void testAvailabilityInvalidParameters() throws Exception {
        mvc.perform(get("/campsite/availability/?from={from}", "2019-01-06"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorMessages.AVAILABILITY_MISSING_PARAMETER.getMessage()));
    }
}
