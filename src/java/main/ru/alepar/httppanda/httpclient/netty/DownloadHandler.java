package ru.alepar.httppanda.httpclient.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.LastHttpContent;
import ru.alepar.httppanda.buffer.Buffer;
import ru.alepar.httppanda.stat.BytePerSecStat;
import ru.alepar.httppanda.stat.IoStat;

import java.util.Map;

public class DownloadHandler extends SimpleChannelInboundHandler<HttpObject> {

    private final Buffer buffer;
    private final IoStat mibPerSecStat = new BytePerSecStat();

    private long pos;

    public DownloadHandler(Buffer buffer) {
        this.buffer = buffer;
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
            final byte[] bytes = new byte[byteBuf.capacity()];
            byteBuf.getBytes(0, bytes);
            buffer.write(bytes, pos);
            pos += bytes.length;

            mibPerSecStat.add(bytes.length);
            System.out.print(String.format("%.4fMiB/s%c", mibPerSecStat.get()/1024.0/1024, (char)13));

            if (content instanceof LastHttpContent) {
                ctx.close();
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}