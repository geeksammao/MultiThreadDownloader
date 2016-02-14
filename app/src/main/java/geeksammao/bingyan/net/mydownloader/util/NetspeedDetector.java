package geeksammao.bingyan.net.mydownloader.util;

import android.content.Context;
import android.net.TrafficStats;

/**
 * Created by Geeksammao on 12/3/15.
 * <p/>
 * A helper class to calculate the network speed
 */
public class NetspeedDetector {
    private Context context;
    private long lastTotalRxBytes = 0;
    private long lastTime = 0;

    public NetspeedDetector(Context context) {
        this.context = context;
    }

    public long getNetworkSpeed() {
        long nowTotalRxBytes = getTotalRxBytes();
        long nowTime = System.currentTimeMillis();
        long speed = (nowTotalRxBytes - lastTotalRxBytes) * 1000 / (nowTime - lastTime);

        lastTotalRxBytes = nowTotalRxBytes;
        lastTime = nowTime;

        return speed;
    }

    private long getTotalRxBytes() {
        return TrafficStats.getUidRxBytes(context.getApplicationInfo().uid) == TrafficStats.UNSUPPORTED ? 0 :
                (TrafficStats.getTotalRxBytes() / 1024);
    }
}
