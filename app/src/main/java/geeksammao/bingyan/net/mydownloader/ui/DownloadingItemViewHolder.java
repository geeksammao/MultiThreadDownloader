package geeksammao.bingyan.net.mydownloader.ui;

import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import geeksammao.bingyan.net.mydownloader.R;

/**
 * Created by Geeksammao on 11/28/15.
 */
public class DownloadingItemViewHolder extends RecyclerView.ViewHolder {
    private ImageView fileImageView;
    private ImageView toggleButtonImageView;
    private ProgressBar progressBar;
    private TextView fileSizeTv;
    private TextView downloadSpeedTv;
    private TextView fileNameTv;
    private TextView downloadProgressTv;
    private boolean isPause;

    public DownloadingItemViewHolder(View itemView) {
        super(itemView);

        isPause = false;

        fileImageView = (ImageView) itemView.findViewById(R.id.down_item_imv);
        toggleButtonImageView = (ImageView) itemView.findViewById(R.id.down_item_toggle_imv);
        progressBar = (ProgressBar) itemView.findViewById(R.id.down_item_prgb);
        fileNameTv = (TextView)itemView.findViewById(R.id.down_item_filename_tv);
        downloadSpeedTv = (TextView)itemView.findViewById(R.id.down_speed_tv);
        fileSizeTv = (TextView)itemView.findViewById(R.id.down_item_filesize_tv);
        downloadProgressTv = (TextView)itemView.findViewById(R.id.down_percent_tv);

        toggleButtonImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPause) {
                    // start
                    toggleButtonImageView.setImageResource(R.drawable.play);
                } else {
                    // pause
                    toggleButtonImageView.setImageResource(R.drawable.pause);
                }
            }
        });
    }

    public void setFileImageView(Bitmap bitmap) {
        fileImageView.setImageBitmap(bitmap);
    }

    public void setFileImageView(String name) {
        String suffix = name.substring(name.lastIndexOf(".") + 1, name.length());
        switch (suffix) {
            case "apk":
                fileImageView.setImageResource(R.drawable.ic_apk);
                break;
            case "jpg":
                fileImageView.setImageResource(R.drawable.ic_image);
                break;
            case "png":
                fileImageView.setImageResource(R.drawable.ic_image);
                break;
            case "gif":
                fileImageView.setImageResource(R.drawable.ic_image);
                break;
            case "webp":
                fileImageView.setImageResource(R.drawable.ic_image);
                break;
            case "txt":
                fileImageView.setImageResource(R.drawable.ic_document);
                break;
            case "mp4":
                fileImageView.setImageResource(R.drawable.ic_movie);
                break;
            case "avi":
                fileImageView.setImageResource(R.drawable.ic_movie);
                break;
            case "rmvb":
                fileImageView.setImageResource(R.drawable.ic_movie);
                break;
            case "mkv":
                fileImageView.setImageResource(R.drawable.ic_movie);
                break;
            default:
                fileImageView.setImageResource(R.drawable.ic_file);
                break;
        }
    }

    public void setProgress(int progress) {
        progressBar.setProgress(progress);
    }

    public void setFileName(String fileName){
        fileNameTv.setText(fileName);
    }

    public void setFileSize(double fileSize) {
        String fileSizeText = String.format("%.2f",fileSize/1024/1024) + "MB";
        fileSizeTv.setText(fileSizeText);
    }

    public void setDownloadProgressTv(int progress){
        downloadProgressTv.setText(Integer.toString(progress) + "%");
    }

    public void setDownloadSpeed(long downloadSpeed) {
        String downloadSpeedText = Long.toString(downloadSpeed) + "kb/s";
        downloadSpeedTv.setText(downloadSpeedText);
    }

}
