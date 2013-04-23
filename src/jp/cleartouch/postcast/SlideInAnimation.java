package jp.cleartouch.postcast;

import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.view.animation.TranslateAnimation;

public class SlideInAnimation extends TranslateAnimation{

    public SlideInAnimation(PostView postView) {
    	super(
                Animation.RELATIVE_TO_SELF,   //fromXType 
                1.0f,                         //fromXValue
                Animation.RELATIVE_TO_PARENT, //toXType
                -1.0f,                        //toXValue
                Animation.RELATIVE_TO_SELF,   //fromYType 
                0.0f,                         //fromYValue
                Animation.RELATIVE_TO_SELF,   //toYType
                0.0f                          //toYValue
              );
    	// set reference to PostView
    	pv = postView;
    }

    private long mElapsedAtPause=0;
    private boolean mPaused=false;
    private PostView pv;

    @Override
    public boolean getTransformation(long currentTime, Transformation outTransformation) {
        if(mPaused && mElapsedAtPause==0) {
            mElapsedAtPause=currentTime-getStartTime();
        }
        if(mPaused)
            setStartTime(currentTime-mElapsedAtPause);
        return super.getTransformation(currentTime, outTransformation);
    }

    public void pause() {
        mElapsedAtPause=0;
        mPaused=true;
    }

    public void resume() {
        mPaused=false;
    }
    
    public PostView getPostView(){
    	return pv;
    }
}