package jp.cleartouch.postcast;

import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.view.animation.TranslateAnimation;

public class HideControlAnimation extends TranslateAnimation{

    public HideControlAnimation() {
    	super(
                Animation.RELATIVE_TO_SELF,   //fromXType 
                0.0f,                         //fromXValue
                Animation.RELATIVE_TO_PARENT, //toXType
                0.0f,                        //toXValue
                Animation.RELATIVE_TO_SELF,   //fromYType 
                0.0f,                         //fromYValue
                Animation.RELATIVE_TO_SELF,   //toYType
                -1.0f                          //toYValue
              );
    }

    private long mElapsedAtPause=0;
    private boolean mPaused=false;

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

}