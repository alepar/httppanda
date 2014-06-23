package ru.alepar.httppanda.buffer;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class FileBufferChannel implements SizedBufferChannel {

    private final FileChannel channel;
    private final RandomAccessFile raw;

    public FileBufferChannel(File file) {
        try {
            raw = new RandomAccessFile(file, "rw");
            channel = raw.getChannel();
        } catch (Exception e) {
            throw new RuntimeException("failed to open file " + file.getAbsolutePath(), e);
        }
    }

    @Override
    public void read(ByteBuffer buffer, long start) {
        try {
            channel.read(buffer, start);
        } catch (Exception e) {
            throw new RuntimeException("failed to read from file", e);
        }
    }

    @Override
    public void write(ByteBuffer buffer, long start) {
        try {
            channel.write(buffer, start);
        } catch (IOException e) {
            throw new RuntimeException("failed to write to file", e);
        }
    }

    @Override
    public long size() {
        try {
            return raw.length();
        } catch (IOException e) {
            throw new RuntimeException("failed to get file size", e);
        }
    }
}
