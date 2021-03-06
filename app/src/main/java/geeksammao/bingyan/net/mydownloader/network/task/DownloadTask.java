package geeksammao.bingyan.net.mydownloader.network.task;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import geeksammao.bingyan.net.mydownloader.network.HttpUtil;
import geeksammao.bingyan.net.mydownloader.network.MultiThreadManager;
import geeksammao.bingyan.net.mydownloader.network.result.RequestResult;
import geeksammao.bingyan.net.mydownloader.util.Logger;

/**
 * Created by Geeksammao on 10/11/15.
 */
public class DownloadTask extends BaseTask {
    private MultiThreadManager threadManager;

    private String targetUrl;
    private File saveDir;
    private long startPosition;
    private long block;
    private long downloadedLength;
    private int threadID;
    private int startTimes;
    private boolean isFinished;
    private boolean isTaskFailed;

    public DownloadTask(MultiThreadManager threadManager, String targetUrl, File saveDir, long block, long downloadedLength
            , int threadID, int startTimes) {
        this.threadManager = threadManager;
        this.targetUrl = targetUrl;
        this.saveDir = saveDir;
        this.block = block;
        this.downloadedLength = downloadedLength;
        this.threadID = threadID;
        this.startPosition = block * (threadID - 1) + downloadedLength;
        this.startTimes = startTimes;
    }

    @Override
    void startTask() {
        startTimes++;

        if (startTimes <= 2) {
            Logger.logString(this, "Thread " + Integer.toString(threadID) + " is start");
            HttpUtil httpUtil = HttpUtil.getInstance();
            long endPosition = block * threadID - 1;
            httpUtil.setStartPosition(startPosition);
            httpUtil.setEndPosition(endPosition);

            try {
                RequestResult<InputStream> result = httpUtil.getInputStream(targetUrl);
                if (result.getStatus() == HttpUtil.HTTP_PARTIAL) {
                    InputStream inputStream = result.getData();
                    try {
                        writeStreamToFile(inputStream);
                    } catch (Exception e) {
                        e.printStackTrace();
                        isTaskFailed = true;
//                        return;
//                        new DownloadTask(threadManager, targetUrl, saveDir, block, downloadedLength, threadID, startTimes).start();
                    } finally {
                        try {
                            inputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    isFinished = true;
                    Logger.logString(this, "Thread " + Integer.toString(threadID) + "'s task is finished");
                } else {
                    Log.e("sam", "download error with error code " + result.getStatus());
                    isTaskFailed = true;
                }
            } catch (Exception e) {
                e.printStackTrace();
                isTaskFailed = true;
            }
        }
    }

    private void writeStreamToFile(InputStream inputStream) throws IOException {
        byte[] buffer = new byte[50 * 1024];
        int length;
        RandomAccessFile randomAccessFile;

        randomAccessFile = new RandomAccessFile(this.saveDir, "rwd");
        randomAccessFile.seek(this.startPosition);

        BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream, 50 * 1024);
        while (!threadManager.isExist() &&
                (length = inputStream.read(buffer, 0, buffer.length)) != -1) {
            randomAccessFile.write(buffer, 0, length);
            downloadedLength += length;
            threadManager.setDownloadedLength(threadID, downloadedLength);
            threadManager.appendDownloadedLength(length);

            threadManager.updateDB();
        }
        randomAccessFile.close();
        inputStream.close();
    }

    public long getDownloadedLength() {
        return downloadedLength;
    }

    public boolean isTaskFailed() {
        return isTaskFailed;
    }

    public boolean isFinished() {
        return isFinished;
    }
}
