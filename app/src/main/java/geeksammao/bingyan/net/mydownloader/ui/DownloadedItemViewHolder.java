package geeksammao.bingyan.net.mydownloader.ui;

import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import geeksammao.bingyan.net.mydownloader.R;

/**
 * Created by Geeksammao on 2/15/16.
 */
public class DownloadedItemViewHolder extends RecyclerView.ViewHolder {
    private ImageView downedItemImv;
    private TextView downedUrlTv;
    private TextView downedDateTv;
    private TextView downedSizeTv;
    private TextView downedFileNameTv;

    public DownloadedItemViewHolder(View rootView) {
        super(rootView);

        downedItemImv = (ImageView) rootView.findViewById(R.id.downed_item_imv);
        downedUrlTv = (TextView) rootView.findViewById(R.id.downed_url_tv);
        downedDateTv = (TextView) rootView.findViewById(R.id.downed_date_tv);
        downedSizeTv = (TextView) rootView.findViewById(R.id.downed_size_tv);
        downedFileNameTv = (TextView) rootView.findViewById(R.id.downed_filename_tv);
    }

    public void setDownedImage(Bitmap bitmap) {
        downedItemImv.setImageBitmap(bitmap);
    }

    public void setDownedUrlText(String text) {
        downedUrlTv.setText(text);
    }

    public void setDownedDateText(String text) {
        downedDateTv.setText(text);
    }

    public void setDownedSizeText(double size) {
        downedSizeTv.setText(String.valueOf(size));
    }

    public void setDownedFileNameTv(String fileName) {
        downedSizeTv.setText(fileName);
    }
}
