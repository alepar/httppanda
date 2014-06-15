package ru.alepar.httppanda.download;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import ru.alepar.httppanda.buffer.BufferChannel;
import ru.alepar.httppanda.buffer.FileBufferChannel;
import ru.alepar.httppanda.download.netty.NettyDownloadWorkerFactory;

import java.io.File;
import java.net.URI;

public class HttpClient {

    public static void main(String[] args) throws Exception {
        final URI uri = new URI("http://192.168.5.2:53907/Content/9B5CFF72D93D58BB0D13DF2141757335CA27238A-0.mkv");
        final BufferChannel bufferChannel = new FileBufferChannel(new File("video.mkv"));

        final EventLoopGroup group = new NioEventLoopGroup(1);
        try {
            final DownloadWorkerFactory downloadWorkerFactory = new NettyDownloadWorkerFactory(uri, group, bufferChannel);
            final DownloadWorker downWorker = downloadWorkerFactory.start(0);
            downWorker.closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }

}
