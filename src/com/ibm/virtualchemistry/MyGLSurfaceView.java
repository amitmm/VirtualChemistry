package com.ibm.virtualchemistry;

import android.annotation.SuppressLint;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

public class MyGLSurfaceView extends GLSurfaceView {
	public MyGLRenderer mRenderer;
	public static float yawAngle;
	public float rotationAngle;
	private float mPreviousY = 0;
	
    public MyGLSurfaceView(Context context) {
        super(context);
    	// Create an OpenGL ES 2.0 context.
    	setEGLContextClientVersion(2);

    	// Set the Renderer for drawing on the GLSurfaceView
    	mRenderer = new MyGLRenderer(context);
    
    	//initialize for the stencil buffer
    	setEGLConfigChooser(8, 8, 8, 8, 16, 8);
    
    	setRenderer(mRenderer);
   
    	// Render the view only when there is a change in the drawing data
    	setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        //setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }
    
    @SuppressLint("ClickableViewAccessibility")
	public boolean onTouchEvent(MotionEvent e) {
	    // MotionEvent reports input details from the touch screen
	    // and other input controls. In this case, you are only
	    // interested in events where the touch position changed.

	    float y = e.getY();

	    switch (e.getAction()) {
	        case MotionEvent.ACTION_MOVE: //if user scrolls up or down
	        	float dy = y - mPreviousY; 
	        	translate(-dy/(320*80)); //translate liquid Quad up by dy
	        	return true;
	    }
	    mPreviousY = y;
	    return true;
	}
    
    public void reaction() {
    	if (BluetoothClass.chatService.getDevice() == BluetoothChatService.DEVICE_SECONDARY) {
    		if(!BluetoothClass.message.contains("null") || MainActivity.lightReaction || MainActivity.shake) {
    			requestRender();
    		}	
    	}
    }
    
    public void rotate(float[] R) {
    	//directions to rotate image on the screen
    	yawAngle = (float) Math.toDegrees(Math.atan2(R[6], R[7])); //angle by which we will apply rotation matrix
    	if (yawAngle < 90 && yawAngle > -90) {
    		rotationAngle = (float) (-40.25f * MyGLRenderer.height + 65.05f);
    		if(yawAngle > rotationAngle || yawAngle < -rotationAngle) { 
    			MyGLRenderer.reduce = true;
    			//requestRender();
    			return;
	    	}
    		
			/*float angleChange = yawAngle - mPreviousAngle;
			if(angleChange > 2 || angleChange < -2) {	
				mPreviousAngle = yawAngle;
				requestRender();
			}*/	
		}
	}
    
    public void translate(float dy) {
	    	mRenderer.changeLiquidCoords(dy);
	    }
}


		

		




