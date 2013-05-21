package jp.cleartouch.wave;

import jp.cleartouch.postcast.R;
import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.util.TypedValue;
import android.view.View.MeasureSpec;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.RelativeLayout.LayoutParams;

public class PostData {

	private static final String TAG = PostData.class.getSimpleName();
	
	private int y;
	private int width; // width in pixel
	private int height; // hight in pixel
	private int slideInAnimationVerocity; // velocity of SlideInAnimation 
	private int displayAt; // posted time in sec
	private String text;
	private String userName;
	private String createdDate;
	private String createdTime;
	private byte[] thumbData;
	
	public PostData(Context context, int y, int screenWidth, int display_at, String text, byte[] thumb_data, String user_name, String created_date, String created_time){
		this.setY(y);
		this.setDisplayAt(display_at);
		this.text = text;
		this.thumbData = thumb_data;
		this.userName = user_name;
		this.createdDate = created_date;
		this.createdTime = created_time;
		
		// following codes are pulled from PostView.java
		// instantiate TextView only for measuring purpose.
		TextView tv = new TextView(context);
        tv.setTypeface(null, Typeface.BOLD);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        tv.setBackgroundResource(R.drawable.postview_bg);
        tv.setMinimumWidth((int) Helpers.convertDpToPixel(85, context));
        tv.setText(text);
        tv.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
        this.width = tv.getMeasuredWidth();
        this.height = tv.getMeasuredHeight();
        
		this.slideInAnimationVerocity = (int) ((this.width + screenWidth) * 1000 / (double) WavePlayer.SLIDE_IN_DURATION);
		
        //Log.d(TAG,text+" w:" + this.width + " h:" + height + " v:" + v + " screenWidth:" + screenWidth); 
	}
	
	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(String createdDate) {
		this.createdDate = createdDate;
	}

	public String getCreatedTime() {
		return createdTime;
	}

	public void setCreatedTime(String createdTime) {
		this.createdTime = createdTime;
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
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	public int getSlideInAnimationVerocity() {
		return slideInAnimationVerocity;
	}
	
	public int getDisplayAt() {
		return displayAt;
	}

	public void setDisplayAt(int displayAt) {
		this.displayAt = displayAt;
	}
	
}
