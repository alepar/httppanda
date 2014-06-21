package ru.alepar.httppanda.download.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.LastHttpContent;
import ru.alepar.httppanda.buffer.BufferChannel;
import ru.alepar.httppanda.stat.BytePerSecStat;
import ru.alepar.httppanda.stat.IoStat;

import java.util.Map;

public class DownloadHandler extends SimpleChannelInboundHandler<HttpObject> {

    private final BufferChannel bufferChannel;
    private final IoStat bytePerSecStat = new BytePerSecStat();

    private volatile double bytePerSec;

    private long pos;

    public DownloadHandler(BufferChannel bufferChannel, long offset) {
        this.bufferChannel = bufferChannel;
        this.pos = offset;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
        if (msg instanceof HttpResponse) {
            HttpResponse response = (HttpResponse) msg;

            if (!response.headers().isEmpty()) {
                for (Map.Entry<String, String> entry : response.headers()) {
                    System.err.println("HEADER: " + entry.getKey() + " = " + entry.getValue());
                }
            }
        } else if (msg instanceof HttpContent) {
            HttpContent content = (HttpContent) msg;

            final ByteBuf byteBuf = content.content();
            bufferChannel.write(byteBuf.nioBuffer(), pos);

            final int length = byteBuf.readableBytes();
            pos += length;
            bytePerSecStat.add(length);
            bytePerSec = bytePerSecStat.get();

            if (content instanceof LastHttpContent) {
                ctx.close();
            }
        }
    }

    public double getBytePerSec() {
        return bytePerSec;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}