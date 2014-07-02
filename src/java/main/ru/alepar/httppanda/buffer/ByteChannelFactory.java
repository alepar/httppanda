package ru.alepar.httppanda.buffer;

import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

public interface ByteChannelFactory {
    ReadableByteChannel readChannel(long start);
    WritableByteChannel writeChannel(long start);

    ReadableByteChannel readChannel(long start, long end);
    WritableByteChannel writeChannel(long start, long end);
}
