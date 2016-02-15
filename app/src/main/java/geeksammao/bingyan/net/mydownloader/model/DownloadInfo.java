package geeksammao.bingyan.net.mydownloader.model;

import android.graphics.Bitmap;

/**
 * Created by Geeksammao on 12/2/15.
 */
public class DownloadInfo {
    public static final int DOWNLOAD_FINISH = 2;
    public static final int DOWNLOAD_FAIL = 1;
    public static final int DOWNLOAD_ONGOING = 0;

    public int progress;
    public long downloadSpeed;
    public double fileSize;
    public String fileName;
    public Bitmap fileImageBitmap;
    public int downloadState = DOWNLOAD_ONGOING;
}
