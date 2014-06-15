package ru.alepar.httppanda.download;

import io.netty.channel.ChannelFuture;

public interface DownloadWorker {
    ChannelFuture closeFuture();
}
