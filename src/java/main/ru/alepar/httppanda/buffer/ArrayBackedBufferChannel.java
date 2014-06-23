package ru.alepar.httppanda.buffer;

import java.nio.ByteBuffer;

public class ArrayBackedBufferChannel implements SizedBufferChannel {

    private final byte[] array;

    public ArrayBackedBufferChannel(byte[] array) {
        this.array = array;
    }

    @Override
    public void read(ByteBuffer buffer, long start) {
        if (start > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("this doesn't support long offsets");
        }

        buffer.put(array, (int)start, buffer.remaining());
    }

    @Override
    public void write(ByteBuffer buffer, long start) {
        if (start > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("this doesn't support long offsets");
        }

        buffer.get(array, (int)start, buffer.remaining());
    }

    @Override
    public long size() {
        return array.length;
    }
}
