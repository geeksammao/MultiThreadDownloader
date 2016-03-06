package geeksammao.bingyan.net.mydownloader;

import android.app.Application;

/**
 * Created by Geeksammao on 3/5/16.
 */
public class MyApplication extends Application {
    private static MyApplication instance ;

    public static MyApplication getInstance(){
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }
}
