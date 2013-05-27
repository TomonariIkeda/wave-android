package jp.cleartouch.sqlite;

import jp.cleartouch.libs.rest.RestClient;
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
	public static final String TABLE_POST_COLUMN_TYPE = "type";
	public static final String TABLE_POST_COLUMN_Y = "y";
	public static final String TABLE_POST_COLUMN_COMMENT = "comment";
	public static final String TABLE_POST_COLUMN_THUMB = "thumb";
	public static final String TABLE_POST_COLUMN_USERNAME = "username";
	public static final String TABLE_POST_COLUMN_DISPLAY_AT = "display_at";
	public static final String TABLE_POST_COLUMN_CREATED_AT = "created_at";
	
	public static final String TABLE_POST_COUNT = "postcounts";
	public static final String TABLE_POST_COUNT_COLUMN_ID = "_id";
	public static final String TABLE_POST_COUNT_COLUMN_COUNT = "count";
	public static final String TABLE_POST_COUNT_COLUMN_INDICATOR_INDEX = "indicator_index";
	
	public static final String TABLE_GET_POST_REQUEST = "getpostrequests";
	public static final String TABLE_GET_POST_REQUEST_COLUMN_MIN = "min"; // data range in min. eg) 1 means 0-59
	public static final String TABLE_GET_POST_REQUEST_COLUMN_STATUS = "status"; // REST request status
	
	public static final String TABLE_CREATE_POST_REQUEST = "createpostrequests";
	public static final String TABLE_CREATE_POST_REQUEST_COLUMN_UUID = "uuid";
	public static final String TABLE_CREATE_POST_REQUEST_COLUMN_STATUS = "status"; // REST request status
	public static final String TABLE_CREATE_POST_REQUEST_COLUMN_RESULT = "result"; // HTTP result code
	
	public static final String TABLE_GET_POST_COUNT_REQUEST = "getpostcountrequests";
	public static final String TABLE_GET_POST_COUNT_REQUEST_COLUMN_ID = "_id";
	public static final String TABLE_GET_POST_COUNT_REQUEST_COLUMN_STATUS = "status"; // REST request status
	public static final String TABLE_GET_POST_COUNT_REQUEST_COLUMN_RESULT = "result"; // HTTP result code
	
	
	public static final String TABLE_PUT_POST_COUNT_REQUEST = "putpostcountrequests";
	public static final String TABLE_PUT_POST_COUNT_REQUEST_COLUMN_ID = "_id";  			// auto-increment
	public static final String TABLE_PUT_POST_COUNT_REQUEST_COLUMN_INDICATOR_INDEX = "indicator_index";
	public static final String TABLE_PUT_POST_COUNT_REQUEST_COLUMN_STATUS = "status"; 		// REST request status
	public static final String TABLE_PUT_POST_COUNT_REQUEST_COLUMN_RESULT = "result"; 		// HTTP result code
	
	public WaveSQLiteHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		
		// create table
		createTablePost(db);
		createTablePostCount(db);
		createTableGetPostRequest(db);
		createTableCreatePostRequest(db);
		createTableGetPostCountRequest(db);
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
				TABLE_POST_COLUMN_TYPE + " integer not null, " +
				TABLE_POST_COLUMN_Y + " integer not null, " +
				TABLE_POST_COLUMN_COMMENT + " text not null, " +
				TABLE_POST_COLUMN_THUMB + " text not null, " +
				TABLE_POST_COLUMN_USERNAME + " text not null, " +
				TABLE_POST_COLUMN_DISPLAY_AT + " integer not null, " +
				TABLE_POST_COLUMN_CREATED_AT + " integer not null " +
				")");
	}
	
	public void savePost(int type, int y, int displayAt, String comment, String thumb, String userName, long createdAt){
		Log.d (TAG, "savePost()");
		
		SQLiteDatabase db = getWritableDatabase();
		
		ContentValues values = new ContentValues();
		values.put(WaveSQLiteHelper.TABLE_POST_COLUMN_TYPE, type);
		values.put(WaveSQLiteHelper.TABLE_POST_COLUMN_Y, y);
		values.put(WaveSQLiteHelper.TABLE_POST_COLUMN_COMMENT, comment);
		values.put(WaveSQLiteHelper.TABLE_POST_COLUMN_THUMB, thumb);
		values.put(WaveSQLiteHelper.TABLE_POST_COLUMN_USERNAME, userName);
	    values.put(WaveSQLiteHelper.TABLE_POST_COLUMN_DISPLAY_AT, displayAt);
	    values.put(WaveSQLiteHelper.TABLE_POST_COLUMN_CREATED_AT, createdAt);
	    
	    db.insert(WaveSQLiteHelper.TABLE_POST, null, values);
	    db.close();
	}

	/////
	//
	// TABLE_POST_COUNT
	//
	/////
	public void createTablePostCount(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE IF NOT EXISTS " +
				TABLE_POST_COUNT + "(" + TABLE_POST_COUNT_COLUMN_ID + " integer primary key autoincrement, " +
				TABLE_POST_COUNT_COLUMN_COUNT + " integer not null, " +
				TABLE_POST_COUNT_COLUMN_INDICATOR_INDEX + " integer not null " +
				")");
	}
	
	public void savePostCount(int indicatorIndex, int count){
		Log.d (TAG, "savePostCount()");
		
		SQLiteDatabase db = getWritableDatabase();
		
		ContentValues values = new ContentValues();
		values.put(WaveSQLiteHelper.TABLE_POST_COUNT_COLUMN_INDICATOR_INDEX, indicatorIndex);
		values.put(WaveSQLiteHelper.TABLE_POST_COUNT_COLUMN_COUNT, count);
	    
	    db.insert(WaveSQLiteHelper.TABLE_POST_COUNT, null, values);
	    db.close();
	}
	
	public void incrementPostCount(int indicatorIndex) {
	    SQLiteDatabase db = getWritableDatabase();
	    String sql = "UPDATE " + WaveSQLiteHelper.TABLE_POST_COUNT + " SET count = count + 1 WHERE "
	    		+ WaveSQLiteHelper.TABLE_POST_COUNT_COLUMN_INDICATOR_INDEX + "= ? ";
	    db.execSQL (sql, new String[] { String.valueOf(indicatorIndex) });
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
	public Integer getCreatePostCountRequestStatus(String uuid){
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

	
	/////
	//
	// TABLE_GET_POST_COUNT_REQUEST
	//
	/////
	public void createTableGetPostCountRequest(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE IF NOT EXISTS " +
				TABLE_GET_POST_COUNT_REQUEST + "(" + 
				TABLE_GET_POST_COUNT_REQUEST_COLUMN_ID + " integer primary key, " +
				TABLE_GET_POST_COUNT_REQUEST_COLUMN_STATUS + " integer not null, " +
				TABLE_GET_POST_COUNT_REQUEST_COLUMN_RESULT + " integer not null " +
				")");
	}

	/*
	 * return null if request doesn't exist.
	 */
	public Integer getGetPostCountRequestStatus(){
		SQLiteDatabase db = getReadableDatabase();
		
		// retrieve data from SQLite
    	Cursor cursor = db.query(WaveSQLiteHelper.TABLE_GET_POST_COUNT_REQUEST, 
    			new String[] { WaveSQLiteHelper.TABLE_GET_POST_COUNT_REQUEST_COLUMN_STATUS,
    							WaveSQLiteHelper.TABLE_GET_POST_COUNT_REQUEST_COLUMN_RESULT },
				null, null, null, null, null, null);
	   
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
	
	public void saveGetPostCountRequest(int status, int result){
		SQLiteDatabase db = getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(WaveSQLiteHelper.TABLE_GET_POST_COUNT_REQUEST_COLUMN_ID, 1);
		values.put(WaveSQLiteHelper.TABLE_GET_POST_COUNT_REQUEST_COLUMN_STATUS, status);
		values.put(WaveSQLiteHelper.TABLE_GET_POST_COUNT_REQUEST_COLUMN_RESULT, result);
	    db.insert(WaveSQLiteHelper.TABLE_GET_POST_COUNT_REQUEST, null, values);
	    db.close();
	}
	
	public void updateGetPostCountRequest(int status, int result) {
	    SQLiteDatabase db = getWritableDatabase();
	    ContentValues values = new ContentValues();
		values.put(WaveSQLiteHelper.TABLE_GET_POST_COUNT_REQUEST_COLUMN_STATUS, status);
		values.put(WaveSQLiteHelper.TABLE_GET_POST_COUNT_REQUEST_COLUMN_RESULT, result);
	    db.update(WaveSQLiteHelper.TABLE_GET_POST_COUNT_REQUEST, values, WaveSQLiteHelper.TABLE_GET_POST_COUNT_REQUEST_COLUMN_ID + " = 1", null);
		db.close();
	}

	
	/////
	//
	// TABLE_PUT_POST_COUNT_REQUEST
	//
	/////
	public void createTablePutPostCountRequest(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE IF NOT EXISTS " +
				TABLE_PUT_POST_COUNT_REQUEST + "(" + 
				TABLE_PUT_POST_COUNT_REQUEST_COLUMN_ID + " integer primary key autoincrement, " +
				TABLE_PUT_POST_COUNT_REQUEST_COLUMN_INDICATOR_INDEX + " integer not null, " +
				TABLE_PUT_POST_COUNT_REQUEST_COLUMN_STATUS + " integer not null, " +
				TABLE_PUT_POST_COUNT_REQUEST_COLUMN_RESULT + " integer not null " +
				")");
	}

	public void savePutPostCountRequest(int indicatorIndex, int status, int httpCode){
		
		SQLiteDatabase db = getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(WaveSQLiteHelper.TABLE_PUT_POST_COUNT_REQUEST_COLUMN_INDICATOR_INDEX, indicatorIndex);
		values.put(WaveSQLiteHelper.TABLE_PUT_POST_COUNT_REQUEST_COLUMN_STATUS, status);
		values.put(WaveSQLiteHelper.TABLE_PUT_POST_COUNT_REQUEST_COLUMN_RESULT, httpCode);
	    db.insert(WaveSQLiteHelper.TABLE_PUT_POST_COUNT_REQUEST, null, values);
	    db.close();
	    
	}
	
	public void updateRequestingPutPostCountRequest(int indicatorIndex, int status, int httpCode) {
		
	    SQLiteDatabase db = this.getWritableDatabase();
	    int id=0;
	    
	    // retrieve primary key from SQLite
    	Cursor cursor = db.query(WaveSQLiteHelper.TABLE_PUT_POST_COUNT_REQUEST, 
    			new String[] { WaveSQLiteHelper.TABLE_PUT_POST_COUNT_REQUEST_COLUMN_ID },
				WaveSQLiteHelper.TABLE_PUT_POST_COUNT_REQUEST_COLUMN_INDICATOR_INDEX + "=? and " + WaveSQLiteHelper.TABLE_PUT_POST_COUNT_REQUEST_COLUMN_STATUS + "=?",
	            new String[] { String.valueOf(indicatorIndex), String.valueOf(RestClient.REST_REQUEST_STATUS_REQUESTING) }, null, null, null, null);
    	if (cursor != null){
	        cursor.moveToFirst();
	        if(cursor.getCount() > 0){
	        	id = Integer.parseInt(cursor.getString(0));
	        }
	        cursor.close();
    	}
    	
	    ContentValues values = new ContentValues();
		values.put(WaveSQLiteHelper.TABLE_PUT_POST_COUNT_REQUEST_COLUMN_STATUS, status);
	    db.update(WaveSQLiteHelper.TABLE_PUT_POST_COUNT_REQUEST, values, WaveSQLiteHelper.TABLE_PUT_POST_COUNT_REQUEST_COLUMN_ID + " = ?",
	            new String[] { String.valueOf(id) });
		db.close();
		
	}	
}
