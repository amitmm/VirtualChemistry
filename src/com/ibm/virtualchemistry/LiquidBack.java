package com.ibm.virtualchemistry;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;

public class LiquidBack {
	private final ShortBuffer drawListBuffer;
	private final FloatBuffer textureBuffer;  // buffer holding the texture coordinates
	private final int mProgram;
	private int mPositionHandle;
	private int mColorHandle;
	private int mTextureCoordinateHandle;//used to pass in model texture coordinate information
	private final int mTextureCoordinateDataSize = 2; //size of the texture coordinate data in elements
	private int mTextureDataHandle; //used to store info about image texture we are loading
	private int mTextureUniformHandle; 
    private int mMVPMatrixHandle;
    private static int[] textures = new int[1];
    
	 // number of coordinates per vertex in this array
	 static final int COORDS_PER_VERTEX = 3;
	 float color[] = { 0.2f, 0.709803922f, 0.898039216f, 1.0f };
	 
	 //Get display width and height
	 
	 private final short drawOrder[] = { 0, 1, 2, 0, 2, 3 }; // order to draw vertices

	 static float textureCoords[] = {
		 0.0f, 0.0f, //top left
		 0.0f, 1.0f, //bottom left
		 1.0f, 1.0f, //bottom right
		 1.0f, 0.0f }; //top right

	 public LiquidBack() {

		 // initialize byte buffer for the draw list (# of coordinate values * 2 bytes per short)
		 ByteBuffer dlb = ByteBuffer.allocateDirect(drawOrder.length * 2);
		 dlb.order(ByteOrder.nativeOrder());
		 drawListBuffer = dlb.asShortBuffer();
		 drawListBuffer.put(drawOrder);
		 drawListBuffer.position(0);
		
		 //initialize byte buffer for texture coordinates
		 ByteBuffer tbb  = ByteBuffer.allocateDirect(textureCoords.length * 4);
		 tbb.order(ByteOrder.nativeOrder());
		 textureBuffer = tbb.asFloatBuffer();
		 textureBuffer.put(textureCoords);
		 textureBuffer.position(0);
		 
		//prepare shaders to use
		 int vertexShader = Shader.loadShader(GLES20.GL_VERTEX_SHADER, Shader.vs_ColoredTexture);
		 int fragmentShader = Shader.loadShader(GLES20.GL_FRAGMENT_SHADER, Shader.fs_ColoredTexture);
		
		 mProgram = GLES20.glCreateProgram();             // create empty OpenGL ES Program
		 GLES20.glAttachShader(mProgram, vertexShader);   // add the vertex shader to program
		 GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program
		 GLES20.glLinkProgram(mProgram);                  // creates OpenGL ES program executables
		 }
	 
	 public void draw(float[] mvpMatrix, int resourceID, Context context, float[] colors, float [] rectangleCoords) {
		 
		 //initialize byte buffer for rectangle coordinates
		// initialize vertex byte buffer for shape coordinates (# of coordinate values * 4 bytes per float)
		 ByteBuffer bb = ByteBuffer.allocateDirect(rectangleCoords.length * 4);
		 bb.order(ByteOrder.nativeOrder());
		 FloatBuffer vertexBuffer = bb.asFloatBuffer();
		 vertexBuffer.put(rectangleCoords);
		 vertexBuffer.position(0);
		//initialize byte buffer for color coordinates
		 ByteBuffer cbb = ByteBuffer.allocateDirect(colors.length*4);
		 cbb.order(ByteOrder.nativeOrder());
		 FloatBuffer colorBuffer = cbb.asFloatBuffer();
		 colorBuffer.put(colors);
		 colorBuffer.position(0);
		 
		 // Add program to OpenGL ES environment
		 GLES20.glUseProgram(mProgram);
		 
		// get handle to vertex shader's vPosition member 
		 mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
		
		 //handle to fragment's vColor member
		 mColorHandle = GLES20.glGetAttribLocation(mProgram, "a_Color");
		
		 //Get handle to texture coordinate's location
		 mTextureCoordinateHandle = GLES20.glGetAttribLocation(mProgram, "a_texCoord");
		 

		 mTextureUniformHandle = GLES20.glGetUniformLocation(mProgram, "s_texture");
		 
		 mTextureDataHandle = loadTextureFromImage (resourceID, context );
	
		 // Set the active texture unit to texture unit 0.
		 GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		 
		 // Bind the texture to this unit.
		 GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandle);
		 
		 // Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
		 GLES20.glUniform1i(mTextureUniformHandle, 0);
	 
		// Enable a handle to the rectangle vertices
		GLES20.glEnableVertexAttribArray(mPositionHandle);
		
	    // Prepare the rectangle coordinate data
	    GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
	                                 GLES20.GL_FLOAT, false,
	                                 0, vertexBuffer);
	    
	    //Enable handle to color vertices
	    GLES20.glEnableVertexAttribArray(mColorHandle);
	    
	    //Prepare color coordinate data
	    GLES20.glVertexAttribPointer(mColorHandle, 4 , GLES20.GL_FLOAT, false, 0, colorBuffer);
	
	    //Set the Color for drawing the rectangle
	    //GLES20.glUniform4fv(mColorHandle, 1, color, 0);
	    
	    //Enable generic vertex attribute array
	    GLES20.glEnableVertexAttribArray(mTextureCoordinateHandle);

	    //Prepare texture coordinates
	    GLES20.glVertexAttribPointer(mTextureCoordinateHandle, mTextureCoordinateDataSize, 
	    							GLES20.GL_FLOAT, false, 
	    							0, textureBuffer);
	    
        //Handle to shape's transformation matrix
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        MyGLRenderer.checkGlError("glGetUniformLocation");

        // Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
        MyGLRenderer.checkGlError("glUniformMatrix4fv");
       
	    GLES20.glDrawElements(
                GLES20.GL_TRIANGLES, drawOrder.length,
                GLES20.GL_UNSIGNED_SHORT, drawListBuffer);
	    
	    // Disable vertex arrays
	    GLES20.glDisableVertexAttribArray(mPositionHandle);
	    GLES20.glDisableVertexAttribArray(mTextureCoordinateHandle);
	    GLES20.glDisableVertexAttribArray(mColorHandle);
	 }
	 
	    /**
	     * Loads a texture onto an image to be drawn on an object
	     * @param resourceId-image ID
	     * @param context
	     */
	    public int loadTextureFromImage (final int resourceId, final Context context) {
	    	
	    	GLES20.glDeleteTextures(1, textures, 0);
	    	GLES20.glGenTextures(1, textures, 0);
	    	if (textures[0] != 0) {
	    		
	    		final BitmapFactory.Options options = new BitmapFactory.Options();
	    		options.inScaled = true;
	    		Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId);
		    	
	    		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);
		    	GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
		    	GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
		    	GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
		    	bitmap.recycle();
	    	}
	    	if (textures[0] == 0) {
	            throw new RuntimeException("Error loading texture.");
	        }
	        return textures[0];
	    }
	   

 }


