package pt.ua.code.favouritetv.gui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector.OnGestureListener;

import com.google.android.maps.MapView;

public class MyMap extends MapView {
	
	private GestureDetector gestureDetector;
	public MyMap(Context context, AttributeSet attrs) {
		super(context, attrs);
	 
	    gestureDetector = new GestureDetector((OnGestureListener)context);
	    gestureDetector.setOnDoubleTapListener((OnDoubleTapListener) context);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		 if(this.gestureDetector.onTouchEvent(ev))
		       return true;
		 else
		      return super.onTouchEvent(ev);
	}
}
