package com.wcy.client12306.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.HashMap;

public class DBHelper extends SQLiteOpenHelper {

    // 数据库文件名
    public static final String DB_NAME = "login_info.db";
    // 数据库表名
    public static final String TABLE_NAME = "user";
    // 数据库版本号
    public static final int DB_VERSION = 1;

    public static final String NAME = "name";
    public static final String PASSWORD = "password";

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    public void insert(String name, String password)
    {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("INSERT INTO user(id,name,password) values(?,?,?)",
                new String[]{"1",name,password});
    }

    public HashMap<String, String> find(Integer id)
    {
        HashMap<String, String> result = new HashMap<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor =  db.rawQuery("SELECT * FROM user WHERE id = ?",
                new String[]{id.toString()});
        //存在数据才返回true
        if(cursor.moveToFirst())
        {
            String name = cursor.getString(cursor.getColumnIndex("name"));
            String password = cursor.getString(cursor.getColumnIndex("password"));
            result.put("name", name);
            result.put("password", password);
            return result;
        }
        cursor.close();
        return null;
    }

    public void update(String name, String password)
    {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("UPDATE user SET name = ?,password = ? WHERE id = ?",
                new String[]{name,password,"1"});
    }

    public void delete(Integer id)
    {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM user WHERE id = ?",
                new String[]{String.valueOf(id)});
    }

    // 当数据库文件创建时，执行初始化操作，并且只执行一次
    @Override
    public void onCreate(SQLiteDatabase db) {
        // 建表
        String sql = "create table " +
                TABLE_NAME +
                "(id integer primary key autoincrement, " +
                NAME + " varchar, " +
                PASSWORD + " varchar"
                + ")";

        db.execSQL(sql);
    }

    // 当数据库版本更新执行该方法
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
