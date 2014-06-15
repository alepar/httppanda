package ru.alepar.httppanda.stat;

public interface IoStat {
    void add(long length);
    double get();
}
