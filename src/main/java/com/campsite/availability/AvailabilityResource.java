package com.campsite.availability;

import org.springframework.hateoas.ResourceSupport;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AvailabilityResource extends ResourceSupport {

    private TimeRangeResource requestedRange;

    private List<TimeRangeResource> availableRanges = new ArrayList<>();

    public TimeRangeResource getRequestedRange() {
        return requestedRange;
    }

    public void setRequestedRange(TimeRangeResource requestedRange) {
        this.requestedRange = requestedRange;
    }

    public List<TimeRangeResource> getAvailableRanges() {
        return availableRanges;
    }

    public void setAvailableRanges(List<TimeRangeResource> availableRanges) {
        this.availableRanges = availableRanges;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        AvailabilityResource that = (AvailabilityResource) o;
        return Objects.equals(requestedRange, that.requestedRange) &&
                Objects.equals(availableRanges, that.availableRanges);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), requestedRange, availableRanges);
    }

    @Override
    public String toString() {
        return "AvailabilityResource{" +
                "requestedRange=" + requestedRange +
                ", availableRanges=" + availableRanges +
                '}';
    }
}
