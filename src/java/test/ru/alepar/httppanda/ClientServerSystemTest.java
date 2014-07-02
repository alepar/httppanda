package ru.alepar.httppanda;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import org.junit.Test;
import ru.alepar.httppanda.buffer.ArrayBackedByteChannelFactory;
import ru.alepar.httppanda.buffer.SizedByteChannelFactory;
import ru.alepar.httppanda.download.DownloadWorker;
import ru.alepar.httppanda.download.netty.NettyDownloadWorkerFactory;
import ru.alepar.httppanda.upload.BufferChannelServer;
import ru.alepar.httppanda.upload.netty.NettyBufferChannelServer;

import java.net.URI;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Random;

import static org.junit.Assert.*;

public class ClientServerSystemTest {

    public static final int DATA_SIZE = 16 * 1024*1024;
    public static final int PORT = 31337;

    @Test
    public void clientDownloadsExactlyTheSameBytesThatServerSends() throws Exception {
        final byte[] expectedBytes = createRandomByteArray(DATA_SIZE);
        final byte[] actualBytes = new byte[DATA_SIZE];

        final SizedByteChannelFactory actualBuffer = new ArrayBackedByteChannelFactory(actualBytes);
        final SizedByteChannelFactory expectedBuffer = new ArrayBackedByteChannelFactory(expectedBytes);

        final EventLoopGroup group = new NioEventLoopGroup(1);
        final BufferChannelServer server = new NettyBufferChannelServer(group, actualBuffer, PORT, new DefaultHttpHeaders());
        try {
            final DownloadWorker client = new NettyDownloadWorkerFactory(
                    new URI("http://127.0.0.1:" + PORT + "/"),
                    group,
                    expectedBuffer
            ).start(0);

            client.closeFuture().sync();

            assertTrue(Arrays.equals(actualBytes, expectedBytes));
        } finally {
            server.close();
            group.shutdownGracefully();
        }
    }

    @Test
    public void clientDownloadsRangeCorrectly() throws Exception {
        final int rangeLength = DATA_SIZE / 2;
        final int rangeStart = DATA_SIZE / 3;
        final int rangeEnd = rangeStart + rangeLength - 1;

        final byte[] expectedBytes = createRandomByteArray(DATA_SIZE);
        final byte[] actualBytes = new byte[DATA_SIZE];

        final SizedByteChannelFactory sendChannel = new ArrayBackedByteChannelFactory(actualBytes);
        final SizedByteChannelFactory receiveChannel = new ArrayBackedByteChannelFactory(expectedBytes);

        final EventLoopGroup group = new NioEventLoopGroup(1);
        final BufferChannelServer server = new NettyBufferChannelServer(group, sendChannel, PORT, new DefaultHttpHeaders());
        try {
            final DownloadWorker client = new NettyDownloadWorkerFactory(
                    new URI("http://127.0.0.1:" + PORT + "/"),
                    group,
                    receiveChannel
            ).start(rangeStart, rangeEnd);

            client.closeFuture().sync();

            assertTrue(Arrays.equals(
                    Arrays.copyOfRange(actualBytes, rangeStart, rangeEnd+1),
                    Arrays.copyOfRange(expectedBytes, rangeStart, rangeEnd+1)
            ));
        } finally {
            server.close();
            group.shutdownGracefully();
        }
    }

    public static byte[] createRandomByteArray(int length) {
        final Random random = new SecureRandom();
        final byte[] array = new byte[length];
        random.nextBytes(array);
        return array;
    }

}
