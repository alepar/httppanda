package ru.alepar.httppanda.download;

import org.junit.After;
import org.junit.Test;
import ru.alepar.httppanda.buffer.BufferChannel;
import ru.alepar.httppanda.buffer.MemoryMappedFileBufferChannel;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.Arrays;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class MemoryMappedFileBufferChannelTest {

    private final File file = createTempFile();

    @Test
    public void ifWriteSmallArrayYouCanReadItBack() throws Exception {
        final int SIZE = 102400;
        final BufferChannel bufferChannel = new MemoryMappedFileBufferChannel(file, SIZE);

        final byte[] expected = createArray(SIZE);
        bufferChannel.write(ByteBuffer.wrap(expected), 0);

        final byte[] actual = new byte[SIZE];
        bufferChannel.read(ByteBuffer.wrap(actual), 0);

        assertThat(Arrays.equals(actual, expected), equalTo(true));
    }

    @Test
    public void ifWriteAcrossBorderYouCanReadItBack() throws Exception {
        final int SIZE = 2;
        final BufferChannel bufferChannel = new MemoryMappedFileBufferChannel(file, SIZE);

        final byte[] expected = createArray(SIZE*3);
        bufferChannel.write(ByteBuffer.wrap(expected), 0);

        final byte[] actual = new byte[SIZE*3];
        bufferChannel.read(ByteBuffer.wrap(actual), 0);

        assertThat(Arrays.equals(actual, expected), equalTo(true));
    }

    @Test
    public void writingAndReadingBackWithBigArrayWorks() throws Exception {
        final BufferChannel bufferChannel = new MemoryMappedFileBufferChannel(file, 1024*1024*32);

        final byte[] expected = createArray(1024*1024*128);
        bufferChannel.write(ByteBuffer.wrap(expected), 0);

        final byte[] actual = new byte[1024*1024*128];
        bufferChannel.read(ByteBuffer.wrap(actual), 0);

        assertThat(Arrays.equals(actual, expected), equalTo(true));
    }

    @Test
    public void readWithOffsetWorksProperly() throws Exception {
        final int SIZE = 3;
        final BufferChannel bufferChannel = new MemoryMappedFileBufferChannel(file, SIZE);

        final byte[] expected = createArray(SIZE*3);
        bufferChannel.write(ByteBuffer.wrap(expected), 0);

        final byte[] actual = new byte[SIZE * 2];
        bufferChannel.read(ByteBuffer.wrap(actual), 2);

        for (int i = 0; i < actual.length; i++) {
            assertThat(actual[i], equalTo((byte)(i+2)));
        }
    }

    @Test
    public void writeWithOffsetWorksProperly() throws Exception {
        final int SIZE = 3;
        final BufferChannel bufferChannel = new MemoryMappedFileBufferChannel(file, SIZE);

        final byte[] write = createArray(SIZE*2);
        bufferChannel.write(ByteBuffer.wrap(write), 1);

        final byte[] actual = new byte[SIZE*3];
        bufferChannel.read(ByteBuffer.wrap(actual), 0);

        assertThat(Arrays.equals(actual, new byte[] { 0, 0, 1, 2, 3, 4, 5, 0, 0 }), equalTo(true));
    }

    @Test
    public void writingReallyBigFileGoesWithoutExceptions() throws Exception {
        final BufferChannel bufferChannel = new MemoryMappedFileBufferChannel(file, 1024*1024*1024);

        final int size = 1024 * 1024 * 128;
        final byte[] array = createArray(size);

        for(long i=0; i<8*4; i++) {
            bufferChannel.write(ByteBuffer.wrap(array), i * size);
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