package ru.alepar.httppanda.download;

import io.netty.channel.EventLoopGroup;
import io.netty.handler.codec.http.HttpHeaders;
import ru.alepar.httppanda.buffer.BufferChannel;
import ru.alepar.httppanda.download.netty.NettyDownloadWorkerFactory;

import java.net.URI;
import java.util.concurrent.ExecutionException;

public class FileDownloader {

    private final EventLoopGroup group;

    public FileDownloader(EventLoopGroup group) {
        this.group = group;
    }

    public void downloadFile(URI uri, int nWorkers, BufferChannel bufferChannel) {
        try {
            final DownloadWorkerFactory downloadWorkerFactory = new NettyDownloadWorkerFactory(uri, group, bufferChannel);

            final long contentLength = getContentLength(downloadWorkerFactory);
            final long slice = contentLength / nWorkers;

            final DownloadWorker[] workers = new DownloadWorker[nWorkers];
            int i;
            for (i = 0; i < nWorkers-1; i++) {
                workers[i] = downloadWorkerFactory.start(i * slice, (i+1)*slice-1);
            }
            workers[i] = downloadWorkerFactory.start(i*slice);

            while(!isDone(workers)) {
                double bytesPerSec = 0;
                for (DownloadWorker worker : workers) {
                    bytesPerSec += worker.getBytePerSec();
                }
                System.out.print(String.format("%.4fMiB/s%c", bytesPerSec/1024.0/1024, (char)13));
                Thread.sleep(500L);
            }
        } catch (Exception e) {
            throw new RuntimeException("failed to download uri", e);
        }
    }

    private static boolean isDone(DownloadWorker[] workers) {
        for (DownloadWorker worker : workers) {
            if (!worker.closeFuture().isDone()) {
                return false;
            }
        }

        return true;
    }

    private static long getContentLength(DownloadWorkerFactory downloadWorkerFactory) throws InterruptedException, ExecutionException {
        final DownloadWorker worker = downloadWorkerFactory.start(0);
        final HttpHeaders headers = worker.getHeadersFuture().get();
        worker.close();

        return Long.valueOf(headers.get("Content-Length"));
    }
}
