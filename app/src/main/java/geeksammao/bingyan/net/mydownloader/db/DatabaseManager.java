package geeksammao.bingyan.net.mydownloader.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by Geeksammao on 10/25/15.
 */
public class DatabaseManager {
    private DownDatabase database;
    private static DatabaseManager databaseManager = null;
    private Context context;

    private DatabaseManager(Context context) {
        this.context = context;
    }

    public static DatabaseManager getInstance(Context context) {
        if (databaseManager == null) {
            databaseManager = new DatabaseManager(context);
        }
        return databaseManager;
    }

    private SQLiteDatabase getDatabase() {
        return new DownDatabase(context).getReadableDatabase();
    }

    public synchronized HashMap<Integer, Long> getDownloadedLengthWithMap(String downloadPath) {
        SQLiteDatabase sqLiteDatabase = getDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("select * from fileloadertable where downpath=?",
                new String[]{downloadPath});
        HashMap<Integer, Long> hashMap = new HashMap<>();
        if (cursor != null) {
            int threadIdIndex = cursor.getColumnIndexOrThrow("threadId");
            int downedLenIndex = cursor.getColumnIndexOrThrow("downedlen");
            while (cursor.moveToNext()) {
                hashMap.put(cursor.getInt(threadIdIndex), cursor.getLong(downedLenIndex));
            }
            cursor.close();
        }
        sqLiteDatabase.close();
        return hashMap;
    }

    public synchronized void delete(String path) {
        SQLiteDatabase sqLiteDatabase = getDatabase();
        sqLiteDatabase.execSQL("delete from fileloadertable where downpath=?",
                new String[]{path});
        sqLiteDatabase.close();
    }

    public synchronized void setData(String path, Map<Integer, Long> map) {
        SQLiteDatabase sqLiteDatabase = getDatabase();
        sqLiteDatabase.beginTransaction();
        Set<Map.Entry<Integer, Long>> set = map.entrySet();
        try {
            for (Map.Entry<Integer, Long> entry : set) {
                ContentValues contentValues = new ContentValues();
                contentValues.put("threadId", entry.getKey());
                contentValues.put("downedlen", entry.getValue());
                contentValues.put("downpath", path);
                sqLiteDatabase.insert("fileloadertable", "_id", contentValues);
            }
            sqLiteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            sqLiteDatabase.endTransaction();
        }
        sqLiteDatabase.close();
    }

    public synchronized void setCacheControlInfo(String path,String etag,String lastModified){
        SQLiteDatabase sqLiteDatabase = getDatabase();
        sqLiteDatabase.execSQL("insert into fileloadertable ");
    }

    public synchronized void update(String path, int threadID, int downloadedLength) {
        SQLiteDatabase sqLiteDatabase = getDatabase();
        sqLiteDatabase.execSQL("update fileloadertable set downedlen=? where " +
                        "threadId=? and downpath=?",
                new String[]{Integer.toString(downloadedLength), Integer.toString(threadID), path});
        sqLiteDatabase.close();
    }

    public synchronized void update(String path, Map<Integer, Long> map) {
        SQLiteDatabase sqLiteDatabase = getDatabase();
        sqLiteDatabase.beginTransaction();
        try {
            Set<Map.Entry<Integer, Long>> set = map.entrySet();
            for (Map.Entry<Integer, Long> entry : set) {
                sqLiteDatabase.execSQL("update fileloadertable set downedlen=? where " +
                                "threadId=? and downpath=?",
                        new String[]{"" + entry.getValue(), "" + entry.getKey(), path});
            }
            sqLiteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            sqLiteDatabase.endTransaction();
        }
        sqLiteDatabase.close();
    }
}
