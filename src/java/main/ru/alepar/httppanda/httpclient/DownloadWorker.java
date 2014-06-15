package ru.alepar.httppanda.httpclient;

import io.netty.channel.ChannelFuture;

public interface DownloadWorker {
    ChannelFuture closeFuture();
}
