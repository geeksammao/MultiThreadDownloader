package geeksammao.bingyan.net.mydownloader.network;

/**
 * Created by Geeksammao on 11/26/15.
 */
public interface OnDownloadCallback {
    void onPreDownload();

    void onDownloadStart();

    void onDownloadError();
}
