package jp.cleartouch.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class WaveSQLiteHelper extends SQLiteOpenHelper {
	private static final String TAG = "WaveSQLiteHelper";
	private static final String DATABASE_NAME = "waveDB.db";
	private static final int DATABASE_VERSION = 1;
	
	public static final String TABLE_POST = "posts";
	public static final String TABLE_POST_COLUMN_ID = "_id";
	public static final String TABLE_POST_COLUMN_Y = "y";
	public static final String TABLE_POST_COLUMN_DATA = "data";
	public static final String TABLE_POST_COLUMN_THUMB = "thumb";
	public static final String TABLE_POST_COLUMN_USERNAME = "username";
	public static final String TABLE_POST_COLUMN_DISPLAY_AT = "display_at";

	public static final String TABLE_GET_POST_REQUEST = "getpostrequests";
	public static final String TABLE_GET_POST_REQUEST_COLUMN_MIN = "min"; // data range in min. eg) 1 means 0-59
	public static final String TABLE_GET_POST_REQUEST_COLUMN_STATUS = "status"; // REST request status
	
	public static final String TABLE_CREATE_POST_REQUEST = "postpostrequests";
	public static final String TABLE_CREATE_POST_REQUEST_COLUMN_UUID = "uuid";
	public static final String TABLE_CREATE_POST_REQUEST_COLUMN_STATUS = "status"; // REST request status
	public static final String TABLE_CREATE_POST_REQUEST_COLUMN_RESULT = "result"; // HTTP result code
	

	
	public WaveSQLiteHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		
		// create table
		createTablePost(db);
		createTableGetPostRequest(db);
		createTableCreatePostRequest(db);
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_POST);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_GET_POST_REQUEST);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_CREATE_POST_REQUEST);
		onCreate(db);
	}
	
	/////
	//
	// TABLE_POST
	//
	/////
	
	public void createTablePost(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE IF NOT EXISTS " +
				TABLE_POST + "(" + TABLE_POST_COLUMN_ID + " integer primary key autoincrement, " +
				TABLE_POST_COLUMN_Y + " integer not null, " +
				TABLE_POST_COLUMN_DATA + " text not null, " +
				TABLE_POST_COLUMN_THUMB + " text not null, " +
				TABLE_POST_COLUMN_USERNAME + " text not null, " +
				TABLE_POST_COLUMN_DISPLAY_AT + " integer not null " +
				")");
	}
	
	public void savePost(int y, int displayAt, String data, String thumb, String userName, String postDate, String postTime){
		Log.d (TAG, "savePost()");
		
		SQLiteDatabase db = getWritableDatabase();
		
		ContentValues values = new ContentValues();
		values.put(WaveSQLiteHelper.TABLE_POST_COLUMN_Y, y);
		values.put(WaveSQLiteHelper.TABLE_POST_COLUMN_DATA, data);
		values.put(WaveSQLiteHelper.TABLE_POST_COLUMN_THUMB, thumb);
		values.put(WaveSQLiteHelper.TABLE_POST_COLUMN_USERNAME, userName);
	    values.put(WaveSQLiteHelper.TABLE_POST_COLUMN_DISPLAY_AT, displayAt);
	    
	    db.insert(WaveSQLiteHelper.TABLE_POST, null, values);
	    db.close();
	}

	
	/////
	//
	// TABLE_GET_POST_REQUEST
	//
	/////
	
	public void createTableGetPostRequest(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE IF NOT EXISTS " +
				TABLE_GET_POST_REQUEST + "(" + 
				TABLE_GET_POST_REQUEST_COLUMN_MIN + " integer primary key, " +
				TABLE_GET_POST_REQUEST_COLUMN_STATUS + " integer not null " +
				")");
	}
	
	/*
	 * return null if request doesn't exist.
	 */
	public Integer getGetPostRequestStatus(int min){
		SQLiteDatabase db = getReadableDatabase();
		
		// retrieve data from SQLite
    	Cursor cursor = db.query(WaveSQLiteHelper.TABLE_GET_POST_REQUEST, 
    			new String[] { WaveSQLiteHelper.TABLE_GET_POST_REQUEST_COLUMN_STATUS },
				WaveSQLiteHelper.TABLE_GET_POST_REQUEST_COLUMN_MIN + "=?",
	            new String[] { String.valueOf(min) }, null, null, null, null);
	   
    	if (cursor != null){
	        cursor.moveToFirst();
	        if(cursor.getCount() > 0){
	        	int status = Integer.parseInt(cursor.getString(0));
	        	cursor.close();
	        	db.close();
	        	return status;
	        }
	        cursor.close();
    	}
    	
    	db.close();
    	return null;
	}
	
	public void saveGetPostRequest(int min, int status){
		SQLiteDatabase db = getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(WaveSQLiteHelper.TABLE_GET_POST_REQUEST_COLUMN_MIN, min);
		values.put(WaveSQLiteHelper.TABLE_GET_POST_REQUEST_COLUMN_STATUS, status);
	    db.insert(WaveSQLiteHelper.TABLE_GET_POST_REQUEST, null, values);
	    db.close();
	}
	
	public void updateGetPostRequest(int min, int status) {
	    SQLiteDatabase db = getWritableDatabase();
	 
	    ContentValues values = new ContentValues();
	    values.put(WaveSQLiteHelper.TABLE_GET_POST_REQUEST_COLUMN_MIN, min);
		values.put(WaveSQLiteHelper.TABLE_GET_POST_REQUEST_COLUMN_STATUS, status);
	    
	    // updating row
	    db.update(WaveSQLiteHelper.TABLE_GET_POST_REQUEST, values, WaveSQLiteHelper.TABLE_GET_POST_REQUEST_COLUMN_MIN + " = ?",
	            new String[] { String.valueOf(min) });
		db.close();
	}	
	
	/////
	//
	// TABLE_CREATE_POST_REQUEST
	//
	/////

	public void createTableCreatePostRequest(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE IF NOT EXISTS " +
				TABLE_CREATE_POST_REQUEST + "(" + 
				TABLE_CREATE_POST_REQUEST_COLUMN_UUID + " text primary key, " +
				TABLE_CREATE_POST_REQUEST_COLUMN_STATUS + " integer not null, " +
				TABLE_CREATE_POST_REQUEST_COLUMN_RESULT + " integer not null " +
				")");
	}

	/*
	 * return null if request doesn't exist.
	 */
	public Integer getCreatePostRequestStatus(String uuid){
		SQLiteDatabase db = getReadableDatabase();
		
		// retrieve data from SQLite
    	Cursor cursor = db.query(WaveSQLiteHelper.TABLE_CREATE_POST_REQUEST, 
    			new String[] { WaveSQLiteHelper.TABLE_CREATE_POST_REQUEST_COLUMN_STATUS,
    							WaveSQLiteHelper.TABLE_CREATE_POST_REQUEST_COLUMN_RESULT },
				WaveSQLiteHelper.TABLE_CREATE_POST_REQUEST_COLUMN_UUID + "=?",
	            new String[] { uuid }, null, null, null, null);
	   
    	if (cursor != null){
	        cursor.moveToFirst();
	        if(cursor.getCount() > 0){
	        	int status = Integer.parseInt(cursor.getString(0));
	        	cursor.close();
	        	db.close();
	        	return status;
	        }
	        cursor.close();
    	}
    	
    	db.close();
    	return null;
	}
	
	public void saveCreatePostRequest(String uuid, int status, int result){
		SQLiteDatabase db = getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(WaveSQLiteHelper.TABLE_CREATE_POST_REQUEST_COLUMN_UUID, uuid);
		values.put(WaveSQLiteHelper.TABLE_CREATE_POST_REQUEST_COLUMN_STATUS, status);
		values.put(WaveSQLiteHelper.TABLE_CREATE_POST_REQUEST_COLUMN_RESULT, result);
	    db.insert(WaveSQLiteHelper.TABLE_CREATE_POST_REQUEST, null, values);
	    db.close();
	}
	
	public void updateCreatePostRequest(String uuid, int status, int result) {
	    SQLiteDatabase db = getWritableDatabase();
	    ContentValues values = new ContentValues();
		values.put(WaveSQLiteHelper.TABLE_CREATE_POST_REQUEST_COLUMN_STATUS, status);
		values.put(WaveSQLiteHelper.TABLE_CREATE_POST_REQUEST_COLUMN_RESULT, result);
	    db.update(WaveSQLiteHelper.TABLE_CREATE_POST_REQUEST, values, WaveSQLiteHelper.TABLE_CREATE_POST_REQUEST_COLUMN_UUID + " = ?",
	            new String[] { uuid });
		db.close();
	}	
	
}
