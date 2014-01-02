
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

    public void Joint(float x1, float y1, float z1, float firstLimbLength, float secondLimbLength, Vector kneeDirection, Shape firstLimbShape, Shape kneeShape, Shape secondLimbShape, float radius, int detail) {
        Joint(0, 0, 0, x1, y1, z1, firstLimbLength, secondLimbLength, kneeDirection, firstLimbShape, kneeShape, secondLimbShape, radius, detail);
    }

    public void Joint(float x1, float y1, float z1, float x2, float y2, float z2, float firstLimbLength, float secondLimbLength, Vector kneeDirection, Shape firstLimbShape, Shape jointShape, Shape secondLimbShape, float radius, int detail) {
        pre();
        gl.glPushMatrix();
        float x3 = x2 - x1;
        float y3 = y2 - y1;
        float z3 = z2 - z1;

        float separation = (float) Math.sqrt(x3 * x3 + y3 * y3 + z3 * z3);
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
        float beta = (float) Math.toDegrees(Math.acos(temp1 / temp2));

        Vector directionalUnitVector = kneeDirection.normalized();

        Vector start = new Vector(x1, y1, z1);
        Vector end = new Vector(x2, y2, z2);
        Vector startToEnd = new Vector(x3, y3, z3);

        // this scalar signifies the distance the end must move in the knee direction to create a vector orthogonal to the knee direction, this is needed for further calculations
        double importantScalar = (startToEnd.dot(directionalUnitVector) / (-directionalUnitVector.dot(directionalUnitVector)));

        // we now have a point
        Vector endProjection = end.add(directionalUnitVector.scale(importantScalar));

        // the vector we get now is orthogonal to the directional vector, we normalise it for ease of use
        Vector orthogonal = endProjection.subtract(start).normalized();

        double alpha = Math.toDegrees(Math.acos(directionalUnitVector.dot(startToEnd.normalized())));
        double theta = beta - (alpha - 90d);

        // define the point of the knee
        Vector joint = start.add(directionalUnitVector.scale(Math.sin(Math.toRadians(theta)) * firstLimbLength).add(orthogonal.scale(Math.cos(Math.toRadians(theta)) * firstLimbLength)));

        float jointX = (float) joint.x();
        float jointY = (float) joint.y();
        float jointZ = (float) joint.z();

        // draw the actual limbs and joint/knee
        DrawShape(x1, y1, z1, jointX, jointY, jointZ, radius, firstLimbShape, detail);
        DrawShape(jointX, jointY, jointZ, x2, y2, z2, radius, secondLimbShape, detail);
        DrawShape(jointX, jointY, jointZ - radius, jointX, jointY, jointZ +  radius, radius * 2f, jointShape, detail);

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

    public enum Shape {

        Sphere(), Rectangle(), Cone(), Cylinder();

        private Shape() {
        }
    }
}
