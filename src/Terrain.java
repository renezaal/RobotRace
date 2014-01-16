
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
        perlin = new PerlinNoise(897964513, 4, 6.0);
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

    private int displayListIndex = -1;
    private double size = 25.0;
    private double waterHeight = 0;
    private boolean prepared = false;
    private int OneDColorId;
    private double gridSize = 0.5;
    private PerlinNoise perlin;

    private void color(double height) {
        double max = (((double)colors.length)/2.0)-0.5;
        if (height > max) {
            height = max;
        } else if (height < -0.5) {
            height = -0.5;
        }
        height += 0.5;
        height /= max+0.5;
        gl.glTexCoord1d(height);

    }

    private Color[] colors
            = {
                new Color(122, 184, 217),
                new Color(232, 203, 126),
                new Color(135, 191, 118),
                new Color(135, 191, 118),
                new Color(135, 191, 118),
                new Color(102, 131, 92),
                new Color(102, 131, 92),
                new Color(70, 79, 68)};

    private double minMaxHeightCorrection(double h, double x, double y) {
        RobotRace.RaceTrack rt = rr.getTrack();
        double trackDist = rt.getClosestPoint(new Vector(x, y, 0)).subtract(new Vector(x, y, 0)).length();
        double maxHeight = trackDist / 4;
        double minHeight = -trackDist / 4;
        if (h > maxHeight) {
            return maxHeight;
        } else if (h < minHeight) {
            return minHeight;
        }
        return h;
    }

    private void prepare() {
        pre();
        perlin = new PerlinNoise((int) (Math.random() * 10000.0), 4, 6.0);

        OneDColorId = OneDTextureInit.create1DTexture(gl, colors);

        if (displayListIndex < 0) {
            displayListIndex = gl.glGenLists(1);
        }
        if (gl.glIsList(displayListIndex)) {
            gl.glDeleteLists(displayListIndex, 1);
        }
        gl.glNewList(displayListIndex, GL_COMPILE);
        RobotRace.Material.NONE.use(gl);
        gl.glEnable(GL_TEXTURE_1D);
        for (double x = -size; x < size; x += gridSize) {

            for (double y = -size; y < size; y += gridSize) {
                if (new Vector(x,y,0).length()>size) {
                    continue;
                }
                double llH = heightAt(x, y);
                double lrH = heightAt(x + gridSize, y);
                double ulH = heightAt(x, y + gridSize);
                double urH = heightAt(x + gridSize, y + gridSize);

                Vector v1 = new Vector(gridSize, gridSize, urH - llH);
                Vector v2 = new Vector(gridSize, 0, lrH - llH);
                Vector v3 = new Vector(0, gridSize, ulH - llH);
                Vector n1 = normal(v1, v2);
                Vector n2 = normal(v1, v3);

                gl.glBindTexture(GL_TEXTURE_1D, OneDColorId);
                gl.glBegin(GL_TRIANGLES);

                gl.glNormal3d(n1.x(), n1.y(), n1.z());

                color(llH);
                gl.glVertex3d(x, y, llH);

                color(lrH);
                gl.glVertex3d(x + gridSize, y, lrH);

                color(urH);
                gl.glVertex3d(x + gridSize, y + gridSize, urH);

                gl.glEnd();

                gl.glBindTexture(GL_TEXTURE_1D, OneDColorId);
                gl.glBegin(GL_TRIANGLES);

                gl.glNormal3d(n2.x(), n2.y(), n2.z());

                color(llH);
                gl.glVertex3d(x, y, llH);

                color(ulH);
                gl.glVertex3d(x, y + gridSize, ulH);

                color(urH);
                gl.glVertex3d(x + gridSize, y + gridSize, urH);

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

    public void ReDraw(double size) {
        this.size=size;
        prepare();
    }

    public void Draw() {
        if (!prepared) {
            prepare();
        }
        pre();

        gl.glCallList(displayListIndex);
    }
    
    public double getSize(){
        return size;
    }

    public double heightAt(double x, double y) {
        double h = perlin.noise2d(x, y) * 5.0;
        return minMaxHeightCorrection(h, x, y);
    }

    private Vector normal(Vector v1, Vector v2) {
        Vector normal = v1.cross(v2);
        if (normal.z() < 0) {
            normal = normal.scale(-1);
        }
        return normal.normalized();
    }
}
