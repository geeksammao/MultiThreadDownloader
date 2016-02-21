package geeksammao.bingyan.net.mydownloader.network;

import android.accounts.NetworkErrorException;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
    private String targetUrl;
    private boolean isExist;
    private boolean isMultiThreadEnabled;
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
        this.saveDir = saveDir;
        databaseManager = DatabaseManager.getInstance(context);

        this.activity = (Activity) context;
        downloadTasks = new DownloadTask[threadNum];
    }

    public void setTargetUrl(String targetUrl) {
        this.targetUrl = targetUrl;
    }

    public void initDownload(final OnDownloadCallback callback) {
        callback.onPreDownload();

        new Thread(new Runnable() {
            @Override
            public void run() {
                int length;

                try {
                    HttpUtil httpUtil = HttpUtil.getInstance();
                    RequestResult<Bundle> requestResult = httpUtil.getHeadFieldForDownload(targetUrl);

                    if (requestResult.getStatus() == 200) {
                        Bundle bundle = requestResult.getMultiData();
                        boolean rangeAccept = ("bytes").equals(bundle.getString("accept_range"));
                        length = bundle.getInt("length");
                        fileName = bundle.getString("name");
                        String etag = bundle.getString("etag");
                        saveDir = new File(saveDir, fileName);

                        if (length <= 0) {
                            activity.runOnUiThread(
                                    new Runnable() {
                                        @Override
                                        public void run() {
                                            callback.onDownloadError();
                                        }
                                    }
                            );
                            throw new NetworkErrorException("File size error");
                        }
                        setFileLength(length);

                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                callback.onDownloadStart();
                            }
                        });

                        if (rangeAccept) {
                            isMultiThreadEnabled = false;
//                            isMultiThreadEnabled = true;
//                            initForMultiThreadDownload(length, callback);
                        } else {
                            isMultiThreadEnabled = false;
                        }
                    } else {
                        activity.runOnUiThread(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        callback.onDownloadError();
                                    }
                                }
                        );
                        throw new NetworkErrorException("Network error with error code " + requestResult.getStatus());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void downloadWithSingleThread(long totalFileLength,final OnProgressUpdateCallback callback) {
        int totalDownloadLength = 0;
        HttpUtil httpUtil = HttpUtil.getInstance();
        RequestResult<InputStream> result = httpUtil.getInputStream(targetUrl);

        if (result.getStatus() == 200) {
            InputStream inputStream = result.getData();

            byte[] buffer = new byte[50 * 1024];
            int length;
            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream, buffer.length);
            FileOutputStream fileOutputStream = null;
            try {
                fileOutputStream = new FileOutputStream(saveDir);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            try {
                while ((length = bufferedInputStream.read(buffer, 0, buffer.length)) != -1) {
                    totalDownloadLength += length;
                    final int progress = totalDownloadLength/(int)totalFileLength;
                    Thread.sleep(500);
                    fileOutputStream.write(buffer);
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            callback.setProgress(progress);
                        }
                    });
                }
                fileOutputStream.flush();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    fileOutputStream.close();
                    bufferedInputStream.close();
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            try {
                throw new NetworkErrorException("Network error with error code " + result.getStatus());
            } catch (NetworkErrorException e) {
                e.printStackTrace();
            }
        }
    }

    private void initForMultiThreadDownload(int length, final OnDownloadCallback callback) throws NetworkErrorException {
        downloadedLengthMap = databaseManager.getDownloadedLengthWithMap(targetUrl);

        if (downloadedLengthMap.size() == threadNum) {
            for (int i = 0; i < threadNum; i++) {
                downloadedLength += downloadedLengthMap.get(i + 1);
            }
        }

        initRandomAccessFile();
    }

    public void download(final OnProgressUpdateCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (isMultiThreadEnabled) {
                    block = (fileLength % threadNum == 0) ? (fileLength / threadNum) : (fileLength / threadNum + 1);

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
                            Thread.sleep(500);
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

                                if (downloadTasks[i].isTaskFailed() && callback != null) {
                                    Log.e("sam", "task fail");
                                    activity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            callback.onFail(targetUrl);
                                        }
                                    });
                                    return;
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
//                                    callback.setProgress(progress);
                                }
                            });
                        }
                    }

                    if (downloadedLength >= fileLength) {
                        databaseManager.delete(targetUrl);
                    }

                } else {
                    downloadWithSingleThread(fileLength,callback);
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
    private String fetchFileName() {
        String fileName;
        HttpUtil httpUtil = HttpUtil.getInstance();
        if ((fileName = httpUtil.getFileName(targetUrl)) != null) {
            return fileName;
        }

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

