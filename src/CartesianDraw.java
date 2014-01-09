
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

    // updates the pointers to the jogl libraries
    private void pre() {
        gl = rr.getGL();
        glu = rr.getGLU();
        glut = rr.getGLUT();
    }
    
    // alternate parameterization of the Orb method
    public void Orb(Vector v, float radius, int detail) {
        Orb((float)v.x(), (float)v.y(), (float)v.z(), radius, detail);
    }

    // alternate parameterization of the Orb method
    public void Orb(float x1, float y1, float z1, float radius, int detail) {
        Orb(x1, y1, z1, radius, radius, radius, detail);
    }

    // draws an orb
    public void Orb(float x1, float y1, float z1, float width, float thickness, float length, int detail) {
        pre();
        gl.glPushMatrix();

        gl.glTranslatef(x1, y1, z1);
        gl.glScalef(width, length, thickness);
        glut.glutSolidSphere(1f, detail, detail / 2);

        gl.glPopMatrix();
    }

    
    // alternate parameterization of the Orb method
    public void Orb(float x1, float y1, float z1, float x2, float y2, float z2, float radius, int detail) {
        pre();
        Orb(x1, y1, z1, x2, y2, z2, radius, radius, detail);
    }

    // draws an orb
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

    
    // alternate parameterization of the Rectangle method
    public void Rectangle(float x2, float y2, float z2, float radius) {
        Rectangle(0, 0, 0, x2, y2, z2, radius);
    }

    // draws a rectangle from the first coordinate to the second
    public void Rectangle(float x1, float y1, float z1, float x2, float y2, float z2, float radius) {
        pre();
        gl.glPushMatrix();
        // translate to the halfway point. A semantic nessesary due to the way a cube is drawn using glut
        gl.glTranslatef((x2 - x1) / 2, (y2 - y1) / 2, (z2 - z1) / 2);
        // use the transform method to ease troubles
        Transform(x1, y1, z1, x2, y2, z2, radius);
        // draw the cube
        glut.glutSolidCube(1f);
        gl.glPopMatrix();
    }

    // function to transform the current transformation matrix
    public void Transform(Vector v) {
        Transform(Vector.O, v, (float) v.length());
    }

    // function to transform the current transformation matrix
    public void Transform(Vector v, float radius) {
        Transform(Vector.O, v, radius);
    }

    // function to transform the current transformation matrix
    public void Transform(float x2, float y2, float z2, float radius) {
        Transform(0, 0, 0, x2, y2, z2, radius);
    }

    // function to transform the current transformation matrix
    public void Transform(Vector v1, Vector v2, float radius) {
        Transform((float) v1.x(), (float) v1.y(), (float) v1.z(), (float) v2.x(), (float) v2.y(), (float) v2.z(), radius);
    }

    // function to transform the current transformation matrix
    public void Transform(float x1, float y1, float z1, float x2, float y2, float z2, float radius) {
        pre();

        // first translate to the first coordinate which we will treat as the origin
        gl.glTranslatef(x1, y1, z1);

        // calculate position of the target vector in respect to our newly found origin
        float x3 = x2 - x1;
        float y3 = y2 - y1;
        float z3 = z2 - z1;

        // get the length
        float length = (float) Math.sqrt(x3 * x3 + y3 * y3 + z3 * z3);

        // if length is zero there's no need to continue calculations
        // this may be quite drastic, but otherwise I have to make assumptions about what the caller wants
        if (length == 0) {
            gl.glScalef(0, 0, 0);
            return;
        }

        // calculate the phi and theta, here we essentially turn the cartesian coordinates into polar ones
        float phi = (float) Math.toDegrees(Math.atan2(y3, x3));
        float theta = (float) Math.toDegrees(Math.acos(z3 / length));

        // we point the top towards the target and the bottom towards the origin
        gl.glRotatef(phi, 0, 0, 1f);
        gl.glRotatef(theta, 0, 1f, 0);

        // then we scale according to the given parameters
        gl.glScalef(radius, radius, length);
    }

    // alternate parameterization of the Joint method
    public void Joint(Vector p1, Vector p2, float firstLimbLength, float secondLimbLength, Vector kneeDirection, float radius, int detail) {
        Joint(p1, p2, firstLimbLength, secondLimbLength, kneeDirection, Shape.Cylinder, Shape.Sphere, Shape.Cone, radius, detail);
    }

    // alternate parameterization of the Joint method
    public void Joint(Vector p1, float firstLimbLength, float secondLimbLength, Vector kneeDirection, Shape firstLimbShape, Shape jointShape, Shape secondLimbShape, float radius, int detail) {
        Joint(new Vector(0, 0, 0), p1, firstLimbLength, secondLimbLength, kneeDirection, firstLimbShape, jointShape, secondLimbShape, radius, detail);
    }

    // alternate parameterization of the Joint method
    public void Joint(Vector p1, Vector p2, float firstLimbLength, float secondLimbLength, Vector kneeDirection, Shape firstLimbShape, Shape jointShape, Shape secondLimbShape, float radius, int detail) {

        Joint((float) p1.x(), (float) p1.y(), (float) p1.z(),
                (float) p2.x(), (float) p2.y(), (float) p2.z(),
                firstLimbLength, secondLimbLength, kneeDirection,
                firstLimbShape, jointShape, secondLimbShape,
                radius, detail);

    }

    // alternate parameterization of the Joint method
    public void Joint(float x1, float y1, float z1, float firstLimbLength, float secondLimbLength, Vector kneeDirection, Shape firstLimbShape, Shape kneeShape, Shape secondLimbShape, float radius, int detail) {
        Joint(0, 0, 0, x1, y1, z1, firstLimbLength, secondLimbLength, kneeDirection, firstLimbShape, kneeShape, secondLimbShape, radius, detail);
    }

    /*
     This method calculates and draws two limbs and a joint based on:
     the starting point of the first limb,
     the ending point of the second limb,
     the length of the first limb,
     the length of the second limb
     and the direction of the joint
    
    this method saves a lot of time if you wish to animate appendages with joints.
     */
    public void Joint(float x1, float y1, float z1, float x2, float y2, float z2, float firstLimbLength, float secondLimbLength, Vector jointDirection, Shape firstLimbShape, Shape jointShape, Shape secondLimbShape, float radius, int detail) {
        pre();
        gl.glPushMatrix();
        float x3 = x2 - x1;
        float y3 = y2 - y1;
        float z3 = z2 - z1;

        // the distance between the start and the end
        float separation = (float) Math.sqrt(x3 * x3 + y3 * y3 + z3 * z3);
        
        // if the distance is larger than the combined length of the limbs, we assume the combined length as the distance and just draw a straight appendage
        Boolean separationTooLarge = separation >= (firstLimbLength + secondLimbLength);

        if (separationTooLarge) {
            DrawShape(x1, y1, z1, x1 + (x3 * firstLimbLength / separation), y1 + (y3 * firstLimbLength / separation), z1 + (z3 * firstLimbLength / separation), radius, firstLimbShape, 10);
            DrawShape(x1 + (x3 * firstLimbLength / separation), y1 + (y3 * firstLimbLength / separation), z1 + (z3 * firstLimbLength / separation), x2, y2, z2, radius, firstLimbShape, 10);
            Orb(x1 + (x3 * firstLimbLength / separation), y1 + (y3 * firstLimbLength / separation), z1 + (z3 * firstLimbLength / separation), radius, 10);
            gl.glPopMatrix();
            return;
        }

        // law of the cosines
        float temp1 = firstLimbLength * firstLimbLength + separation * separation - secondLimbLength * secondLimbLength;
        float temp2 = ((float) 2) * firstLimbLength * separation;
        // this is the angle between the vector from the start to the knee and the vector from the start to the end
        float alpha = (float) Math.toDegrees(Math.acos(temp1 / temp2));

        // normalize the vector of the direction of the joint to avert trouble
        Vector directionalUnitVector = jointDirection.normalized();

        // define the coordinates as vectors
        Vector start = new Vector(x1, y1, z1);
        Vector end = new Vector(x2, y2, z2);
        Vector startToEnd = new Vector(x3, y3, z3);

        // this scalar signifies the distance the end must move in the joint direction to create a vector orthogonal to the joint direction, this is needed for further calculations
        double importantScalar = (startToEnd.dot(directionalUnitVector) / (-directionalUnitVector.dot(directionalUnitVector)));

        // we now have a point
        Vector endProjection = end.add(directionalUnitVector.scale(importantScalar));

        // the vector we get now is orthogonal to the directional vector, we normalise it for ease of use
        Vector orthogonal = endProjection.subtract(start).normalized();

        // the angle between the vector in the direction of the knee and the vector from the start to the end
        double beta = Math.toDegrees(Math.acos(directionalUnitVector.dot(startToEnd.normalized())));
        
        // if we substract 90 degrees from beta the result is the angle between the vector orthogonal to the knee direction and the vector from the start to the end
        // if we then substract the resulting angle from the angle between the first limb and the vector from start to end, we get the angle of the first limb to our "x-axis"
        double gamma = alpha - (beta - 90d);

        // define the point of the joint
        Vector joint = start.add(directionalUnitVector.scale(Math.sin(Math.toRadians(gamma)) * firstLimbLength).add(orthogonal.scale(Math.cos(Math.toRadians(gamma)) * firstLimbLength)));

        // convert the vector to seperate values for ease of use and readability
        float jointX = (float) joint.x();
        float jointY = (float) joint.y();
        float jointZ = (float) joint.z();

        // draw the actual limbs and joint
        DrawShape(x1, y1, z1, jointX, jointY, jointZ, radius, firstLimbShape, detail);
        DrawShape(jointX, jointY, jointZ, x2, y2, z2, radius, secondLimbShape, detail);
        DrawShape(jointX, jointY, jointZ - radius, jointX, jointY, jointZ + radius, radius * 2f, jointShape, detail);

        gl.glPopMatrix();
    }

    public void DrawShape(float x1, float y1, float z1, float radius, Shape shape, int detail) {
        DrawShape(0, 0, 0, x1, y1, z1, radius, shape, detail);
    }

    public void DrawShape(float x1, float y1, float z1, float x2, float y2, float z2, float radius, Shape shape, int detail) {
        pre();

        gl.glPushMatrix();
        if ((shape != Shape.Cone) && (shape != Shape.Cylinder)) {
            gl.glTranslatef((x2 - x1) / 2, (y2 - y1) / 2, (z2 - z1) / 2);
        }
        Transform(x1, y1, z1, x2, y2, z2, radius);
        drawShape(shape, detail);
        gl.glPopMatrix();
    }

    private void drawShape(Shape shape, int detail) {
        switch (shape) {
            case Cone:
                glut.glutSolidCone(0.5f, 1f, detail, detail);
                break;
            case Cylinder:
                glut.glutSolidCylinder(0.5f, 1f, detail, detail / 5);
                break;
            case Sphere:
                glut.glutSolidSphere(0.5f, detail, detail / 2);
                break;
            case Rectangle:
                glut.glutSolidCube(1f);
                break;
        }
    }
    
    private void cube(){
        gl.glBegin(GL_QUADS);
        
        
        
        gl.glEnd();
    }

    public enum Shape {

        Sphere(), Rectangle(), Cone(), Cylinder();

        private Shape() {
        }
    }
}
