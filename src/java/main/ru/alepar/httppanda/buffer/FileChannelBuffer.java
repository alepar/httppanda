package ru.alepar.httppanda.buffer;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class FileChannelBuffer implements Buffer {

    private final FileChannel channel;

    public FileChannelBuffer(File file) {
        try {
            channel = new RandomAccessFile(file, "rw").getChannel();
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
}
