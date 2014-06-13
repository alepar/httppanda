package ru.alepar.httppanda;

public interface Buffer {
    void read(byte[] buffer, long start);
    void write(byte[] buffer, long start);
}
