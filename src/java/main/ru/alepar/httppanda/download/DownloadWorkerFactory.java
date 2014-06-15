package ru.alepar.httppanda.download;

public interface DownloadWorkerFactory {

    DownloadWorker start(long offset);

}
