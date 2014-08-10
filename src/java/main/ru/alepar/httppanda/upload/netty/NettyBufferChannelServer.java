package ru.alepar.httppanda.upload.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpChunkedInput;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedNioStream;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.alepar.httppanda.buffer.SizedByteChannelFactory;
import ru.alepar.httppanda.upload.BufferChannelServer;

import java.io.IOException;

import static io.netty.handler.codec.http.HttpHeaders.Names.*;
import static io.netty.handler.codec.http.HttpMethod.*;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.*;

public class NettyBufferChannelServer implements BufferChannelServer {

    private static final Logger log = LoggerFactory.getLogger(NettyBufferChannelServer.class);

    private final Channel serverChannel;
    private final SizedByteChannelFactory channelFactory;
    private final HttpHeaders headers;

    public NettyBufferChannelServer(NioEventLoopGroup group, SizedByteChannelFactory channelFactory, int port, HttpHeaders headers) {
        this.channelFactory = channelFactory;
        this.headers = headers;
        try {
            final ServerBootstrap b = new ServerBootstrap();
            b.group(group)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            final ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new HttpServerCodec());
                            pipeline.addLast(new HttpObjectAggregator(102400));
                            pipeline.addLast(new ChunkedWriteHandler());
                            pipeline.addLast(new Handler());
                        }
                    });

            serverChannel = b.bind(port).sync().channel();
            log.debug("listening at {}", serverChannel.localAddress());
        } catch (Exception e) {
            throw new RuntimeException("failed to start server", e);
        }
    }

    @Override
    public void close() {
        try {
            serverChannel.close().sync();
        } catch (InterruptedException e) {
            throw new RuntimeException("interrupted while closing", e);
        }
    }

    private class Handler extends SimpleChannelInboundHandler<FullHttpRequest> {

        private long start;
        private long end;

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
            if (!request.getDecoderResult().isSuccess()) {
                sendError(ctx, BAD_REQUEST);
                return;
            }

            final String rangeHeader = request.headers().get("Range");
            final Range range;
            if (rangeHeader == null) {
                range = new Range("bytes=0-");
            } else {
                range = new Range(rangeHeader);
            }

            if (request.getMethod() != GET) {
                sendError(ctx, METHOD_NOT_ALLOWED);
                return;
            }

            final long totalLength = channelFactory.size();

            start = range.start;
            end = range.end == null ? totalLength-1 : range.end;
            final long contentLength = end - start + 1;

            final HttpResponse response = new DefaultHttpResponse(HTTP_1_1, OK);
            response.headers().add(headers);
            response.headers().set("Content-Range", String.format("bytes %d-%d/%d", start, end, totalLength));
            response.headers().set("Content-Length", contentLength);

            log.debug("started channel: {}, range {}-{}", ctx.channel(), start, end);
            ctx.write(response);

            ctx.writeAndFlush(new HttpChunkedInput(new ChunkedNioStream(channelFactory.readChannel(start, end), 102400)))
                    .addListener(f -> {
                        log.debug("transfer complete {}", ctx.channel());
                        if (ctx.channel().isActive()) {
                            ctx.channel().close();
                        }
                    });
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            if (ctx.channel().isActive()) {
                ctx.channel().close();
            }
            if (cause instanceof IOException) {
                log.debug("io exception in channel {}", ctx.channel());
            } else {
                log.warn("exception in channel " + ctx.channel(), cause);
            }
        }

    }

    private static void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
        final FullHttpResponse response = new DefaultFullHttpResponse(
                HTTP_1_1, status, Unpooled.copiedBuffer("Failure: " + status + "\r\n", CharsetUtil.UTF_8)
        );
        response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
        log.info("sent error {} to channel {}", status, ctx.channel());
    }

}
