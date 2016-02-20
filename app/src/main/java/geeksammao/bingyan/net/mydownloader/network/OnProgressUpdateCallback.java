package geeksammao.bingyan.net.mydownloader.network;

public interface OnProgressUpdateCallback {
    void setProgress(int progress);

    void onFail(String url);
}
