package com.tcpclient;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.SensorManager;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {
private static final String DATABASE_NAME="shutdown";
public static final String COMPUTERNAME="name";
public static final String LAST_IP="last_ip";
private static final String tag = null;

public DatabaseHelper(Context context) {
	super(context, DATABASE_NAME, null, 1);
}

@Override
public void onCreate(SQLiteDatabase db) {
	db.execSQL("CREATE TABLE computers (_id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, last_ip TEXT);");
	
	ContentValues cv=new ContentValues();
	
	// add some mock data for the moment
	cv.put(COMPUTERNAME, "pamina");
	cv.put(LAST_IP, "192.168.0.100");
	db.insert("computers", COMPUTERNAME, cv);
}

@Override
public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	android.util.Log.w("Constants", "Database is going to be updated you will lose all data.");
	db.execSQL("DROP TABLE IF EXISTS computers");
	onCreate(db);
}
}