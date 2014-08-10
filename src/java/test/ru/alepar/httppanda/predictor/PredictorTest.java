package ru.alepar.httppanda.predictor;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class PredictorTest {

    private final Predictor predictor = new Predictor(new LinearEquationSolver());

    @Test
    public void simpleOneReaderBehindOneWriterWithIntervalFilledInBetween() throws Exception {
        final List<Cursor> readers = new ArrayList<>();
        readers.add(new StaticCursor(0, 10));
        final List<Cursor> writers = new ArrayList<>();
        writers.add(new StaticCursor(10, 5));
        final IntervalSet written = new IntervalSet();
        written.add(0, 10);

        final double time = predictor.predict(written, readers, writers);
        assertThat(time, equalTo(2.0));
    }

    @Test
    public void oneReaderBehindManyWriters() throws Exception {
        final List<Cursor> readers = new ArrayList<>();
        readers.add(new StaticCursor(0, 2));
        final List<Cursor> writers = new ArrayList<>();
        writers.add(new StaticCursor(4, 1));
        writers.add(new StaticCursor(8, 1));
        writers.add(new StaticCursor(16, 1));
        final IntervalSet written = new IntervalSet();
        written.add(0, 3);

        final double time = predictor.predict(written, readers, writers);
        assertThat(time, equalTo(16.0));
    }

    private class StaticCursor implements Cursor {
        private final double position;
        private final double speed;

        public StaticCursor(double position, double speed) {
            this.position = position;
            this.speed = speed;
        }

        @Override
        public double position() {
            return position;
        }

        @Override
        public double speed() {
            return speed;
        }
    }
}
