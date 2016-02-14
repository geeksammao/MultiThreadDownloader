package geeksammao.bingyan.net.mydownloader.ui;

import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import geeksammao.bingyan.net.mydownloader.R;
import geeksammao.bingyan.net.mydownloader.model.DownloadInfo;

/**
 * Created by Geeksammao on 11/28/15.
 */
public class DownloadingItemAdapter extends RecyclerView.Adapter<DownloadItemViewHolder> {
    private MainActivity activity;
    private List<DownloadInfo> downloadInfoList;

    public DownloadingItemAdapter(MainActivity activity, List<DownloadInfo> downloadInfoList) {
        this.activity = activity;
        this.downloadInfoList = downloadInfoList;
    }

    @Override
    public DownloadItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rootView = LayoutInflater.from(activity).inflate(R.layout.downloading_item_layout, parent, false);

        return new DownloadItemViewHolder(rootView, activity);
    }

    @Override
    public void onBindViewHolder(DownloadItemViewHolder holder, int position) {
        int progress = downloadInfoList.get(position).progress;
        long downloadSpeed = downloadInfoList.get(position).downloadSpeed;
        double fileSize = downloadInfoList.get(position).fileSize;
        String fileName = downloadInfoList.get(position).fileName;
        Bitmap fileImage = downloadInfoList.get(position).fileImageBitmap;

        holder.setProgress(progress);
        holder.setFileSize(fileSize);
        holder.setFileName(fileName);
        holder.setDownloadSpeed(downloadSpeed);
        holder.setDownloadProgressTv(progress);
        if (fileImage != null){
            holder.setFileImageView(fileImage);
        } else {
            // set the image according to the file suffix
            holder.setFileImageView(fileName);
        }
    }

    @Override
    public int getItemCount() {
        return downloadInfoList.size();
    }

    public void setDownloadInfoList(List<DownloadInfo> list){
        this.downloadInfoList = list;
    }
}
