package com.campsite;

import com.campsite.reservation.ReservationResource;
import com.campsite.reservation.ReservationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.annotation.Repeat;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = UpgradeApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ConcurrencyTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ReservationService reservationService;

    @Test
    @Repeat(value = 10) //Repeat 10 times for a higher chance of failure if there is a concurrency problem
    public void testConcurrentCreateReservation() throws Exception {
        LinkedBlockingQueue<Integer> results = new LinkedBlockingQueue();
        int threadCount = 10;
        final String[] reservationId = new String[1];

        //Try to make a reservation in 10 parallel threads
        runMultithreaded( new Runnable() {
                       public void run() {
                           try{
                               makeReservation(21, 22, reservationId, results);                           }
                           catch(Exception e)
                           {
                               e.printStackTrace();
                           }
                       }
                   }, threadCount);

        assertEquals(1, Collections.frequency(results, 201)); //Only one reservation is created
        assertEquals(threadCount - 1, Collections.frequency(results, 400)); //The remaining fail

        //Remove reservation for the next repetition
        reservationService.delete(reservationId[0]);

    }

    @Test
    public void testConcurrentCreateAndUpdateReservation() throws Exception {
        //Create a reservation to update later
        ReservationResource reservation = new ReservationResource();
        reservation.setGuestName("Diego Rivera");
        reservation.setGuestMail("diegor@gmail.com");
        reservation.setArrivalDate(LocalDate.now().plusDays(21));
        reservation.setDepartureDate(LocalDate.now().plusDays(22));

        String responseString = mvc.perform(post("/campsite/reservation")
                .accept("application/campsite-reservation-response-v1-hal+json")
                .contentType("application/campsite-reservation-v1-hal+json")
                .content(objectMapper.writeValueAsString(reservation))).andReturn().getResponse().getContentAsString();

        String firstReservationId = objectMapper.readValue(responseString, ReservationResource.class).getReservationId();


        LinkedBlockingQueue<Integer> results = new LinkedBlockingQueue();
        int threadCount = 10;
        final String[] reservationId = new String[1];

        //Try to make a reservation in 10 parallel threads
        runMultithreaded( new Runnable() {
            public void run() {
                try{
                    makeReservation(25, 26, reservationId, results);                           }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
            }
        }, threadCount);

        assertEquals(1, Collections.frequency(results, 201)); //Only one reservation is created
        assertEquals(threadCount - 1, Collections.frequency(results, 400)); //The remaining fail

        //Remove reservations for the next repetition
        reservationService.delete(reservationId[0]);
        reservationService.delete(firstReservationId);

    }

    private void makeReservation(int fromOffset, int toOffset, String[] reservationId, LinkedBlockingQueue<Integer> results) throws Exception {
        ReservationResource reservation = new ReservationResource();
        reservation.setGuestName("Diego Rivera");
        reservation.setGuestMail("diegor@gmail.com");
        reservation.setArrivalDate(LocalDate.now().plusDays(21));
        reservation.setDepartureDate(LocalDate.now().plusDays(22));

        MvcResult response = mvc.perform(post("/campsite/reservation")
                .accept("application/campsite-reservation-response-v1-hal+json")
                .contentType("application/campsite-reservation-v1-hal+json")
                .content(objectMapper.writeValueAsString(reservation))).andReturn();

        int statusCode = response.getResponse().getStatus();

        //In case of success, save the id to delete it after
        if (statusCode == 201) {
            String responseString = response.getResponse().getContentAsString();
            reservationId[0] = objectMapper.readValue(responseString, ReservationResource.class).getReservationId();
        }

        results.put(statusCode); //Save return code in a list

    }

    private static void runMultithreaded(Runnable  runnable, int threadCount) throws InterruptedException
    {
        List<Thread> threadList = new LinkedList<Thread>();
        for(int i = 0 ; i < threadCount; i++)
        {
            threadList.add(new Thread(runnable));
        }
        for( Thread t :  threadList)
        {
            t.start();
        }
        for( Thread t :  threadList)
        {
            t.join();
        }
    }
}
