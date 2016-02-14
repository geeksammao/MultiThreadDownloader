package geeksammao.bingyan.net.mydownloader.network.result;

import android.os.Bundle;

/**
 * Created by Geeksammao on 11/13/15.
 */
public class RequestResult<T> {
    private int status;
    private T data;
    private Bundle multiData;

    public void setStatus(int status) {
        this.status = status;
    }

    public void setData(T data) {
        this.data = data;
    }

    public void setMultiData(Bundle multiData) {
        this.multiData = multiData;
    }

    public int getStatus() {
        return status;
    }

    public T getData() {
        return data;
    }

    public Bundle getMultiData() {
        return multiData;
    }
}
