package ru.alepar.httppanda.predictor;

import java.util.NavigableSet;
import java.util.TreeSet;

public class IntervalSet {

    private final NavigableSet<Interval> intervals = new TreeSet<>((left, right) -> Integer.compare(left.start, right.start));

    public void add(int start, int end) {
        final Interval interval = new Interval(start, end);
        final Interval floor = intervals.floor(interval);

        Interval toAdd;
        if (floor == null) {
            toAdd = interval;
        } else if (floor.end >= interval.start || floor.end+1 == interval.start) {
            intervals.remove(floor);
            toAdd = new Interval(floor.start, interval.end);
        } else {
            toAdd = interval;
        }

        final Interval higher = intervals.higher(toAdd);
        if (higher != null && (toAdd.end >= higher.start || higher.start == toAdd.end+1)) {
            intervals.remove(higher);
            toAdd = new Interval(toAdd.start, higher.end);
        }

        intervals.add(toAdd);
    }

    public Integer firstAbsentPointIn(int start, int end) {
        final Interval interval = new Interval(start, end);
        final Interval floor = intervals.floor(interval);

        if (floor == null) {
            return interval.start;
        } else if (floor.end >= interval.end) {
            return null;
        } else {
            return Math.max(interval.start, floor.end+1);
        }
    }

    private static class Interval {
        private final int start;
        private final int end;

        private Interval(int start, int end) {
            this.start = start;
            this.end = end;
        }
    }
}
