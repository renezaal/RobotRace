
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
        float footX = (left ? -0.6f : 0.6f)+x;
        float footY = (front ? 1f : -1f)+y;
        float footZ = (-dGround)+z;

        // joint start and end points
        cd.Joint(x, y, z, footX, footY, footZ, 
                // length of the first and second limbs
                1.3f, 2.3f, 
                // direction of the knee
                Vector.Z, 
                // shapes of the first limb, knee, second limb
                CartesianDraw.Shape.Cylinder, CartesianDraw.Shape.Sphere, CartesianDraw.Shape.Cone, 
                // radius and detail level
                0.14f, 10);
        
        gl.glPopMatrix();
    }
}
