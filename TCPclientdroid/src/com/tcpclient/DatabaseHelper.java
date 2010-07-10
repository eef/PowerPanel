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
private static final String DATABASE_NAME="db";
public static final String COMPUTERNAME="title";
public static final String VALUE="value";
private static final String tag = null;
public boolean authenticated = false;

public DatabaseHelper(Context context) {
	super(context, DATABASE_NAME, null, 1);
}

@Override
public void onCreate(SQLiteDatabase db) {
	db.execSQL("CREATE TABLE constants (_id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT, value REAL);");
	
	ContentValues cv=new ContentValues();
	
	// add some mock data for the moment
	Log.d(tag, "==========12============");
	cv.put(COMPUTERNAME, "Amber");
	Log.d(tag, "==========22============");
	cv.put(VALUE, 123);
	Log.d(tag, "==========32============");
	db.insert("constants", COMPUTERNAME, cv);
	Log.d(tag, "==========42============");
	cv.put(COMPUTERNAME, "Arthur");
	Log.d(tag, "==========52============");
	cv.put(VALUE, 132);
	Log.d(tag, "==========62============");
	db.insert("constants", COMPUTERNAME, cv);
	Log.d(tag, "==========72============");
}

@Override
public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	android.util.Log.w("Constants", "Database is going to be updated you will lose all data.");
	db.execSQL("DROP TABLE IF EXISTS constants");
	onCreate(db);
}


}