package ru.alepar.httppanda.buffer;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.HashMap;
import java.util.Map;

public class MemoryMappedFileByteChannelFactory implements SizedByteChannelFactory {

    private final Map<Integer, MappedByteBuffer> windows = new HashMap<>();

    private final int windowSize;
    private final FileChannel fc;
    private RandomAccessFile rw;

    public MemoryMappedFileByteChannelFactory(File file) {
        this(file, Integer.MAX_VALUE);
    }

    public MemoryMappedFileByteChannelFactory(File file, int windowSize) {
        this.windowSize = windowSize;

        try {
            rw = new RandomAccessFile(file, "rw");
            fc = rw.getChannel();
        } catch (Exception e) {
            throw new RuntimeException("failed to create MemoryMappedFileBuffer", e);
        }
    }

    private synchronized ByteBuffer getWindow(int windowIdx) throws IOException {
        MappedByteBuffer window = windows.get(windowIdx);

        if (window == null) {
            window = fc.map(FileChannel.MapMode.READ_WRITE, ((long)windowIdx) * windowSize, windowSize);
            windows.put(windowIdx, window);
        }

        return window.slice();
    }

    @Override
    public long size() {
        try {
            return rw.length();
        } catch (IOException e) {
            throw new RuntimeException("failed to read length of file", e);
        }
    }

    @Override
    public ReadableByteChannel readChannel(long start) {
        return new Channel(start, -1);
    }

    @Override
    public WritableByteChannel writeChannel(long start) {
        return new Channel(start, -1);
    }

    @Override
    public ReadableByteChannel readChannel(long start, long end) {
        return new Channel(start, end);
    }

    @Override
    public WritableByteChannel writeChannel(long start, long end) {
        return new Channel(start, end);
    }

    private class Channel implements ByteChannel {
        private final long end;
        private long pos;

        private Channel(long start, long end) {
            this.pos = start;
            this.end = end;
        }


        private int transfer(ByteBuffer buffer, Lambda f) {
            final int originalLimit = buffer.limit();
            final int startWindowIdx = (int)(pos / windowSize);
            final int endWindowIdx = (int)(endPos(buffer) / windowSize);

            int transferred = 0;

            try {
                for(int i = 0; i <= endWindowIdx-startWindowIdx; i++) {
                    final ByteBuffer mem = getWindow(i + startWindowIdx);

                    if (i == 0) {
                        mem.position((int) (pos % windowSize));
                    }

                    final int length = Math.min(buffer.remaining(), mem.remaining());
                    buffer.limit(buffer.position() + length);
                    mem.limit(mem.position() + length);

                    f.transfer(mem, buffer);
                    buffer.limit(originalLimit);

                    transferred += length;
                }

                pos += transferred;
                return transferred;
            } catch (Exception e) {
                throw new RuntimeException("failed to write to memory mapped file", e);
            }
        }

        private long endPos(ByteBuffer buf) {
            long endPos = pos + buf.remaining() - 1;
            if (end >= 0) {
                endPos = Math.min(endPos, end);
            }
            return endPos;
        }

        @Override
        public int read(ByteBuffer dst) throws IOException {
            return transfer(dst, (s, b) -> b.put(s));
        }

        @Override
        public int write(ByteBuffer src) throws IOException {
            return transfer(src, (s, b) -> s.put(b));
        }

        @Override
        public boolean isOpen() {
            return pos < size();
        }

        @Override
        public void close() { }
    }

    private interface Lambda {
        void transfer(ByteBuffer storage, ByteBuffer buffer);
    }

}
