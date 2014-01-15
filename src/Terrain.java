
import com.jogamp.opengl.util.gl2.GLUT;
import java.awt.Color;
import javax.media.opengl.GL2;
import static javax.media.opengl.GL2.*;
import javax.media.opengl.glu.GLU;
import robotrace.Vector;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Administrator
 */
public class Terrain {

    public Terrain(RobotRace rr) {
        this.rr = rr;
    }
    RobotRace rr;
    private GL2 gl;
    private GLU glu;
    private GLUT glut;

    private void pre() {
        gl = rr.getGL();
        glu = rr.getGLU();
        glut = rr.getGLUT();
    }

    private int displayListIndex = 1;
    private double size = 75.0;
    private double waterHeight = 0;
    private boolean prepared = false;
    private int OneDColorId;
    private double gridSize = 0.5;

    private void color(double height) {
        if (height > 1.5) {
            height = 1.5;
        } else if (height < -0.5) {
            height = -0.5;
        }
        height += 0.5;
        height /= 2.0;
        gl.glTexCoord1d(height);

    }

    private void prepare() {
        pre();

        Color[] colors = new Color[5];
        colors[0] = new Color(122, 184, 217);
        colors[1] = new Color(232, 203, 126);
        colors[2] = new Color(135, 191, 118);
        colors[3] = new Color(102, 131, 92);
        colors[4] = new Color(70, 79, 68);
        OneDColorId = OneDTextureInit.create1DTexture(gl, colors);

        displayListIndex = gl.glGenLists(1);
        gl.glNewList(displayListIndex, GL_COMPILE);
        RobotRace.Material.NONE.use(gl);
        gl.glEnable(GL_TEXTURE_1D);
        for (double x = -size; x < size; x += gridSize) {

            for (double y = -size; y < size; y += gridSize) {
                double llH = heightAt(x, y);
                double lrH = heightAt(x + gridSize, y);
                double ulH = heightAt(x, y + gridSize);
                double urH = heightAt(x + gridSize, y + gridSize);

                Vector v1 = new Vector(gridSize, gridSize, urH - llH);
                Vector v2 = new Vector(gridSize, 0, lrH - llH);
                Vector v3 = new Vector(0, gridSize, ulH - llH);
                Vector n1 = normal(v1, v2);
                Vector n2 = normal(v1,v3);
                
                gl.glBindTexture(GL_TEXTURE_1D, OneDColorId);
                gl.glBegin(GL_TRIANGLES);

                gl.glNormal3d(n1.x(), n1.y(), n1.z());
                
                color(llH);
                gl.glVertex3d(x, y, llH);
                
                color(lrH);
                gl.glVertex3d(x+gridSize, y, lrH);
                
                color(urH);
                gl.glVertex3d(x+gridSize, y+gridSize, urH);
                
                gl.glEnd();
                
                
                gl.glBindTexture(GL_TEXTURE_1D, OneDColorId);
                gl.glBegin(GL_TRIANGLES);
                
                gl.glNormal3d(n2.x(), n2.y(), n2.z());
                
                color(llH);
                gl.glVertex3d(x, y, llH);
                
                color(ulH);
                gl.glVertex3d(x, y+gridSize, ulH);
                
                color(urH);
                gl.glVertex3d(x+gridSize, y+gridSize, urH);
                
                gl.glEnd();

            }

        }
        
        gl.glDisable(GL_TEXTURE_1D);
        
        gl.glBegin(GL_QUADS);
        RobotRace.Material.WATER.use(gl);
        gl.glVertex3d(-size, -size, 0);
        gl.glVertex3d(size, -size, 0);
        gl.glVertex3d(size, size, 0);
        gl.glVertex3d(-size, size, 0);
        gl.glEnd();
        
        gl.glEndList();

        prepared = true;
    }

    public void Draw() {
        if (!prepared) {
            prepare();
        }
        pre();

        gl.glCallList(displayListIndex);
    }

    public double heightAt(double x, double y) {
        return (0.6 * Math.cos(0.3 * x + 0.2 * y) + 0.4 * Math.cos(x - 0.5 * y));
    }

    private Vector normal(Vector v1, Vector v2) {
        Vector normal = v1.cross(v2);
        if (normal.z() < 0) {
            normal = normal.scale(-1);
        }
        return normal.normalized();
    }
}
