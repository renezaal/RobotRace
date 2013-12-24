
import com.jogamp.opengl.util.gl2.GLUT;
import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import static javax.media.opengl.GL2.*;
import javax.media.opengl.glu.GLU;
import robotrace.Base;
import robotrace.Vector;

/**
 *
 * @author rene
 */
public class CartesianDraw {

    public CartesianDraw(RobotRace rr) {
        this.rr = rr;
    }

    private GL2 gl;
    private GLU glu;
    private GLUT glut;
    private RobotRace rr;

    private void pre() {
        gl = rr.getGL();
        glu = rr.getGLU();
        glut = rr.getGLUT();
    }

    public enum Shape {

        Cone {
                },
        Square {
                },
        Orb {
                },
        Triangle {
                };

        private Shape() {
        }
    }

    public void Orb(float x1, float y1, float z1, float radius, int detail) {
        pre();
        Orb(x1, y1, z1, radius, radius, radius, detail);
    }

    public void Orb(float x1, float y1, float z1, float width, float thickness, float length, int detail) {
        pre();
        gl.glPushMatrix();

        gl.glTranslatef(x1, y1, z1);
        gl.glScalef(width, length, thickness);
        glut.glutSolidSphere(1f, detail, detail / 2);

        gl.glPopMatrix();
    }

    public void Orb(float x1, float y1, float z1, float x2, float y2, float z2, float radius, int detail) {
        pre();
        Orb(x1, y1, z1, x2, y2, z2, radius, radius, detail);
    }

    public void Orb(float x1, float y1, float z1, float x2, float y2, float z2, float width, float thickness, int detail) {
        pre();

        gl.glPushMatrix();

        gl.glTranslatef((x2 - x1) / 2, (y2 - y1) / 2, (z2 - z1) / 2);
        Transform(x1, y1, z1, x2, y2, z2, thickness);
        float thicknessScale = thickness / width;
        gl.glScalef(thicknessScale, 1f, 1f);
        glut.glutSolidSphere(1f, detail, detail / 2);

        gl.glPopMatrix();

    }

    public void Rectangle(float x2, float y2, float z2, float radius) {
        pre();
        Rectangle(0, 0, 0, x2, y2, z2, radius);
    }

    public void Rectangle(float x1, float y1, float z1, float x2, float y2, float z2, float radius) {
        pre();
        gl.glPushMatrix();
        gl.glTranslatef((x2 - x1) / 2, (y2 - y1) / 2, (z2 - z1) / 2);
        Transform(x1, y1, z1, x2, y2, z2, radius);
        glut.glutSolidCube(1f);
        gl.glPopMatrix();
    }

    public void Transform(float x2, float y2, float z2, float radius) {
        pre();
        Transform(0, 0, 0, x2, y2, z2, radius);
    }

    public void Transform(float x1, float y1, float z1, float x2, float y2, float z2, float radius) {
        pre();

        gl.glTranslatef(x1, y1, z1);

        float x3 = x2 - x1;
        float y3 = y2 - y1;
        float z3 = z2 - z1;

        float length = (float) Math.sqrt(x3 * x3 + y3 * y3 + z3 * z3);
        if (length == 0) {
            gl.glScalef(0, 0, 0);
            return;
        }

        float phi = (float) Math.toDegrees(Math.atan2(y3, x3));
        float theta = (float) Math.toDegrees(Math.acos(z3 / length));

        gl.glRotatef(phi, 0, 0, 1f);
        gl.glRotatef(theta, 0, 1f, 0);

        gl.glScalef(radius, radius, length);
    }
}
