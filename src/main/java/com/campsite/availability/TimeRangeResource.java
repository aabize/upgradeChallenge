package com.campsite.availability;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

public class TimeRangeResource implements Serializable {

    private LocalDate from;

    private LocalDate to;

    public TimeRangeResource(LocalDate from, LocalDate to) {
        this.from = from;
        this.to = to;
    }

    public LocalDate getFrom() {
        return from;
    }

    public void setFrom(LocalDate from) {
        this.from = from;
    }

    public LocalDate getTo() {
        return to;
    }

    public void setTo(LocalDate to) {
        this.to = to;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimeRangeResource that = (TimeRangeResource) o;
        return Objects.equals(from, that.from) &&
                Objects.equals(to, that.to);
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, to);
    }

    @Override
    public String toString() {
        return "TimeRangeResource{" +
                "from=" + from +
                ", to=" + to +
                '}';
    }
}
