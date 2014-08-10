package ru.alepar.httppanda.predictor;

public class LinearEquationSolver {

    public Point solve(double readerPos, double readerSpeed, double writerPos, double writerSpeed) {
        if(writerPos < readerPos) {
            return null;
        }

        if(readerPos == writerPos) {
            if (readerSpeed > writerSpeed) {
                return new Point(0, readerPos);
            } else {
                return null;
            }
        }

        if (writerSpeed >= readerSpeed) {
            return null;
        }

        final double time = (writerPos-readerPos)/(readerSpeed-writerSpeed);
        return new Point(time, readerPos + readerSpeed*time);
    }
}
