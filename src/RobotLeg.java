
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

    public RobotLeg(RobotRace rr, CartesianDraw cd, Vector f, boolean right, boolean front) {
        this.rr = rr;
        this.cd = cd;
        this.foot = f;
        this.footProjection = f;
        this.neutral = f;
        this.rightLeg = right;
        this.front = front;
    }

    private RobotRace rr;
    private CartesianDraw cd;
    private GL2 gl;
    private GLU glu;
    private GLUT glut;
    private boolean onTheGround = true;
    // the neutral point is some sort of ideal point for the foot to stan in
    private Vector neutral;
    private double relaxTimer = 0;
    private double maxRelaxTime = 2.0;
    private double maxDistance = 0.4;
    private Vector stepTarget = Vector.O;
    private Vector footProjection;
    private Vector foot;
    private Vector stepStart;
    private double stepStartTime = 0;
    private boolean rightLeg;
    private boolean front;
    private double upperLegLength = 1.3;
    private double lowerLegLength = 2.3;

    private void pre() {
        gl = rr.getGL();
        glu = rr.getGLU();
        glut = rr.getGLUT();
    }

    public boolean isRight() {
        return rightLeg;
    }

    public boolean isFront() {
        return front;
    }

    private void setStep(Vector target) {

        // we're leaving ground
        onTheGround = false;
        // the target of the step is located in the new neutral zone
        stepTarget = target;
        stepStart = footProjection;
        // the maximum distance is randomized a bit
        maxDistance = (Math.random() * 0.5) + 0.5;
        // the starting point is where the foot is now. 
        stepStart = footProjection;
        // we just started moving
        stepStartTime = 0;
    }

    private double stepLengthMem;
    private double timeMem;

    private double stepLength(double time) {
        if (time == timeMem) {
            return stepLengthMem;
        }
        // calculate the total length of this step
        double stepLength = stepStart.subtract(stepTarget).length();
        if (stepLength < 0.001) {
            stepLength = 0;
        }

        return stepLength;
    }

    public void Advance(Vector newNeutral, Vector attachment) {
        if (Double.isNaN(foot.x()) || Double.isNaN(foot.y()) || Double.isNaN(foot.z())) {
            foot = newNeutral;
            onTheGround=true;
            footProjection=newNeutral;
            neutral=newNeutral;
        }
        // get the time since the last render from the robotrace class
        double time = rr.getTime();
        // regulate the loop
        if (onTheGround) {
            // keep track of the amount of time this foot has stood still
            relaxTimer += rr.getTime();
            // how far is the foot from the new point?
            double distance = footProjection.subtract(newNeutral).length();

            if (distance > maxDistance) {
                // if it's farther away than the allowed distance, it has got to move towards the new point.
                // for that it has to leave the ground

                setStep(newNeutral.add(newNeutral.subtract(new Vector(footProjection.x(), footProjection.y(), 0)).normalized().scale(maxDistance / 2.0)));
            } else {
                // this is a restless robot
                if (relaxTimer > maxRelaxTime) {
                    // we'll give a little randomness to the time it takes to relax
                    maxRelaxTime = (Math.random() * 2.0) + 1.0;

                    setStep(newNeutral);
                }
            }
        } else {
            // if we're moving, we're not relaxing
            relaxTimer = 0;

            // if the foot is near the target or too low, we'll assume it's made it to teh target
            if ((stepLength(time) == 0 && stepStartTime > 0.5) || (stepLength(time) > 0 && stepStart.subtract(footProjection).length() > stepStart.subtract(stepTarget).length())) {
                // and reset it's height to be sure
                footProjection = new Vector(footProjection.x(), footProjection.y(), newNeutral.z());
                // so it's on the ground now
                onTheGround = true;
            }
        }

        // give the coordinates to the foot
        if (!onTheGround) {
            relaxTimer = 0;
            // calculate the direction in which the foot should move, do not bother with the z value
            Vector stepDirection = stepTarget.subtract(footProjection).normalized();
            // move the foot toward the target
            footProjection = footProjection.add(stepDirection.scale(time * time + (4.0 * stepLength(time) * time)));
// calculate the distance the foot has moved since the start of the step. again, don't bother with the z value
            double distanceMoved = stepStart.subtract(footProjection).length();
            // instantiate the variable that is going to contain the z value of the foot
            double footZ;

            if (stepLength(time) == 0) {
                stepStartTime += time;
                footZ = (1.0 - Math.pow((stepStartTime / 0.25) - 1.0, 2)) * 0.3;
            } else if (distanceMoved > stepLength(time)) {
                // if we've moved beyond our target we don't modify the height
                footZ = 0;
            } else {
                footZ = (1.0 - Math.pow(((distanceMoved / stepLength(time)) * 2) - 1.0, 2)) * 0.3;
            }
            foot = footProjection.add(Vector.Z.scale(footZ));
        }

        // draw the entire leg
        draw(foot, attachment);

        neutral = newNeutral;
    }

    private void draw(Vector foot, Vector attachment) {
        pre();
        gl.glPushMatrix();
        // joint start and end points
        cd.Joint(attachment, foot,
                // length of the first and second limbs
                (float) upperLegLength, (float) lowerLegLength,
                // direction of the knee
                Vector.Z,
                // shapes of the first limb, knee, second limb
                CartesianDraw.Shape.Cylinder, CartesianDraw.Shape.Sphere, CartesianDraw.Shape.Cone,
                // radius and detail level
                0.14f, 10);

        gl.glPopMatrix();
    }
}
