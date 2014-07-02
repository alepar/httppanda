package ru.alepar.httppanda.buffer;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

public class FileByteChannelFactory implements SizedByteChannelFactory {

    private final RandomAccessFile raw;

    public FileByteChannelFactory(File file) {
        try {
            raw = new RandomAccessFile(file, "rw");
        } catch (Exception e) {
            throw new RuntimeException("failed to open file " + file.getAbsolutePath(), e);
        }
    }

    private ByteChannel openChannel(long start) {
        try {
            final FileChannel channel = raw.getChannel();
            channel.position(start);
            return channel;
        } catch (IOException e) {
            throw new RuntimeException("failed to open file channel", e);
        }
    }

    @Override
    public ReadableByteChannel readChannel(long start) {
        return openChannel(start);
    }

    @Override
    public WritableByteChannel writeChannel(long start) {
        return openChannel(start);
    }

    @Override
    public ReadableByteChannel readChannel(long start, long end) {
        return new LimitedChannel(openChannel(start), end-start+1);
    }

    @Override
    public WritableByteChannel writeChannel(long start, long end) {
        return new LimitedChannel(openChannel(start), end-start+1);
    }

    @Override
    public long size() {
        try {
            return raw.length();
        } catch (IOException e) {
            throw new RuntimeException("failed to get file size", e);
        }
    }

    private class LimitedChannel implements ByteChannel {
        private final ByteChannel byteChannel;

        private long remaining;

        private LimitedChannel(ByteChannel byteChannel, long length) {
            this.byteChannel = byteChannel;
            this.remaining = length;
        }

        private int transfer(ByteBuffer buf, Lambda transfer) {
            final int limit = buf.limit();
            try {
                if (buf.remaining() > remaining) {
                    buf.limit((int) remaining); // safe to downcast here
                }
                final int read = transfer.transfer(byteChannel, buf);
                remaining -= read;
                return read;
            } catch(Exception e) {
                throw new RuntimeException("failed to transfer bytes through FileChannel", e);
            } finally {
                buf.limit(limit);
            }
        }


        @Override
        public int read(ByteBuffer dst) throws IOException {
            return transfer(dst, (c, b) -> c.read(b));
        }

        @Override
        public int write(ByteBuffer src) throws IOException {
            return transfer(src, (c, b) -> c.write(b));
        }

        @Override
        public boolean isOpen() {
            return remaining > 0;
        }

        @Override
        public void close() throws IOException {
            byteChannel.close();
        }
    }

    private interface Lambda {
        int transfer(ByteChannel byteChannel, ByteBuffer buffer) throws IOException;
    }
}
