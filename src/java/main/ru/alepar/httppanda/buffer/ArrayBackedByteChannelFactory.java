package ru.alepar.httppanda.buffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

public class ArrayBackedByteChannelFactory implements SizedByteChannelFactory {

    private final byte[] array;

    public ArrayBackedByteChannelFactory(byte[] array) {
        this.array = array;
    }

    @Override
    public long size() {
        return array.length;
    }

    @Override
    public ReadableByteChannel readChannel(long start) {
        return new ReadChannel((int) start, array.length);
    }

    @Override
    public WritableByteChannel writeChannel(long start) {
        return new WriteChannel((int) start, array.length);
    }

    @Override
    public ReadableByteChannel readChannel(long start, long end) {
        return new ReadChannel((int) start, (int)(end+1));
    }

    @Override
    public WritableByteChannel writeChannel(long start, long end) {
        return new WriteChannel((int) start, (int)(end+1));
    }

    private class ReadChannel extends BaseChannel implements ReadableByteChannel {
        public ReadChannel(int start, int end) {
            super(start, end);
        }

        @Override
        public int read(ByteBuffer dst) throws IOException {
            return transfer(dst, ByteBuffer::put);
        }
    }

    private class WriteChannel extends BaseChannel implements WritableByteChannel {
        private WriteChannel(int start, int end) {
            super(start, end);
        }

        @Override
        public int write(ByteBuffer src) throws IOException {
            return transfer(src, ByteBuffer::get);
        }
    }

    private class BaseChannel implements Channel {

        private final int end;
        protected int pos;

        private BaseChannel(int start, int end) {
            this.end = end;
            this.pos = start;
        }

        @Override
        public boolean isOpen() {
            return pos < end;
        }

        @Override
        public void close() { }

        protected int transfer(ByteBuffer buf, Lambda transfer) {
            if (!isOpen()) {
                return -1;
            }

            final int length = Math.min(buf.remaining(), end - pos);
            transfer.transfer(buf, array, pos, length);
            pos += length;
            return length;
        }
    }

    private interface Lambda {
        ByteBuffer transfer(ByteBuffer buf, byte[] array, int offset, int length);
    }

}
