package ru.alepar.httppanda.download;

import io.netty.channel.ChannelFuture;
import io.netty.handler.codec.http.HttpHeaders;

import java.util.concurrent.Future;

public interface DownloadWorker {
    void close();
    ChannelFuture closeFuture();
    double getBytePerSec();
    Future<HttpHeaders> getHeadersFuture();
}
