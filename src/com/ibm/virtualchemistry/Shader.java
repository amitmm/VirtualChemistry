package com.ibm.virtualchemistry;
import android.opengl.GLES20;
public class Shader {
	
	
	/*gradient SHADER
	 * For a gradient of colors in the openGL object based on the vertex color data
	 */
	public static final String vs_Gradient = 
			"uniform mat4 uMVPMatrix;" +
			"attribute vec4 vPosition;" +
			"attribute vec4 a_Color;" +
			"varying vec4 v_Color;" +
			"void main() {" +
			"	gl_Position = uMVPMatrix*vPosition;" +
			"	v_Color = a_Color;" +
			"}";
	
	public static final String fs_Gradient = 
			"precision mediump float;" +
			"varying vec4 v_Color;" +
			"void main() {" +
			"	gl_FragColor = v_Color;" +
			"{";
	
	/*COLORED TEXTURES SHADER
     * Allows for a tint over the texture
     */
    public static final String vs_ColoredTexture =
  		  "uniform mat4 uMVPMatrix;" +
  		  "attribute vec4 vPosition;" +
  		  "attribute vec4 a_Color;" +
  		  "attribute vec2 a_texCoord;" +
  		  "varying vec4 v_Color;" + 
  		  "varying vec2 v_texCoord;" +
  		  "void main() {" +
  		  "  gl_Position = uMVPMatrix * vPosition;" +
  		  "  v_texCoord = a_texCoord;" +
  		  "  v_Color = a_Color;" + 
  		  "}";
	
  public static final String fs_ColoredTexture =
  		  "precision mediump float;" +
  		  "varying vec2 v_texCoord;" +
  		  "varying vec4 v_Color;" + 
  		  "uniform sampler2D s_texture;" +
  		  "void main() {" +
  		  "  gl_FragColor = texture2D( s_texture, v_texCoord ) * v_Color;" +
  		  "  gl_FragColor.rgb *= v_Color.a;" +
  		  "}";  
	
	/*TEXTURE SHADER
	 * This shader is for attaching a texture onto a primitive object
	 */
	
	//OpenGL ES graphics code for rendering the vertices of a shape
    public static final String vs_Texture =
    	    "uniform mat4 uMVPMatrix;" +
    	    "attribute vec4 vPosition;" +
    	    "attribute vec2 a_texCoord;" +
    	    "varying vec2 v_texCoord;" +
    	    "void main() {" +
    	    "  gl_Position = uMVPMatrix * vPosition;" +
    	    "  v_texCoord = a_texCoord;" +
    	    "}";
     
  //OpenGL ES code for rendering the face of a shape with colors or textures
    public static final String fs_Texture =
    	    "precision mediump float;" +
    	    "varying vec2 v_texCoord;" +
    	    "uniform sampler2D s_texture;" +
    	    "void main() {" +
    	    "  gl_FragColor = texture2D( s_texture, v_texCoord );" +
    	    "}";
    
    //Allows for stenciling of certain pixels from fragment
    //if alpha value is 0-discards
    public static final String fs_StencilShader = 
			"precision mediump float;" +
		    "varying vec2 v_texCoord;" +
		    "uniform sampler2D s_texture;" +
		    //"varying vec4 value;" +
		    "void main() {" +
		    "   vec4 value = texture2D( s_texture, v_texCoord );" +
		    "	if (value.r == 0.0 && value.g == 0.0 && value.b == 0.0) {" +
		    "		discard;" +
		    "	}" +
		    "	gl_FragColor = value;" +
		    "}";

	/**
	 * Utility method for compiling a OpenGL shader.
	 *
	 * <p><strong>Note:</strong> When developing shaders, use the checkGlError()
	 * method to debug shader coding errors.</p>
	 *
	 * @param type - Vertex or fragment shader type.
	 * @param shaderCode - String containing the shader code.
	 * @return - Returns an id for the shader.
	 */
	public static int loadShader (int type, String shaderCode) {
		//Creates a vertex shader type or fragment shader type
		//Type is either GLES20.GL_VERTEX_SHADER or GLES20.GL_FRAGMENT_SHADER
		int shader = GLES20.glCreateShader(type);
		
		//add the source code to the shader, compile
		GLES20.glShaderSource(shader, shaderCode);
		GLES20.glCompileShader(shader);
		
		//return shader
		return shader;
	}
	
	
	
}