package com.wellbaked.powerpanel;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class DataHelper {

   private static final String DATABASE_NAME = "powerpanel";
   private static final int DATABASE_VERSION = 1;
   private static final String TABLE_NAME = "computers";

   private Context context;
   private SQLiteDatabase db;

   private SQLiteStatement insertStmt;
   private static final String INSERT = "insert into " 
      + TABLE_NAME + "(name, pkey, last_ip, mac) values (?, ?, ?, ?)";

   public DataHelper(Context context) {
      this.context = context;
      OpenHelper openHelper = new OpenHelper(this.context);
      this.db = openHelper.getWritableDatabase();
      this.insertStmt = this.db.compileStatement(INSERT);
   }

   public long insert(String name, String pkey, String ip, String mac) {
      this.insertStmt.bindString(1, name);
      this.insertStmt.bindString(2, pkey);
      this.insertStmt.bindString(3, ip);
      this.insertStmt.bindString(4, mac);
      return this.insertStmt.executeInsert();
   }
   
   public String isSaved(String pkey) {
	   Cursor cursor = this.db.query(TABLE_NAME, new String[] {"name", "mac", "last_ip"}, "pkey = " + "'"+ pkey +"'", null, null, null, null);
	   if(cursor.moveToFirst()) {
		   String name = cursor.getString(0);
		   cursor.close();
		   return name;
	   } else {
		   cursor.close();
		   return "";
	   }
   }

   public void deleteAll() {
      this.db.delete(TABLE_NAME, null, null);
   }
   

   public List<String> selectAll() {
      List<String> list = new ArrayList<String>();
      Log.e("Database Itr", "In method");
      Cursor cursor = this.db.query(TABLE_NAME, new String[] { "name, pkey, last_ip, mac" }, 
        null, null, null, null, "name desc");
      if (cursor.moveToFirst()) {
         do {
            list.add(cursor.getString(2));
            Log.e("Database Itr", cursor.getString(0) + cursor.getString(1) + cursor.getString(2) + cursor.getString(3));
         } while (cursor.moveToNext());
      }
      if (cursor != null && !cursor.isClosed()) {
         cursor.close();
      }
      return list;
   }

   private static class OpenHelper extends SQLiteOpenHelper {

      OpenHelper(Context context) {
         super(context, DATABASE_NAME, null, DATABASE_VERSION);
      }

      @Override
      public void onCreate(SQLiteDatabase db) {
         db.execSQL("CREATE TABLE " + TABLE_NAME + "(id INTEGER PRIMARY KEY, name TEXT, pkey TEXT, last_ip TEXT, mac TEXT)");
      }

      @Override
      public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
         Log.w("Example", "Upgrading database, this will drop tables and recreate.");
         db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
         onCreate(db);
      }
   }
}