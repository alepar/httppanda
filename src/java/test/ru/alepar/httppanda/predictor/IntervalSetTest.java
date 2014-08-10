package ru.alepar.httppanda.predictor;

import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class IntervalSetTest {

    private final IntervalSet intervals = new IntervalSet();

    @Test
    public void noAbsentPointsBetweenTwoIntersectingIntervals() throws Exception {
        intervals.add(0, 15);
        intervals.add(10, 20);

        assertThat(intervals.firstAbsentPointIn(0, 20), nullValue());
    }

    @Test
    public void absentPointBetweenTwoNonIntersectingIntervals() throws Exception {
        intervals.add(0, 8);
        intervals.add(10, 20);

        assertThat(intervals.firstAbsentPointIn(0, 20), equalTo(9));
    }

    @Test
    public void noAbsentPointInASpecificCase() throws Exception {
        intervals.add(0, 8);
        intervals.add(10, 20);
        intervals.add(0, 9);

        assertThat(intervals.firstAbsentPointIn(0, 20), nullValue());
    }
}
