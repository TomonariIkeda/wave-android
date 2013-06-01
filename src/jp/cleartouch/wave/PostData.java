package jp.cleartouch.wave;

import jp.cleartouch.wave.R;
import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.util.TypedValue;
import android.view.View.MeasureSpec;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.RelativeLayout.LayoutParams;

public class PostData {
	
	public PostData(Context context, int type, int y, int screenWidth, int display_at, String comment, byte[] thumb_data, String user_name, long created_at){
		this.setType(type);
		this.setY(y);
		this.setDisplayAt(display_at);
		this.setComment(comment);
		this.setCreatedAt(created_at);
		this.thumbData = thumb_data;
		this.userName = user_name;
		
		// following codes are pulled from PostView.java
		// instantiate TextView only for measuring purpose.
		/*
		TextView tv = new TextView(context);
        tv.setTypeface(null, Typeface.BOLD);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        tv.setBackgroundResource(R.drawable.postview_bg);
        tv.setMinimumWidth((int) Helpers.convertDpToPixel(85, context));
        tv.setText(comment);
        tv.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
        
        this.width = tv.getMeasuredWidth();
        this.height = tv.getMeasuredHeight();
        
		this.slideInAnimationVerocity = (int) ((this.width + screenWidth) * 1000 / (double) WavePlayer.SLIDE_IN_DURATION);
		*/
		
        //Log.d(TAG,text+" w:" + this.width + " h:" + height + " v:" + v + " screenWidth:" + screenWidth); 
	}
	
	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public long getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(long createdAt) {
		this.createdAt = createdAt;
	}

	public byte[] getThumbData() {
		return thumbData;
	}

	public void setThumbData(byte[] thumbData) {
		this.thumbData = thumbData;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}
	/*
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	public int getSlideInAnimationVerocity() {
		return slideInAnimationVerocity;
	}
	*/
	public int getDisplayAt() {
		return displayAt;
	}
	
	public void setDisplayAt(int displayAt) {
		this.displayAt = displayAt;
	}

	
	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}


	////
	//
	//  Class Variables
	//
	////
	private static final String TAG = PostData.class.getSimpleName();
	public static final int TYPE_CLAP = 0;
	public static final int TYPE_COMMENT = 1;
	
	private int type;
	private int y;
	//private int width; // width in pixel
	//private int height; // hight in pixel
	//private int slideInAnimationVerocity; // velocity of SlideInAnimation 
	private int displayAt; // posted time in sec
	private String comment;
	private String userName;
	private long createdAt;
	private byte[] thumbData;
}
