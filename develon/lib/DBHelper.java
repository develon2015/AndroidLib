package develon.lib;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.HashMap;

/**
 * 用途:
 * 管理 SQLite 数据库
 * 确保 getWritableDatabase()... 不会重复打开数据库
 */
public class DBHelper extends SQLiteOpenHelper {
    static HashMap<String, DBHelper> insts = new HashMap<>();

    private DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public static DBHelper getDBHelper(Context context, String dbname, int version) {
        DBHelper inst = insts.get(dbname);
        if (inst == null) {
            inst = new DBHelper(context, dbname, null, version);
            insts.put(dbname, inst);
        }
        return inst;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        App.log("onCreate");
        switch (getDatabaseName()) {
            case "test":
                try {
                    db.execSQL("CREATE TABLE log(id INT, log VARCHER(255));");
                } catch (Exception e) {
                    e.printStackTrace();
                    App.log(e.getMessage());
                }
                break;
            default:
        }
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        App.log("onOpen");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        App.log("upgrade");
    }

    @Override
    public synchronized void close() {
        super.close();
    }
}
