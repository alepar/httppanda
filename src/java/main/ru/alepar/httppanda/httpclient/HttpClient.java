package ru.alepar.httppanda.httpclient;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import ru.alepar.httppanda.buffer.Buffer;
import ru.alepar.httppanda.buffer.FileChannelBuffer;
import ru.alepar.httppanda.httpclient.netty.NettyDownloadWorkerFactory;

import java.io.File;
import java.net.URI;

public class HttpClient {

    public static void main(String[] args) throws Exception {
        final URI uri = new URI("http://192.168.5.2:53907/Content/9B5CFF72D93D58BB0D13DF2141757335CA27238A-0.mkv");
        final Buffer buffer = new FileChannelBuffer(new File("video.mkv"));

        final EventLoopGroup group = new NioEventLoopGroup(1);
        try {
            final DownloadWorkerFactory downloadWorkerFactory = new NettyDownloadWorkerFactory(uri, group, buffer);
            final DownloadWorker downWorker = downloadWorkerFactory.start(0);
            downWorker.closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }

}
