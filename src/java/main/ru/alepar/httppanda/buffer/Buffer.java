package ru.alepar.httppanda.buffer;

import java.nio.ByteBuffer;

public interface Buffer {
    void read(ByteBuffer buffer, long start);
    void write(ByteBuffer buffer, long start);
}
