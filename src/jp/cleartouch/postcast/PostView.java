package jp.cleartouch.postcast;



import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


public class PostView extends RelativeLayout{
    @SuppressLint("NewApi")
	public PostView(Context context) {
        super(context);

        //this.setBackgroundColor(Color.RED);
        this.setVisibility(View.INVISIBLE);
        
        // contentText
        contentText = new TextView(context);
        contentText.setId(Helpers.generateViewId());
        contentText.setTextColor(Color.BLACK);
        contentText.setTypeface(null, Typeface.BOLD);
        contentText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        contentText.setBackgroundResource(R.drawable.postview_bg);
        // set min length and max lines so that username will be displayed correctly
        contentText.setMinimumWidth((int) Helpers.convertDpToPixel(85, context));
        RelativeLayout.LayoutParams contentTextLayoutParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        contentTextLayoutParams.topMargin = (int) Helpers.convertDpToPixel(16, context);
        contentTextLayoutParams.rightMargin = (int) Helpers.convertDpToPixel(5, context);
        this.addView(contentText, contentTextLayoutParams);
        
        // userName
        userName = new TextView(context);
        userName.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 10);
        userName.setTextColor(Color.WHITE);
        userName.setShadowLayer(1.5f, 1, 1, Color.BLACK);
        RelativeLayout.LayoutParams userNameLayoutParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        userNameLayoutParams.addRule(RelativeLayout.ALIGN_RIGHT, contentText.getId());
        this.addView(userName, userNameLayoutParams);
        
        // thumb
        thumbnail = new ImageView(context);
        thumbnail.setId(Helpers.generateViewId());
        RelativeLayout.LayoutParams thumbnailLayoutParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        thumbnailLayoutParams.addRule(RelativeLayout.RIGHT_OF, contentText.getId());
        this.addView(thumbnail, thumbnailLayoutParams);

        // postDate
        createdDate = new TextView(context);
        createdDate.setId(Helpers.generateViewId());
        createdDate.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 8);
        createdDate.setTextColor(Color.WHITE);
        createdDate.setShadowLayer(1.5f, 1, 1, Color.BLACK);
        RelativeLayout.LayoutParams createdDateLayoutParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        createdDateLayoutParams.addRule(RelativeLayout.ALIGN_LEFT, thumbnail.getId());
        createdDateLayoutParams.addRule(RelativeLayout.BELOW, thumbnail.getId());
        this.addView(createdDate, createdDateLayoutParams);
        
        // postTime
        createdTime = new TextView(context);
        createdTime.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 9);
        createdTime.setTextColor(Color.WHITE);
        createdTime.setShadowLayer(1.5f, 1, 1, Color.BLACK);
        RelativeLayout.LayoutParams createdTimeLayoutParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        createdTimeLayoutParams.addRule(RelativeLayout.ALIGN_LEFT, createdDate.getId());
        createdTimeLayoutParams.addRule(RelativeLayout.BELOW, createdDate.getId());
        createdTimeLayoutParams.topMargin = (int) Helpers.convertDpToPixel(-3, context);
        this.addView(createdTime, createdTimeLayoutParams);
        
        // layout
        layoutParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        setLayoutParams(layoutParams);
        
        // animation
		animation = new SlideInAnimation(this);
        animation.setInterpolator(new LinearInterpolator());
        animation.setDuration(5000);
        //this.setAnimation(animation);
    }
    

    @Override
    protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld){
    	
    	if( xNew > 0 && yNew > 0 ){
    		//Log.d (TAG, "Dim of PostView(" + this.contentText.getText() + "): " + (int) Helpers.convertPixelsToDp(this.getHeight(), this.getContext())  + "," + (int) Helpers.convertPixelsToDp(this.getWidth(), this.getContext()));
    		//this.state = STATE_PREPARED;
    	}
    	
    	super.onSizeChanged(xNew, yNew, xOld, yOld);
    }
    
    public void startSlideIn(){
    	Log.d (TAG, "startSlideIn()");
    	this.setVisibility(View.VISIBLE);
    	this.startAnimation(this.animation);
    }
    
    public void pauseAnimation(){
    	this.animation.pause();
    }
    
    public void resumeAnimation(){
    	this.animation.resume();
    }
    
    public void setData(String text, int thumb_res_id, String user_name,
			String created_date, String created_time, int y, int posted_at) {

    	contentText.setText(text);
    	thumbnail.setImageResource(thumb_res_id);
    	userName.setText(user_name);
    	createdDate.setText(created_date);
    	createdTime.setText(created_time);
    	layoutParams.topMargin = (int) Helpers.convertDpToPixel(y, this.getContext());
    	postedAt = posted_at;
	}
    
    public void setContentText(String text){
    	this.contentText.setText(text);
    }
    
    public void setUserName(String user_name){
    	this.userName.setText(user_name);
    }
    
    public void setThumbnail(int thumb_res_id){
    	this.thumbnail.setImageResource(thumb_res_id);
    } 
    
    public void setCreatedDate(String date){
    	this.createdDate.setText(date);
    }
    
    public void setCreatedTime(String time){
    	this.createdTime.setText(time);
    }
 
    public void setDuration(int duration){
        this.animation.setDuration(duration);
    }
    
    public void setYCoord(int y){
    	layoutParams.topMargin = (int) Helpers.convertDpToPixel(y, this.getContext());
    }
    
    public void setPostedAt(int postedAt){
    	this.postedAt = postedAt;
    }
 
    public CharSequence getContentText(){
    	return this.contentText.getText();
    }
    
    public int getPostedAt(){
    	return postedAt;
    }
    
    public int getYCoord(){
    	return yCoord;
    }
    
    public SlideInAnimation getSlideInAnimation(){
    	return animation;
    }

    
    private static final String TAG = "PostView";
    
    private int postedAt; // duration time in sec
    private int yCoord; // yCoord of postview in dp
    private int state; // PREPARING, PREPARED, STARTED, FINISHED
    private static final int STATE_PREPARING = 0;
    private static final int STATE_PREPARED = 1;
    private static final int STATE_STARTED = 2;
    private static final int STATE_FINISHED = 3;
    
    // view properties
    private TextView contentText;
    private ImageView thumbnail;
    private TextView userName;
    private TextView createdDate;
    private TextView createdTime;
    
    private RelativeLayout.LayoutParams layoutParams;
    private SlideInAnimation animation;
	
    
}
