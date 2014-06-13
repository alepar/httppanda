package ru.alepar.httppanda;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;

public class MemoryMappedFileBuffer implements Buffer {

    private final Map<Integer, ByteBuffer> windows = new HashMap<>();

    private final int windowSize;
    private final FileChannel fc;

    public MemoryMappedFileBuffer(File file, int windowSize) {
        this.windowSize = windowSize;

        try {
            fc = new RandomAccessFile(file, "rw").getChannel();
        } catch (Exception e) {
            throw new RuntimeException("failed to create MemoryMappedFileBuffer", e);
        }
    }

    private synchronized ByteBuffer getWindow(int windowIdx) throws IOException {
        ByteBuffer window = windows.get(windowIdx);

        if (window == null) {
            window = fc.map(FileChannel.MapMode.READ_WRITE, (long) (windowIdx * windowSize), windowSize);
            windows.put(windowIdx, window);
        }

        window.position(0);
        return window;
    }

    private void slices(byte[] buffer, long startPos, BufFunc f) {
        final long endPos = startPos + buffer.length - 1;
        final int startWindowIdx = (int)(startPos / windowSize);
        final int endWindowIdx = (int)(endPos / windowSize);

        try {
            for(int i = 0; i <= endWindowIdx-startWindowIdx; i++) {
                final ByteBuffer mem = getWindow(i + startWindowIdx);

                final int offset;
                final int firstWindowOffset = (int) (startPos % windowSize);
                if (i == 0) {
                    mem.position(firstWindowOffset);
                    offset = 0;
                } else {
                    offset = i * windowSize - firstWindowOffset;
                }

                final int length = Math.min(buffer.length - offset, windowSize - mem.position());
                f.call(mem, buffer, offset, length);
            }
        } catch (Exception e) {
            throw new RuntimeException("failed to read from memory mapped file", e);
        }
    }

    @Override
    public void read(byte[] buffer, long startPos) {
        slices(buffer, startPos, ByteBuffer::get);
    }

    @Override
    public void write(byte[] buffer, long startPos) {
        slices(buffer, startPos, ByteBuffer::put);
    }

    private interface BufFunc {
        void call(ByteBuffer m, byte[] b, int o, int l);
    }

}
