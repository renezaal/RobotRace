
import java.awt.Color;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import javax.media.opengl.GL2;
import static javax.media.opengl.GL.*;
import static javax.media.opengl.GL2GL3.GL_TEXTURE_1D;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Administrator
 */
public class OneDTextureInit {
    /**
* Creates a new 1D - texture.
* @param gl
* @param colors
* @return the texture ID for the generated texture.
*/
public static int create1DTexture(GL2 gl, Color[] colors){
gl.glDisable(GL_TEXTURE_2D);
gl.glEnable(GL_TEXTURE_1D);
int[] texid = new int[]{-1};
gl.glGenTextures(1, texid, 0);
ByteBuffer bb = ByteBuffer.allocateDirect(colors.length * 4).order(ByteOrder.nativeOrder());
for (Color color : colors) {
   int pixel = color.getRGB();
   bb.put((byte) ((pixel >> 16) & 0xFF)); // Red component
   bb.put((byte) ((pixel >> 8) & 0xFF));  // Green component
   bb.put((byte) (pixel & 0xFF));         // Blue component
   bb.put((byte) ((pixel >> 24) & 0xFF)); // Alpha component
}
bb.flip();
gl.glBindTexture(GL_TEXTURE_1D, texid[0]);
gl.glTexImage1D(GL_TEXTURE_1D, 0, GL_RGBA8, colors.length, 0, GL_RGBA, GL_UNSIGNED_BYTE, bb);
gl.glTexParameteri(GL_TEXTURE_1D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
gl.glTexParameteri(GL_TEXTURE_1D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
gl.glBindTexture(GL_TEXTURE_1D, 0);
return texid[0];
}
}
