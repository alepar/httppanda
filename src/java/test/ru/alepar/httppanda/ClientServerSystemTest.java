package ru.alepar.httppanda;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import org.junit.Test;
import ru.alepar.httppanda.buffer.ArrayBackedBufferChannel;
import ru.alepar.httppanda.buffer.SizedBufferChannel;
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

        final SizedBufferChannel actualBuffer = new ArrayBackedBufferChannel(actualBytes);
        final SizedBufferChannel expectedBuffer = new ArrayBackedBufferChannel(expectedBytes);

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

    public static byte[] createRandomByteArray(int length) {
        final Random random = new SecureRandom();
        final byte[] array = new byte[length];
        random.nextBytes(array);
        return array;
    }

}
