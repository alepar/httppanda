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
    private final File file;

    public FileByteChannelFactory(File file) {
        this.file = file;
        try {
            raw = new RandomAccessFile(file, "rw");
        } catch (Exception e) {
            throw new RuntimeException("failed to open file " + file.getAbsolutePath(), e);
        }
    }

    private ByteChannel openChannel(long start) {
        try {
            final FileChannel channel = new RandomAccessFile(file, "rw").getChannel();
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

        @Override
        public int read(ByteBuffer dst) throws IOException {
            if (remaining == 0) {
                return -1;
            }
            if (dst.remaining() == 0) {
                return 0;
            }

            final int limit = dst.limit();
            try {
                if (dst.remaining() > remaining) {
                    dst.limit((int) remaining); // safe to downcast here
                }
                final int read = byteChannel.read(dst);
                remaining -= read;
                return read;
            } catch(Exception e) {
                throw new RuntimeException("failed to read bytes from fileChannel", e);
            } finally {
                dst.limit(limit);
            }
        }

        @Override
        public int write(ByteBuffer src) throws IOException {
            if (src.remaining() == 0) {
                return 0;
            }

            final int limit = src.limit();
            try {
                if (src.remaining() > remaining) {
                    src.limit((int) remaining); // safe to downcast here
                }
                final int read = byteChannel.write(src);
                remaining -= read;
                return read;
            } catch(Exception e) {
                throw new RuntimeException("failed to write bytes to file channel", e);
            } finally {
                src.limit(limit);
            }
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

}
