
package com.ibm.virtualchemistry;

import java.util.Arrays;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

/**
 * Provides drawing instructions for a GLSurfaceView object. This class
 * must override the OpenGL ES drawing lifecycle methods:
 * <ul>
 *   <li>{@link android.opengl.GLSurfaceView.Renderer#onSurfaceCreated}</li>
 *   <li>{@link android.opengl.GLSurfaceView.Renderer#onDrawFrame}</li>
 *   <li>{@link android.opengl.GLSurfaceView.Renderer#onSurfaceChanged}</li>
 * </ul>s
 */
public class MyGLRenderer implements GLSurfaceView.Renderer {

    private static final String TAG = "MyGLRenderer";
    private LiquidBack mLiquidBack;
    
    private texturedQuad Foam;
    private texturedQuad Stencil;
    private texturedQuad Bubbles;
    
    BluetoothClass mBluetooth;
    
    // mMVPMatrix is an abbreviation for "Model View Projection Matrix"
    private final float[] mMVPMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];
    private final float[] mRotationMatrix = new float[16];

    private final float[] foamTransform = new float[16];
    private final float[] liquidTransform = new float[16];
    private final float[] bubbleTransform = new float[16];

    public static Context mContext;
    public static boolean reduce = false;
    public float offset = 0.0f;
    public float foamOffset;
    public static float height;
    
    public int foamImageCounter = 15;
    public String imageName;
    public int colorChangeCounter = 0;
    public int delayCounter = 0;
    
    public static boolean reaction = false;
    public static boolean colorChangeEffect;
    public boolean stopChangingColor = false;
    public static boolean reacted = false;
    private int liquid2BackgroundImage = R.drawable.white;
    
    //initialize color coordinates for liquid on second device
    public float [] secondLiquidColors = Arrays.copyOf(MainActivity.info.colorValues.get(MainActivity.info.chemical2Color), 16);
    
    //coordinates for all our openGL objects
	private float liquidRectCoords[] = {
		 -1.8f,  0.0f, 0.0f,   // top left
		 -1.8f, -1.30f, 0.0f,   // bottom left
         1.8f, -1.30f, 0.0f,   // bottom right
         1.8f,  0.0f, 0.0f }; // top right
	 
	private float foamRectCoords[] = {
		 -1.8f,  0.4f, 0.0f,   // top left
		 -1.8f, 0.0f, 0.0f,   // bottom left
          1.8f, 0.0f, 0.0f,   // bottom right
          1.8f,  0.4f, 0.0f }; // top right
	 
	private float bubbleRectCoords[] = {
		 -1.8f, 1.9f, 0.0f, //top left
		 -1.8f, 0.4f, 0.0f, //bottom left
		 1.8f, 0.4f, 0.0f, //bottom right 
		 1.8f, 1.9f, 0.0f }; //top right
	
	private float stencilRectCoords[] = {
			 -0.8f, 1.4f, 0.0f,   // top left
			 -0.8f, -1.3f, 0.0f,   // bottom left
	          0.8f, -1.3f, 0.0f,   // bottom right
	          0.8f, 1.4f, 0.0f }; // top right
			
    public MyGLRenderer(Context context) {
    	mContext = context;
	}
    
	public void onSurfaceCreated(GL10 unused, EGLConfig config)  {
        
    	//Set the clear color to black
    	GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1);	
	
    	//Initialize objects for liquid animation
        mLiquidBack = new LiquidBack();
        Foam = new texturedQuad(Shader.vs_Texture, Shader.fs_Texture);
        Stencil = new texturedQuad(Shader.vs_Texture, Shader.fs_StencilShader);
        Bubbles = new texturedQuad(Shader.vs_Texture, Shader.fs_Texture);
    }
	
	/**
	 * This method draws scene to screen every time request render is called
	 */
    public void onDrawFrame(GL10 unused) {
        
        // Draw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_STENCIL_BUFFER_BIT);
        
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        
        //Set up for stencil buffer
        GLES20.glEnable(GLES20.GL_STENCIL_TEST);        
        
        //Disable depth masks
        GLES20.glDepthMask(false);
        
       //Directions for stencil function        	
        GLES20.glStencilFunc(GLES20.GL_ALWAYS, 1, 1);
        GLES20.glStencilOp(GLES20.GL_KEEP, GLES20.GL_KEEP, GLES20.GL_REPLACE);
        
        // Set the camera position (View matrix)
        Matrix.setLookAtM(mViewMatrix, 0, 0, 0, -3, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
     
        // Calculate the model view projection matrix
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);
        
        //Draw in stencil mask
        Stencil.draw(mMVPMatrix, R.drawable.beaker, mContext,stencilRectCoords);

        //Enable depth mask again
        GLES20.glDepthMask(true);
        
        //Directions for stencil
        GLES20.glStencilFunc(GLES20.GL_EQUAL, 1, 1);
        GLES20.glStencilOp(GLES20.GL_KEEP, GLES20.GL_KEEP, GLES20.GL_KEEP);
        
        // Create a rotation matrix for the Foam
        Matrix.setRotateM(mRotationMatrix, 0, MyGLSurfaceView.yawAngle, 0, 0, 1.0f);

        // Create transformation matrix for foam using rotation and mMVP matrices
        // Note that the mMVPMatrix factor *must be first* in order
        // for the matrix multiplication product to be correct.
        Matrix.multiplyMM(foamTransform, 0, mMVPMatrix, 0, mRotationMatrix, 0);
        
        //Create transformation matrix for liquid using rotation and mMVP matrices
        Matrix.multiplyMM(liquidTransform, 0, mMVPMatrix, 0, mRotationMatrix, 0);
        
        //Create transformation matrix for bubble object
        Matrix.multiplyMM(bubbleTransform, 0, mMVPMatrix, 0, mRotationMatrix, 0);
        
        //Translate foam and liquid with respect to input from tilt
        //Reduce only true if accelerometer angle is enough
        if(reduce) {
        	if(MyGLSurfaceView.yawAngle < 75.0f || MyGLSurfaceView.yawAngle > -75.0f)
        		offset -= 0.01f;
        	else
        		offset -= 0.02f;
            reduce = false;
            if(BluetoothClass.chatService.getDevice() == BluetoothChatService.DEVICE_PRIMARY)
            	//send message from first device to the second device
            	BluetoothClass.message = Float.toString(MyGLSurfaceView.yawAngle);
        }
        
        //Right now message only contains yaw angle-so reduce is true
        //If message is not empty, level is reducing on device 1, so it should rise on device 2
        if(BluetoothClass.chatService.getDevice() == BluetoothChatService.DEVICE_SECONDARY) {
        	if(!BluetoothClass.message.contains("null")) {
        		changeLiquidCoords(.04f); //separate heights for device 1 and 2 automatically
        		reaction = true;
        		delayCounter ++; //increment delay counter by 1 after pouring has started
        	}	
        }
        
        //Translate foam based on y position of liquid coordinates, so foam is directly on top
        foamOffset = liquidRectCoords[1];
        height = foamOffset + offset;
        
        //translate objects on screen according to tilt offset and foam offset
        Matrix.translateM(foamTransform, 0, 0, height - 0.09f, 0);
        Matrix.translateM(liquidTransform, 0, 0, offset, 0);  
        Matrix.translateM(bubbleTransform, 0, 0, offset-.3f, 0);
        
        //On every render, draw new foam image
        if(foamImageCounter > 80)
        	foamImageCounter = 15;
        imageName = "foam_00" + foamImageCounter;
        foamImageCounter +=1; 
        int resID = mContext.getResources().getIdentifier(imageName , "drawable", mContext.getPackageName());
        
        //When bluetooth connection is not false, begin "reaction"
        if (MainActivity.bluetoothConnection != false) {
        	
        	//First draw in optional bubble object behind foam on second device, after optional delay
        	if (BluetoothClass.chatService.getDevice() == BluetoothChatService.DEVICE_SECONDARY &&
        		colorChangeEffect
        		&& MainActivity.info.bubbles.contains("yes"))
        			Bubbles.draw(bubbleTransform, R.drawable.bubbles, mContext, bubbleRectCoords);
        	
        	//Then draw foam
        	Foam.draw(foamTransform, resID, mContext, foamRectCoords);
        	
        	//Draw liquid in first Device
        	if(BluetoothClass.chatService.getDevice() == BluetoothChatService.DEVICE_PRIMARY) {
        		mLiquidBack.draw(liquidTransform, R.drawable.white, mContext, MainActivity.info.colorValues.get(MainActivity.info.chemical1Color), liquidRectCoords);
        	}
        
        	//2nd device
        	if (BluetoothClass.chatService.getDevice() == BluetoothChatService.DEVICE_SECONDARY) {
            	if (reaction) {
            		if (delayCounter >= MainActivity.info.delay) {

                		if (MainActivity.info.light.contains("yes") && !(MainActivity.info.shake.contains("yes"))) {
                			if (MainActivity.lightReaction)
                				colorChangeEffect = true;
                		}
                		
                		else if (!(MainActivity.info.light.contains("yes")) && (MainActivity.info.shake.contains("yes"))) {
                			if (MainActivity.shake)
                				colorChangeEffect = true;
                		}
                		
                		else if (MainActivity.info.light.contains("yes") && (MainActivity.info.shake.contains("yes"))) {
                			if (MainActivity.lightReaction && MainActivity.shake)
                				colorChangeEffect = true;
                		}
                		
                		else {
                			colorChangeEffect = true;
                		}
            		}

            	}
        		
        		if (colorChangeEffect && !stopChangingColor) {
        			
        			//change color over 30 renders (number can change)
        			changeColors(MainActivity.info.colorValues.get(MainActivity.info.chemical2Color),
            				30, MainActivity.info.colorValues.get(MainActivity.info.productColor));
        			
        			colorChangeCounter ++;
        			//"Reaction" ends after 30 renders
        			if (colorChangeCounter == 30)
        				stopChangingColor = true;
        				reacted = true; //now change information in display text in main activity
        				
        				//now add in optional sediment image change
        				if (MainActivity.info.sedResult.contains("yes")) //sediment photo has appeared instead of white 
        					liquid2BackgroundImage = R.drawable.white;
        		}
        		mLiquidBack.draw(liquidTransform, liquid2BackgroundImage, mContext, secondLiquidColors, liquidRectCoords);
        		BluetoothClass.message = "null"; //remember to reset message to null at the end of every render
        	}
        }

        //Disable stencil test after liquid and foam objects have drawn inside stencil
        GLES20.glDisable(GLES20.GL_STENCIL_TEST);
    }

    
    public void onSurfaceChanged(GL10 unused, int width, int height) {
    	// Adjust the viewport based on geometry changes,
        // such as screen rotation
        GLES20.glViewport(0, 0, width, height);

        float ratio = (float) width / height;

        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, 2, 7);
    }

    /**
    * Utility method for debugging OpenGL calls. Provide the name of the call
    * just after making it:
    * <pre>
    * mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
    * MyGLRenderer.checkGlError("glGetUniformLocation");</pre>
    * If the operation is not successful, the check throws an error.
    * @param glOperation - Name of the OpenGL call to check.
    */
    public static void checkGlError(String glOperation) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, glOperation + ": glError " + error);
            throw new RuntimeException(glOperation + ": glError " + error);
        }
    }

    /**
     * Method to change rectangle size for liquid object
     * @param dy-amount to add to y coordinates for top right and left vertex
     */
    public void changeLiquidCoords(float dy) {
		liquidRectCoords[1] += dy; //top left vertex
		liquidRectCoords[10] += dy; //top right vertex
		bubbleRectCoords[4] += dy;
		bubbleRectCoords[7] +=dy;
	}
	
    /**
     * Sets new color coordinates for liquid object
     * @param changedColorCoords
     */
	public void setColor(float [] changedColorCoords) {
		secondLiquidColors = changedColorCoords;
	}
	
	/**
	 * Method creating color changing effect    
	 * @param initialColorCoords-float [] for initial color
	 * @param goalColors-final colors
	 * @param gradientChanges-number of color coordinate changes
	 */
    public void changeColors(float [] initialColorCoords, int gradientChanges, float [] goalColors) {
    	
    	float rtopChange = goalColors[0] - initialColorCoords[0];
    	float gtopChange = goalColors[1] - initialColorCoords[1];
    	float btopChange = goalColors[2] - initialColorCoords[2];
    	
    	float rbotChange = goalColors[4] - initialColorCoords[4];
    	float gbotChange = goalColors[5] - initialColorCoords[5];
    	float bbotChange = goalColors[6] - initialColorCoords[6];
    	
    	float rtop1 = secondLiquidColors[0];
		float gtop1 = secondLiquidColors[1];
		float btop1 = secondLiquidColors[2];  
		float rbot1 = secondLiquidColors[4];
		float gbot1 = secondLiquidColors[5];
		float bbot1 = secondLiquidColors[6];
		
		rtop1 += (rtopChange)/gradientChanges;
		gtop1 += (gtopChange)/gradientChanges;
		btop1 += (btopChange)/gradientChanges;
			
		rbot1 += (rbotChange)/gradientChanges;
		gbot1 += (gbotChange)/gradientChanges;
		bbot1 += (bbotChange)/gradientChanges;
			
		float [] currentColorCoords = {rtop1, gtop1, btop1, 1.0f, 
									   rbot1, gbot1, bbot1, 1.0f,
									   rbot1, gbot1, bbot1, 1.0f,
									   rtop1, gtop1, btop1, 1.0f };
		setColor(currentColorCoords);
    }
    
    /**
	 * Method creating color changing effect    
	 * @param initialColorCoords-float [] for initial color
	 * @param goalColors-final colors
	 * @param gradientChanges-number of color coordinate changes
	 */
    public void changeColors2(float [] initialColorCoords, int gradientChanges, float [] goalColors) {
    	
    	//numbers in color array we need to change
    	int [] colorOrder = {0, 1, 2, 4, 5, 6, 8, 9, 10, 12, 13, 14}; 
    	
    	for (int i = 0; i < colorOrder.length; i ++ ) {
    		int pos = colorOrder[i];
    		//increment each coordinate by factor based on initial and final coordinate
    		secondLiquidColors[pos] += (initialColorCoords[pos] - goalColors[pos])/gradientChanges;
    	}
    }

}

	

