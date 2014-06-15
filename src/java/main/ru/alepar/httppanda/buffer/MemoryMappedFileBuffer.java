package ru.alepar.httppanda.buffer;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;

public class MemoryMappedFileBuffer implements Buffer {

    private final Map<Integer, MappedByteBuffer> windows = new HashMap<>();

    private final int windowSize;
    private final FileChannel fc;

    public MemoryMappedFileBuffer(File file) {
        this(file, Integer.MAX_VALUE);
    }

    public MemoryMappedFileBuffer(File file, int windowSize) {
        this.windowSize = windowSize;

        try {
            fc = new RandomAccessFile(file, "rw").getChannel();
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

    private void slices(ByteBuffer buffer, long startPos, BufFunc f) {
        final long endPos = startPos + buffer.remaining() - 1;
        final int originalLimit = buffer.limit();
        final int startWindowIdx = (int)(startPos / windowSize);
        final int endWindowIdx = (int)(endPos / windowSize);

        try {
            for(int i = 0; i <= endWindowIdx-startWindowIdx; i++) {
                final ByteBuffer mem = getWindow(i + startWindowIdx);

                if (i == 0) {
                    mem.position((int) (startPos % windowSize));
                }

                final int length = Math.min(buffer.remaining(), mem.remaining());
                buffer.limit(buffer.position() + length);
                mem.limit(mem.position() + length);

                f.call(mem, buffer);
                buffer.limit(originalLimit);
            }
        } catch (Exception e) {
            throw new RuntimeException("failed to write to memory mapped file", e);
        }
    }

    @Override
    public void read(ByteBuffer buffer, long startPos) {
        slices(buffer, startPos, (s, b) -> b.put(s));
    }

    @Override
    public void write(ByteBuffer buffer, long startPos) {
        slices(buffer, startPos, (s, b) -> s.put(b));
    }

    private interface BufFunc {
        void call(ByteBuffer storage, ByteBuffer buffer);
    }

}
