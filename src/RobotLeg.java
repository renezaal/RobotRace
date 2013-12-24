
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
public class RobotLeg {

    public RobotLeg(RobotRace rr, CartesianDraw cd, Boolean front, Boolean left, float x, float y, float z) {
        this.rr = rr;
        this.cd = cd;
        this.front = front;
        this.left = left;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    private RobotRace rr;
    private CartesianDraw cd;
    private GL2 gl;
    private GLU glu;
    private GLUT glut;
    private Boolean front;
    private Boolean left;
    private float x;
    private float y;
    private float z;

    private void pre() {
        gl = rr.getGL();
        glu = rr.getGLU();
        glut = rr.getGLUT();
    }

    public void DrawLeg(float dGround) {
    pre();
    
        gl.glPushMatrix();
        gl.glTranslatef(x, y, z);
        final float kneeXOffset = 0.5f;
        final float kneeYOffset = 0.3f;
        float kneeX = left ? -kneeXOffset : kneeXOffset;
        float kneeY = front ? kneeYOffset : -kneeYOffset;
        float kneeZ = 1.2f;

        gl.glPushMatrix();
        cd.Rectangle(kneeX, kneeY, kneeZ, 0.1f);
        gl.glPopMatrix();
        //Bottom Leg
        gl.glPushMatrix();
        cd.Transform(kneeX, kneeY, kneeZ, kneeX * 2, kneeY * 2, -dGround, 0.1f);
        glut.glutSolidCone(1f, 1f, 10, 10);
        gl.glPopMatrix();

        //Upper and Bottom Leg joint
        gl.glPushMatrix();
        gl.glTranslatef(kneeX, kneeY, kneeZ * 0.95f);
        glut.glutSolidSphere(0.15f, 20, 10);
        gl.glPopMatrix();
        gl.glPopMatrix();
    }
}
