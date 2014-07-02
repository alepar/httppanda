package ru.alepar.httppanda.download;

import org.junit.After;
import org.junit.Test;
import ru.alepar.httppanda.buffer.ByteChannelFactory;
import ru.alepar.httppanda.buffer.MemoryMappedFileByteChannelFactory;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.Arrays;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class MemoryMappedFileByteChannelFactoryTest {

    private final File file = createTempFile();

    @Test
    public void ifWriteSmallArrayYouCanReadItBack() throws Exception {
        final int SIZE = 102400;
        final ByteChannelFactory factory = new MemoryMappedFileByteChannelFactory(file, SIZE);

        final byte[] expected = createArray(SIZE);
        factory.writeChannel(0).write(ByteBuffer.wrap(expected));

        final byte[] actual = new byte[SIZE];
        factory.readChannel(0).read(ByteBuffer.wrap(actual));

        assertThat(Arrays.equals(actual, expected), equalTo(true));
    }

    @Test
    public void ifWriteAcrossBorderYouCanReadItBack() throws Exception {
        final int SIZE = 2;
        final ByteChannelFactory factory = new MemoryMappedFileByteChannelFactory(file, SIZE);

        final byte[] expected = createArray(SIZE*3);
        factory.writeChannel(0).write(ByteBuffer.wrap(expected));

        final byte[] actual = new byte[SIZE*3];
        factory.readChannel(0).read(ByteBuffer.wrap(actual));

        assertThat(Arrays.equals(actual, expected), equalTo(true));
    }

    @Test
    public void writingAndReadingBackWithBigArrayWorks() throws Exception {
        final ByteChannelFactory factory = new MemoryMappedFileByteChannelFactory(file, 1024*1024*32);

        final byte[] expected = createArray(1024*1024*128);
        factory.writeChannel(0).write(ByteBuffer.wrap(expected));

        final byte[] actual = new byte[1024*1024*128];
        factory.readChannel(0).read(ByteBuffer.wrap(actual));

        assertThat(Arrays.equals(actual, expected), equalTo(true));
    }

    @Test
    public void readWithOffsetWorksProperly() throws Exception {
        final int SIZE = 3;
        final ByteChannelFactory factory = new MemoryMappedFileByteChannelFactory(file, SIZE);

        final byte[] expected = createArray(SIZE*3);
        factory.writeChannel(0).write(ByteBuffer.wrap(expected));

        final byte[] actual = new byte[SIZE * 2];
        factory.readChannel(2).read(ByteBuffer.wrap(actual));

        for (int i = 0; i < actual.length; i++) {
            assertThat(actual[i], equalTo((byte)(i+2)));
        }
    }

    @Test
    public void writeWithOffsetWorksProperly() throws Exception {
        final int SIZE = 3;
        final ByteChannelFactory factory = new MemoryMappedFileByteChannelFactory(file, SIZE);

        final byte[] write = createArray(SIZE*2);
        factory.writeChannel(1).write(ByteBuffer.wrap(write));

        final byte[] actual = new byte[SIZE*3];
        factory.readChannel(0).read(ByteBuffer.wrap(actual));

        assertThat(Arrays.equals(actual, new byte[] { 0, 0, 1, 2, 3, 4, 5, 0, 0 }), equalTo(true));
    }

    @Test
    public void writingReallyBigFileGoesWithoutExceptions() throws Exception {
        final ByteChannelFactory factory = new MemoryMappedFileByteChannelFactory(file, 1024*1024*1024);

        final int size = 1024 * 1024 * 128;
        final byte[] array = createArray(size);

        final WritableByteChannel channel = factory.writeChannel(0);
        for(long i=0; i<8*4; i++) {
            channel.write(ByteBuffer.wrap(array));
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @After
    public void tearDown() throws Exception {
        file.delete();
    }

    private static byte[] createArray(int size) {
        final byte[] expected = new byte[size];
        for (int i = 0; i < expected.length; i++) {
            expected[i] = (byte)(i % 256);
        }
        return expected;
    }

    private static File createTempFile() {
        try {
            return File.createTempFile("MemoryMappedFileBufferTest", "");
        } catch(Exception e) {
            throw new RuntimeException("failed to create temp file", e);
        }
    }
}