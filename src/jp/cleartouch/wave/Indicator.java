package jp.cleartouch.wave;

import java.util.ArrayList;

import jp.cleartouch.sqlite.WaveSQLiteHelper;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.util.SparseIntArray;


public class Indicator {

	private ArrayList<IndicatorColumn> mColumns;
	
	public Indicator(WavePlayer wavePlayer){
		super();
		
		this.wavePlayer = wavePlayer;
		mediaDuration = wavePlayer.getMediaDuration();
		secPerColumn = (int) Math.ceil( (double) mediaDuration / NUM_OF_COLUMS );
		mColumns = new ArrayList<IndicatorColumn>();
		postCounts = new SparseIntArray();
		
		IndicatorColumn column;
		int x = 18; // initial x in dp
		int y = 6;  // initial y in dp
		for ( int i = 0; i < NUM_OF_COLUMS; ++i ) {
			column = new IndicatorColumn(wavePlayer.getActivity(), x, y, 0);
			wavePlayer.getControl().addView(column);
			mColumns.add(column);
			x += 22;
		}
		wavePlayer.getSeekBar().bringToFront();
		
	}

	/*
	 * retrieve post counts from SQLite and update indicator. 
	 */
	public void update(){
		Log.d (TAG, "update()");
		
		if( mediaDuration <= 0 ) return;
		
		Activity activity = wavePlayer.getActivity();
		activity.runOnUiThread(new Runnable() {
	        public void run() {
	        	
	        	WaveSQLiteHelper dbHelperObject = new WaveSQLiteHelper(wavePlayer.getActivity());
	    		SQLiteDatabase db = dbHelperObject.getReadableDatabase();
	    		
	        	Cursor cursor = db.query(WaveSQLiteHelper.TABLE_POST_COUNT, 
	        			new String[] { WaveSQLiteHelper.TABLE_POST_COUNT_COLUMN_INDICATOR_INDEX, WaveSQLiteHelper.TABLE_POST_COUNT_COLUMN_COUNT},
	    				null,null, null, null, null, null);
	    	   
	        	if (cursor != null){
	    	        cursor.moveToFirst();
	        	
	    	    	while( ! cursor.isAfterLast() ){
	    	    		int index = Integer.parseInt(cursor.getString(0));
	    	    	    int count = Integer.parseInt(cursor.getString(1));
	    	    	    //Log.e (TAG, " countAt:"+countAt+" count:"+count);
	    	    	    postCounts.put(index, count);
	    	        	cursor.moveToNext();
	    	    	}
	    	    	cursor.close();
	        	}
	    		db.close();
	    		
	    		IndicatorColumn column;
	    		int x = 18; // initial x in dp
	    		int y = 6;  // initial y in dp
	    		for ( int i = 0; i < NUM_OF_COLUMS; ++i ) {
	    			column = new IndicatorColumn(wavePlayer.getActivity(), x, y, calculateLevelOfColumn(i));
	    			wavePlayer.getControl().addView(column);
	    			mColumns.add(column);
	    			x += 22;
	    		}
	    		wavePlayer.getSeekBar().bringToFront();	
	        }
	    });
		
	}
	
	private int calculateLevelOfColumn(int columnIndex){
		//Log.d (TAG, "calculateLevelOfColumn("+columnIndex+")");
		
		int avgCount =  (int) Math.ceil( (double) postCounts.get(columnIndex) / secPerColumn);
		
		if( avgCount <= 0 )
			return 0;
		else if( 1<=avgCount && avgCount<=2 )
			return 1;
		else if( 3<=avgCount && avgCount<=4 )
			return 2;
		else if( 5<=avgCount && avgCount<=6 )
			return 3;
		else if( 7<=avgCount && avgCount<=8 )
			return 4;
		else if( 9<=avgCount && avgCount<=12 )
			return 5;
		else if( 13<=avgCount && avgCount<=16 )
			return 6;
		else if( 17<=avgCount && avgCount<=20 )
			return 7;
		else if( 21<=avgCount && avgCount<=28 )
			return 8;
		else if( 29<=avgCount )
			return 9;
		
		return 0;
	}

	public int getColumnIndex(int elapsed){
		return ( elapsed / secPerColumn );
	}
	
	////
	//
	//  Class Variables
	//
	////
	private static String TAG = "Indicator";
	public static int NUM_OF_COLUMS = 13;
	private WavePlayer wavePlayer;
	private int mediaDuration = 0;
	private int secPerColumn = 0;
	private SparseIntArray postCounts;
}
