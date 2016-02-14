package geeksammao.bingyan.net.mydownloader.util;

import android.util.Log;

/**
 * Created by Geeksammao on 10/23/15.
 */
public class Logger {
    public static void logString(Object object,String string) {
        Log.e(object.getClass().getName(), string);
    }

    public static void logInt(int intger) {
        Log.e("sammao", Integer.toString(intger));
    }

    public static void logLong(long num) {
        Log.e("sammao", Long.toString(num));
    }

    public static void logBoolean(boolean value) {
        Log.e("sammao", Boolean.toString(value));
    }

    public static void logDouble(double value) {
        Log.e("sammao", Double.toString(value));
    }

    public static void logFloat(float value) {
        Log.e("sammao", Float.toString(value));
    }
}
