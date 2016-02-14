package geeksammao.bingyan.net.mydownloader.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

/**
 * Created by Geeksammao on 10/16/15.
 */
public class NetworkHelper {
    public static final int[] _2G_TYPES = {TelephonyManager.NETWORK_TYPE_1xRTT, TelephonyManager.NETWORK_TYPE_EDGE,
            TelephonyManager.NETWORK_TYPE_GPRS, TelephonyManager.NETWORK_TYPE_CDMA, TelephonyManager.NETWORK_TYPE_IDEN
    };

//    public static final int[] _3G_TYPES = {TelephonyManager.NETWORK_TYPE_UMTS, TelephonyManager.NETWORK_TYPE_EVDO_0,
//            TelephonyManager.NETWORK_TYPE_HSDPA, TelephonyManager.NETWORK_TYPE_EVDO_B, TelephonyManager.NETWORK_TYPE_EVDO_A,
//            TelephonyManager.NETWORK_TYPE_HSPA, TelephonyManager.NETWORK_TYPE_HSUPA, TelephonyManager.NETWORK_TYPE_HSPAP,
//            TelephonyManager.NETWORK_TYPE_EHRPD
//    };

    public static final int[] _4G_TYPES = {TelephonyManager.NETWORK_TYPE_LTE
    };

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isAvailable();
    }

    public static boolean isWifiAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        return networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI;
    }

    public static boolean is2GAvailable(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager)
                context.getSystemService(Context.TELEPHONY_SERVICE);
        int networkType = telephonyManager.getNetworkType();

        for (int _2GType : _2G_TYPES) {
            if (networkType == _2GType) {
                return true;
            }
        }

        return false;
    }

    public static boolean is4GAvailable(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager)
                context.getSystemService(Context.TELEPHONY_SERVICE);
        int networkType = telephonyManager.getNetworkType();

        for (int _4GType : _4G_TYPES) {
            if (networkType == _4GType) {
                return true;
            }
        }

        return false;
    }
}
