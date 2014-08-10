package ru.alepar.httppanda.predictor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

public class Predictor {

    private final LinearEquationSolver solver;

    public Predictor(LinearEquationSolver solver) {
        this.solver = solver;
    }

    public double predict(IntervalSet written, Collection<Cursor> readers, Collection<Cursor> writers) {
        final List<Point> marks = new ArrayList<>();

        for (Cursor reader : readers) {
            for (Cursor writer : writers) {
                final Point point = solver.solve(reader.position(), reader.speed(), writer.position(), writer.speed());
                if (point != null) {
                    marks.add(point);
                }
            }
        }

        Collections.sort(marks, (left, right) -> Double.compare(left.time, right.time));

        final SortedSet<Double> times = new TreeSet<>();
        for (Point mark : marks) {
            final double time = mark.time;

            for (Cursor writer : writers) {
                written.add((int) writer.position(), (int) (writer.position() + writer.speed() * time) - 1);
            }

            for (Cursor reader : readers) {
                final Integer absent = written.firstAbsentPointIn((int) reader.position(), (int) (reader.speed()*time));
                if(absent != null) {
                    times.add((absent - reader.position()) / reader.speed());
                }
            }

            if (!times.isEmpty()) {
                return times.first();
            }
        }

        return -1;
    }

}
