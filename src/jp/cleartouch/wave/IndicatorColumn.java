package jp.cleartouch.wave;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.ShapeDrawable;
import android.view.View;

public class IndicatorColumn extends View {

	private ArrayList<ShapeDrawable> mDrawables;
	private final int GRAY = 0xff343434;
	private final int GREEN = 0xff70c403;
	private final int YELLOW = 0xffbfc403;
	private final int RED = 0xffc41503;
	
	public IndicatorColumn(Context context, int x, int y, int level) {
		super(context);
		
		mDrawables = new ArrayList<ShapeDrawable>();
		
		int xPx = (int) Helpers.convertDpToPixel(x, context);
	    int yPx = (int) Helpers.convertDpToPixel(y, context);
	    int widthPx = (int) Helpers.convertDpToPixel(20, context);
	    int heightPx = (int) Helpers.convertDpToPixel(3, context);	    
	    
	    ShapeDrawable cell;
		for ( int i = 0; i < 2; ++i ) {
			cell = new ShapeDrawable();
		    cell.getPaint().setColor(RED);
		    cell.setBounds(xPx, yPx, xPx + widthPx, yPx + heightPx);
		    mDrawables.add(cell);
		    yPx += (int) Helpers.convertDpToPixel(4, context);
		}
		for ( int i = 0; i < 3; ++i ) {
			cell = new ShapeDrawable();
		    cell.getPaint().setColor(YELLOW);
		    cell.setBounds(xPx, yPx, xPx + widthPx, yPx + heightPx);
		    mDrawables.add(cell);
		    yPx += (int) Helpers.convertDpToPixel(4, context);
		}
		for ( int i = 0; i < 4; ++i ) {
			cell = new ShapeDrawable();
		    cell.getPaint().setColor(GREEN);
		    cell.setBounds(xPx, yPx, xPx + widthPx, yPx + heightPx);
		    mDrawables.add(cell);
		    yPx += (int) Helpers.convertDpToPixel(4, context);
		}
		
		yPx = (int) Helpers.convertDpToPixel(y, context);
		int counter = 9-level;
		for ( int i = 0; i < counter; ++i ) {
			cell = new ShapeDrawable();
		    cell.getPaint().setColor(GRAY);
		    cell.setBounds(xPx, yPx, xPx + widthPx, yPx + heightPx);
		    mDrawables.add(cell);
		    yPx += (int) Helpers.convertDpToPixel(4, context);
		}
	}
	
	protected void onDraw(Canvas canvas) {
		for(ShapeDrawable cell : mDrawables){
			cell.draw(canvas);
		}
	}
	
}
