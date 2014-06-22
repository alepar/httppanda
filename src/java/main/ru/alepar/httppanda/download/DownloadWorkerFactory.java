package ru.alepar.httppanda.download;

public interface DownloadWorkerFactory {

    DownloadWorker start(long start);
    DownloadWorker start(long start, long end);

}
