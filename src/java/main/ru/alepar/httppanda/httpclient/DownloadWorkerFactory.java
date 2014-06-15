package ru.alepar.httppanda.httpclient;

public interface DownloadWorkerFactory {

    DownloadWorker start(long offset);

}
