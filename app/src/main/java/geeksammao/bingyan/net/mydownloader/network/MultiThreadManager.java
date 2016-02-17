package geeksammao.bingyan.net.mydownloader.network;

import android.accounts.NetworkErrorException;
import android.app.Activity;
import android.content.Context;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import geeksammao.bingyan.net.mydownloader.db.DatabaseManager;
import geeksammao.bingyan.net.mydownloader.network.result.RequestResult;
import geeksammao.bingyan.net.mydownloader.network.task.DownloadTask;

/**
 * Created by Geeksammao on 10/16/15.
 */
public class MultiThreadManager {
    private DatabaseManager databaseManager;
    private final int CORE_NUM = Runtime.getRuntime().availableProcessors();
    private ExecutorService dbExecutorService = Executors.newFixedThreadPool(9);
    private ExecutorService taskExecutorService = Executors.newFixedThreadPool(CORE_NUM + 1);

    private int threadNum;
    private  String targetUrl;
    private boolean isExist;
    private long fileLength;
    private int downloadedLength;
    private long block;
    private File saveDir;
    private String fileName;
    private int retryNum;

    private Activity activity;

    private DownloadTask[] downloadTasks;
    private Map<Integer, Long> downloadedLengthMap = new HashMap<>();

    public MultiThreadManager(int threadNum, String targetUrl, File saveDir, Context context) {
        this.threadNum = threadNum;
        this.targetUrl = targetUrl;
        this.fileName = setFileName();
        this.saveDir = new File(saveDir, this.fileName);
        databaseManager = DatabaseManager.getInstance(context);

        this.activity = (Activity) context;
        downloadTasks = new DownloadTask[threadNum];
    }

    public void fetchDownloadFileLength(final OnDownloadCallback callback) {
        callback.onPreDownload();

        new Thread(new Runnable() {
            @Override
            public void run() {
                int length;

                try {
                    HttpUtil httpUtil = HttpUtil.getInstance();
                    RequestResult<Integer> requestResult = httpUtil.getContentLength(targetUrl);

                    if (requestResult.getStatus() == 200) {
                        length = requestResult.getData();

                        if (length <= 0) {
                            throw new NetworkErrorException("File size error");
                        }

                        setFileLength(length);

                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                callback.onDownloadStart();
                            }
                        });

                        downloadedLengthMap = databaseManager.getDownloadedLengthWithMap(targetUrl);

                        if (downloadedLengthMap.size() == threadNum) {
                            for (int i = 0; i < threadNum; i++) {
                                downloadedLength += downloadedLengthMap.get(i + 1);
                            }
                        }
                    } else {
                        retryNum++;
                        if (retryNum < 3) {
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    fetchDownloadFileLength(callback);
                                }
                            });
                        } else {
                            activity.runOnUiThread(
                                    new Runnable() {
                                        @Override
                                        public void run() {
                                            callback.onDownloadError();
                                        }
                                    }
                            );
                        }
                        throw new NetworkErrorException("Network error with error code " + requestResult.getStatus());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void download(final OnProgressUpdateCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                block = (fileLength % threadNum == 0) ? (fileLength / threadNum) : (fileLength / threadNum + 1);
                initRandomAccessFile();

                if (threadNum != downloadedLengthMap.size()) {
                    downloadedLengthMap.clear();
                    for (int i = 0; i < threadNum; i++) {
                        downloadedLengthMap.put(i + 1, 0l);
                    }
                    downloadedLength = 0;
                }
                for (int i = 0; i < threadNum; i++) {
                    if (downloadedLengthMap.get(i + 1) < block && downloadedLength < fileLength) {
                        startDownloadThread(i);
                    } else {
                        downloadTasks[i] = null;
                    }
                }

                databaseManager.delete(targetUrl);
                databaseManager.setData(targetUrl, downloadedLengthMap);

                boolean isFinished = false;
                while (!isFinished) {
                    try {
                        Thread.sleep(600);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    isFinished = true;
                    for (int i = 0; i < threadNum; i++) {
                        if (downloadTasks[i] != null &&
                                !downloadTasks[i].isFinished()) {
                            isFinished = false;
                            if (downloadTasks[i].getDownloadedLength() == -1) {
                                startDownloadThread(i);
                            }
                        }
                    }

                    // update the progress
                    if (callback != null) {
                        final int progress = (int) (100L * ((long) downloadedLength) / fileLength);
                        if (downloadedLength >= fileLength) {
                            isFinished = true;
                        }
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                callback.setProgress(progress);
                            }
                        });
                    }
                }

                dbExecutorService.shutdown();
                if (downloadedLength >= fileLength) {
                    databaseManager.delete(targetUrl);
                }

            }
        }).start();
    }

    private void initRandomAccessFile() {
        RandomAccessFile randomAccessFile;
        try {
            randomAccessFile = new RandomAccessFile(saveDir, "rwd");
            if (fileLength > 0) {
                randomAccessFile.setLength(fileLength);
            }
            randomAccessFile.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startDownloadThread(int i) {
        downloadTasks[i] = new DownloadTask(MultiThreadManager.this, targetUrl,
                saveDir, block, downloadedLengthMap.get(i + 1), i + 1, 0);
        downloadTasks[i].setPriority(Thread.MAX_PRIORITY);
        taskExecutorService.execute(downloadTasks[i]);
    }

    public void setDownloadedLength(int threadId, long downloadedLength) {
        downloadedLengthMap.put(threadId, downloadedLength);
    }

    public void updateDB() {
        dbExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                databaseManager.update(targetUrl, downloadedLengthMap);
            }
        });
    }

    public synchronized void appendDownloadedLength(int length) {
        downloadedLength += length;
    }

    public boolean isExist() {
        return isExist;
    }

    public void setIsExist(boolean isExist) {
        this.isExist = isExist;
    }

    public void setThreadNum(int threadNum) {
        this.threadNum = threadNum;
    }

    public long getFileLength() {
        return fileLength;
    }

    public void setFileLength(long fileLength) {
        this.fileLength = fileLength;
    }

    public int getDownloadedLength() {
        return downloadedLength;
    }

    public int getThreadNum() {
        return threadNum;
    }

    public synchronized ExecutorService getDbExecutorService() {
        return dbExecutorService;
    }

    public String getFileName() {
        return fileName;
    }

    // need to fix suffix
    private String setFileName() {
        String fileName = null;

        if ()
         fileName = targetUrl.substring(targetUrl.lastIndexOf("/") + 1);
        if (fileName.trim().equals("")) {
            fileName = String.valueOf(UUID.randomUUID());
        }
        fileName += ".apk";
        return fileName;
    }

    public void shutdownNow() {
        dbExecutorService.shutdownNow();
        taskExecutorService.shutdownNow();
    }
}

