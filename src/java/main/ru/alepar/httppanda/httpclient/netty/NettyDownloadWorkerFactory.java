package ru.alepar.httppanda.httpclient.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import ru.alepar.httppanda.buffer.Buffer;
import ru.alepar.httppanda.httpclient.DownloadWorker;
import ru.alepar.httppanda.httpclient.DownloadWorkerFactory;

import java.net.URI;

public class NettyDownloadWorkerFactory implements DownloadWorkerFactory {

    private final URI uri;
    private final EventLoopGroup group;
    private final Buffer buffer;

    public NettyDownloadWorkerFactory(URI uri, EventLoopGroup group, Buffer buffer) {
        this.uri = uri;
        this.group = group;
        this.buffer = buffer;
    }

    @Override
    public DownloadWorker start(long offset) {
        try {
            final DownloadHandler handler = new DownloadHandler(buffer);

            final Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) {
                            final ChannelPipeline p = ch.pipeline();
                            p.addLast(new HttpClientCodec());
                            p.addLast(handler);
                        }
                    });

            final Channel ch = bootstrap.connect(uri.getHost(), uri.getPort()).sync().channel();

            final HttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, uri.getRawPath());
            request.headers().set(HttpHeaders.Names.HOST, uri.getHost());
            request.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.CLOSE);

            ch.writeAndFlush(request);

            return new NettyDownloadWorker(ch, handler);
        } catch (Exception e) {
            throw new RuntimeException("failed to start new download worker", e);
        }
    }
}
