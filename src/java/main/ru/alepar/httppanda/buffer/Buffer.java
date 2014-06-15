package ru.alepar.httppanda.buffer;

public interface Buffer {
    void read(byte[] buffer, long start);
    void write(byte[] buffer, long start);
}
