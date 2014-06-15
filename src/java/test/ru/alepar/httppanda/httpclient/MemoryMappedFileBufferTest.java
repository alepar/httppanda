package ru.alepar.httppanda.httpclient;

import org.junit.After;
import org.junit.Test;
import ru.alepar.httppanda.buffer.Buffer;
import ru.alepar.httppanda.buffer.MemoryMappedFileBuffer;

import java.io.File;
import java.util.Arrays;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class MemoryMappedFileBufferTest {

    private final File file = createTempFile();

    @Test
    public void ifWriteSmallArrayYouCanReadItBack() throws Exception {
        final int SIZE = 102400;
        final Buffer buffer = new MemoryMappedFileBuffer(file, SIZE);

        final byte[] expected = createArray(SIZE);
        buffer.write(expected, 0);

        final byte[] actual = new byte[SIZE];
        buffer.read(actual, 0);

        assertThat(Arrays.equals(actual, expected), equalTo(true));
    }

    @Test
    public void ifWriteAcrossBorderYouCanReadItBack() throws Exception {
        final int SIZE = 2;
        final Buffer buffer = new MemoryMappedFileBuffer(file, SIZE);

        final byte[] expected = createArray(SIZE*3);
        buffer.write(expected, 0);

        final byte[] actual = new byte[SIZE*3];
        buffer.read(actual, 0);

        assertThat(Arrays.equals(actual, expected), equalTo(true));
    }

    @Test
    public void writingAndReadingBackWithBigArrayWorks() throws Exception {
        final Buffer buffer = new MemoryMappedFileBuffer(file, 1024*1024*32);

        final byte[] expected = createArray(1024*1024*128);
        buffer.write(expected, 0);

        final byte[] actual = new byte[1024*1024*128];
        buffer.read(actual, 0);

        assertThat(Arrays.equals(actual, expected), equalTo(true));
    }

    @Test
    public void readWithOffsetWorksProperly() throws Exception {
        final int SIZE = 3;
        final Buffer buffer = new MemoryMappedFileBuffer(file, SIZE);

        final byte[] expected = createArray(SIZE*3);
        buffer.write(expected, 0);

        final byte[] actual = new byte[SIZE * 2];
        buffer.read(actual, 2);

        for (int i = 0; i < actual.length; i++) {
            assertThat(actual[i], equalTo((byte)(i+2)));
        }
    }

    @Test
    public void writeWithOffsetWorksProperly() throws Exception {
        final int SIZE = 3;
        final Buffer buffer = new MemoryMappedFileBuffer(file, SIZE);

        final byte[] write = createArray(SIZE*2);
        buffer.write(write, 1);

        final byte[] actual = new byte[SIZE*3];
        buffer.read(actual, 0);

        assertThat(Arrays.equals(actual, new byte[] { 0, 0, 1, 2, 3, 4, 5, 0, 0 }), equalTo(true));
    }

    @Test
    public void writingReallyBigFileGoesWithoutExceptions() throws Exception {
        final Buffer buffer = new MemoryMappedFileBuffer(file, 1024*1024*1024);

        final int size = 1024 * 1024 * 128;
        final byte[] array = createArray(size);

        for(long i=0; i<8*4; i++) {
            buffer.write(array, i * size);
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