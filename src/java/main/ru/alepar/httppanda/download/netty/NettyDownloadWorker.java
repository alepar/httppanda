package ru.alepar.httppanda.download.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import ru.alepar.httppanda.download.DownloadWorker;

public class NettyDownloadWorker implements DownloadWorker {

    private final Channel ch;
    private final DownloadHandler handler;

    public NettyDownloadWorker(Channel ch, DownloadHandler handler) {
        this.ch = ch;
        this.handler = handler;
    }

    @Override
    public ChannelFuture closeFuture() {
        return ch.closeFuture();
    }

    @Override
    public double getBytePerSec() {
        return handler.getBytePerSec();
    }
}
