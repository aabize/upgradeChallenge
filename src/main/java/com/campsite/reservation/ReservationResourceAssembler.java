package com.campsite.reservation;

import com.campsite.CampsiteController;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.mvc.ResourceAssemblerSupport;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@Component
public class ReservationResourceAssembler extends ResourceAssemblerSupport<Reservation, ReservationResource> {

    public ReservationResourceAssembler() {
        super(CampsiteController.class, ReservationResource.class);
    }

    @Override
    public ReservationResource toResource(Reservation entity) {

        ReservationResource resource = new ReservationResource();
        resource.setReservationId(entity.getId());
        resource.setArrivalDate(entity.getArrivalDate());
        resource.setDepartureDate(entity.getDepartureDate());
        resource.setGuestMail(entity.getGuestMail());
        resource.setGuestName(entity.getGuestName());

        Link link = linkTo(methodOn(CampsiteController.class).getReservation(entity.getId())).withSelfRel();
        resource.add(link);

        return resource;
    }

    public Reservation toEntity(ReservationResource resource) {

        Reservation entity = new Reservation();
        entity.setArrivalDate(resource.getArrivalDate());
        entity.setDepartureDate(resource.getDepartureDate());
        entity.setGuestMail(resource.getGuestMail());
        entity.setGuestName(resource.getGuestName());

        return entity;
    }
}
