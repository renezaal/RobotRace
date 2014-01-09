
import com.jogamp.opengl.util.gl2.GLUT;
import java.util.Date;
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
    private float stepPos = (float) (0.3 - (Math.random() * 0.6));
    private float stepStart = -0.3f;
    private float stepEnd = 0.3f;
    private boolean onTheGround = Math.random() < 0.5;
    private Date time = new Date();
    // the neutral point is some sort of ideal point for the foot to stan in
    private Vector neutral;
    private int relaxTimer=0;
    private double maxDistance=0.3;

    private float stepLength() {
        return stepEnd - stepStart;
    }

    private void pre() {
        gl = rr.getGL();
        glu = rr.getGLU();
        glut = rr.getGLUT();
    }

    public void Advance(float dGround, Vector newNeutral) {
        Vector foot;
        Date newTime = new Date();
        double timeLapsed = ((double) (newTime.getTime() - time.getTime())) * 0.01;
        time = newTime;
        
        // regulate the loop
        if (onTheGround) {
            double distance = neutral.subtract(newNeutral).length();
            if (distance>maxDistance) {
                onTheGround = false;
            }
        } else {
            stepPos += timeLapsed;
            if (stepPos > stepEnd) {
                stepStart = (float) -((Math.random() * 1d) + 0.3);
                onTheGround = true;
                stepPos = stepEnd;
            }
        }

        // give the coordinates to the foot
        if (onTheGround) {

            double footy = stepPos;
            foot = new Vector(0, footy, 0);

        } else {

            double footy = stepPos;

            double footz;
            if (stepLength() == 0) {
                footz = 0;
            } else {
                footz = ((-Math.pow((2 * (stepPos - stepStart) / stepLength()) - 1, 2)) + 1) * 0.3;
            }
            foot = new Vector(0, footy, footz);

        }

        // draw the entire leg
        draw(dGround, foot);
    }

    private void draw(float dGround, Vector foot) {
        pre();

        Vector neutral = new Vector(0.6, front ? 1 : -1, -dGround);
        foot = foot.add(neutral);

        float footX = (float) (left ? -foot.x() : foot.x()) + x;
        float footY = (float) foot.y() + y;
        float footZ = (float) (foot.z()) + z;

        gl.glPushMatrix();
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

    public void Draw(float dGround) {
       // Advance(dGround, 0.1);
    }
}
