package com.campsite.exception;

public enum ErrorMessages
{
    RESERVATION_NOT_FOUND("There is no reservation with the given code"),
    PROVIDE_DATES("You must provide both arrival and departure date"),
    PROVIDE_NAME("You must provide the guest full name"),
    PROVIDE_MAIL("You must provide the guest mail"),
    NOT_AVAILABLE("The campsite is not available in the period requested"),
    UPDATE_ONE_DATE("You can't update only one date"),
    EMPTY_UPDATE("At least one field of the reservation must be updated"),
    INVALID_RANGE("Departure date must be after arrival date"),
    ARRIVAL_DATE_TOO_EARLY("The campsite can be reserved minimum 1 day ahead of arrival"),
    ARRIVAL_DATE_TOO_LATE("The campsite can be reserved up to one month in advance"),
    MAX_RANGE_EXCEEDED("The max reservation time is 3 days"),
    AVAILABILITY_MISSING_PARAMETER("You must specify both 'from' and 'to' parameters or none of them");

    private String message;

    ErrorMessages(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}