package geeksammao.bingyan.net.mydownloader.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Geeksammao on 11/23/15.
 */
public class DownDatabase extends SQLiteOpenHelper {
    private static final String NAME = "fileloader.db";
    private static final int VERSION = 1;

    public DownDatabase(Context context) {
        super(context, NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table if not exists fileloadertable (" +
                "_id integer primary key AUTOINCREMENT," +
                "threadId integer not null," +
                "downpath text not null," +
                "downedlen long not null," +
                "etag text," +
                "last_modified text)"
        )
        ;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop talbe if exists fileloadertable");
        onCreate(db);
    }
}