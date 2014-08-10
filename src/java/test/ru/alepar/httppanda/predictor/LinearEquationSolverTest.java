package ru.alepar.httppanda.predictor;

import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class LinearEquationSolverTest {

    private final LinearEquationSolver solver = new LinearEquationSolver();

    @Test
    public void whenReaderFasterThanWriterReturnsTimeWhenTheyWillMeet() throws Exception {
        final Point solution = solver.solve(0, 2, 5, 1);
        assertThat(solution, notNullValue());
        assertThat(solution.time, equalTo(5.0));
        assertThat(solution.position, equalTo(10.0));
    }

    @Test
    public void whenWriterFasterThanReaderReturnsNull() throws Exception {
        final Point solution = solver.solve(0, 1, 5, 2);
        assertThat(solution, nullValue());
    }

    @Test
    public void whenSpeedsAreEqualStillReturnsNull() throws Exception {
        final Point solution = solver.solve(0, 1, 5, 2);
        assertThat(solution, nullValue());
    }

    @Test
    public void ifWriterIsBehindReturnsNull() throws Exception {
        final Point solution = solver.solve(5, 1, 0, 2);
        assertThat(solution, nullValue());
    }
}
