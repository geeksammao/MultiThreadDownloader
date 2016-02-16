package geeksammao.bingyan.net.mydownloader.ui;

import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import geeksammao.bingyan.net.mydownloader.R;
import geeksammao.bingyan.net.mydownloader.model.DownloadInfo;

/**
 * Created by Geeksammao on 11/28/15.
 */
public class DownloadingItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private MainActivity activity;
    private List<DownloadInfo> downloadInfoList;

    public DownloadingItemAdapter(MainActivity activity, List<DownloadInfo> downloadInfoList) {
        this.activity = activity;
        this.downloadInfoList = downloadInfoList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;

        switch (viewType) {
            case DownloadInfo.DOWNLOAD_ONGOING:
                View downloadingRootView = LayoutInflater.from(activity).inflate(R.layout.downloading_item_layout, parent, false);
                viewHolder = new DownloadingItemViewHolder(downloadingRootView);
                break;
            case DownloadInfo.DOWNLOAD_FINISH:
                View downloadedRootView = LayoutInflater.from(activity).inflate(R.layout.downloaded_item_layout, parent, false);
                viewHolder = new DownloadedItemViewHolder(downloadedRootView);
                break;
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int progress = downloadInfoList.get(position).progress;
        long downloadSpeed = downloadInfoList.get(position).downloadSpeed;
        double fileSize = downloadInfoList.get(position).fileSize;
        String fileName = downloadInfoList.get(position).fileName;
        Bitmap fileImage = downloadInfoList.get(position).fileImageBitmap;
        String fileUrl = downloadInfoList.get(position).url;

        switch (holder.getItemViewType()) {
            case DownloadInfo.DOWNLOAD_ONGOING:
                DownloadingItemViewHolder downloadingItemViewHolder = (DownloadingItemViewHolder) holder;

                downloadingItemViewHolder.setProgress(progress);
                downloadingItemViewHolder.setFileSize(fileSize);
                downloadingItemViewHolder.setFileName(fileName);
                downloadingItemViewHolder.setDownloadSpeed(downloadSpeed);
                downloadingItemViewHolder.setDownloadProgressTv(progress);
                if (fileImage != null) {
                    downloadingItemViewHolder.setFileImageView(fileImage);
                } else {
                    // set the image according to the file suffix
                    downloadingItemViewHolder.setFileImageView(fileName);
                }
                break;

            case DownloadInfo.DOWNLOAD_FINISH:
                DownloadedItemViewHolder downloadedItemViewHolder = (DownloadedItemViewHolder) holder;

                downloadedItemViewHolder.setDownedSizeText(fileSize);
                downloadedItemViewHolder.setDownedFileNameTv(fileName);
                downloadedItemViewHolder.setDownedUrlText(fileUrl);
                downloadedItemViewHolder.setDownedDateText(new SimpleDateFormat("MM DD", Locale.CHINA).
                        format(new Date(System.currentTimeMillis())));
                if (fileImage != null) {
                    downloadedItemViewHolder.setDownedImage(null);
                } else {
                    // set the image according to the file suffix
                    downloadedItemViewHolder.setDownedImage(null);
                }
                break;
        }
    }

    @Override
    public int getItemCount() {
        return downloadInfoList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return downloadInfoList.get(position).downloadState;
    }

    public void setDownloadInfoList(List<DownloadInfo> list) {
        this.downloadInfoList = list;
    }
}
