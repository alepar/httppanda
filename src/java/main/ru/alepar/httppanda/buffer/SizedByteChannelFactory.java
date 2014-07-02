package ru.alepar.httppanda.buffer;

public interface SizedByteChannelFactory extends ByteChannelFactory {
    long size();
}
