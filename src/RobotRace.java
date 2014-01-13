
import com.jogamp.opengl.util.gl2.GLUT;
import com.jogamp.opengl.util.texture.Texture;
import java.util.Date;
import javax.media.opengl.GL2;
import static javax.media.opengl.GL2.*;
import javax.media.opengl.glu.GLU;
import robotrace.Base;
import robotrace.Vector;

/**
 * Handles all of the RobotRace graphics functionality, which should be extended
 * per the assignment.
 *
 * OpenGL functionality: - Basic commands are called via the gl object; -
 * Utility commands are called via the glu and glut objects;
 *
 * GlobalState: The gs object contains the GlobalState as described in the
 * assignment: - The camera viewpoint angles, phi and theta, are changed
 * interactively by holding the left mouse button and dragging; - The camera
 * view width, vWidth, is changed interactively by holding the right mouse
 * button and dragging upwards or downwards; - The center point can be moved up
 * and down by pressing the 'q' and 'z' keys, forwards and backwards with the
 * 'w' and 's' keys, and left and right with the 'a' and 'd' keys; - Other
 * settings are changed via the menus at the top of the screen.
 *
 * Textures: Place your "track.jpg", "brick.jpg", "head.jpg", and "torso.jpg"
 * files in the same folder as this file. These will then be loaded as the
 * texture objects track, bricks, head, and torso respectively. Be aware, these
 * objects are already defined and cannot be used for other purposes. The
 * texture objects can be used as follows:
 *
 * gl.glColor3f(1f, 1f, 1f); track.bind(gl); gl.glBegin(GL_QUADS);
 * gl.glTexCoord2d(0, 0); gl.glVertex3d(0, 0, 0); gl.glTexCoord2d(1, 0);
 * gl.glVertex3d(1, 0, 0); gl.glTexCoord2d(1, 1); gl.glVertex3d(1, 1, 0);
 * gl.glTexCoord2d(0, 1); gl.glVertex3d(0, 1, 0); gl.glEnd();
 *
 * Note that it is hard or impossible to texture objects drawn with GLUT. Either
 * define the primitives of the object yourself (as seen above) or add
 * additional textured primitives to the GLUT object.
 */
public class RobotRace extends Base {

    private CartesianDraw cd = new CartesianDraw(this);

    public GL2 getGL() {
        return gl;
    }

    public GLU getGLU() {
        return glu;
    }

    public GLUT getGLUT() {
        return glut;
    }
    /**
     * Array of the four robots.
     */
    private final Robot[] robots;

    /**
     * Instance of the camera.
     */
    private final Camera camera;

    /**
     * Instance of the race track.
     */
    private final RaceTrack raceTrack;

    /**
     * Instance of the terrain.
     */
    private final Terrain terrain;

    /**
     * Constructs this robot race by initializing robots, camera, track, and
     * terrain.
     */
    public RobotRace() {

        // Create a new array of four robots
        robots = new Robot[4];

        // Initialize robot 0
        for (int i = 0; i < 4; i++) {
            robots[i] = new Robot(Material.GOLD, new Vector(0, i * 10, 0), new Vector(0, 1, 0), this);
        }

        // Initialize the camera
        camera = new Camera();

        // Initialize the race track
        raceTrack = new RaceTrack();

        // Initialize the terrain
        terrain = new Terrain();
    }

    /**
     * Called upon the start of the application. Primarily used to configure
     * OpenGL.
     */
    @Override
    public void initialize() {
        // Enable blending.
        gl.glEnable(GL_BLEND);
        gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        // Enable anti-aliasing.
        gl.glEnable(GL_LINE_SMOOTH);
        gl.glEnable(GL_POLYGON_SMOOTH);
        gl.glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);
        gl.glHint(GL_POLYGON_SMOOTH_HINT, GL_NICEST);

        // Enable depth testing.
        gl.glEnable(GL_DEPTH_TEST);
        gl.glDepthFunc(GL_LESS);

        // Enable textures. 
        gl.glEnable(GL_TEXTURE_2D);
        gl.glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);
        gl.glBindTexture(GL_TEXTURE_2D, 0);

        // Enable lighting
        gl.glEnable(GL_LIGHTING);
        gl.glEnable(GL_LIGHT0);
        gl.glEnable(GL_LIGHT1);
        gl.glEnable(GL_NORMALIZE);

        // add ambient light
        float ambientLight[] = new float[]{0.8f, 0.8f, 0.8f, 0.5f};
        gl.glLightfv(GL_LIGHT0, GL_AMBIENT, ambientLight, 0);
    }

    /**
     * Configures the viewing transform.
     */
    @Override
    public void setView() {
        // Select part of window.
        gl.glViewport(0, 0, gs.w, gs.h);

        // Set projection matrix.
        gl.glMatrixMode(GL_PROJECTION);
        gl.glLoadIdentity();

        // Set the perspective.
        // Modify this to meet the requirements in the assignment.
        // to define the perspective, we need to start with the aspect
        // the aspect is simply the width of the window versus the height
        float aspect = (float) gs.w / (float) gs.h;

        // then we need the vertical field of view.
        // to calculate the vertical field of view we first need the height of the scene
        float vHeight = gs.vWidth / aspect;

        // we can now calculate the angle, due to the way the calculations work
        // we need to take half the height first, then later on multiply by two
        // essentially we create a triangle with a 90 degree angle with the vDist 
        // and the upper half of the height
        float fovY = (float) Math.atan2((0.5f * vHeight), gs.vDist) * 2f;
        fovY = (float) Math.toDegrees(fovY);

        // now simply add our calculated values to the method call
        glu.gluPerspective(fovY, aspect, 0.1, 1000);

        // Set camera.
        gl.glMatrixMode(GL_MODELVIEW);
        gl.glLoadIdentity();

        // Update the view according to the camera mode
        camera.update(gs.camMode);
    }

    // variable for storing the last time a draw was called
    private Date time = new Date();

    // variable for storing the time between draws
    private double timePassed = 1;

// variable for keeping track of loops, is reset at 600 seconds/10 minutes
    private double loop = 0;

// gets the time between this draw and the previous one
    public double getTime() {
        return timePassed;
    }
// calculates the time that passed since the last time this method was called

    private void newTime() {
        // new time that is current
        Date newTime = new Date();

        // difference between the already existing time and the new one
        long difference = newTime.getTime() - time.getTime();

        // difference expressed as fractional seconds
        timePassed = ((double) difference) / 1000.0;

        // updates the loop
        loop = (loop + timePassed) % 600;

        // set the time to the current one
        time = newTime;
    }

    /**
     * Draws the entire scene.
     */
    @Override
    public void drawScene() {
        // update the measured time between draws
        newTime();

        // Background color.
        gl.glClearColor(1f, 1f, 1f, 0f);

        // Clear background.
        gl.glClear(GL_COLOR_BUFFER_BIT);

        // Clear depth buffer.
        gl.glClear(GL_DEPTH_BUFFER_BIT);

        // Set color to black.
        gl.glColor3f(0f, 0f, 0f);

        gl.glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);

        // enable transparency
        gl.glEnable(GL_BLEND);
        gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        // Draw the axis frame
        if (gs.showAxes) {
            drawAxisFrame();
        }

        double x = Math.sin((loop/1.0) % (2.0 * Math.PI)) * 10;
        double y = Math.cos((loop/1.0) % (2.0 * Math.PI)) * 5;

        Vector roboLoc = new Vector(x, y, 0);
        Vector heading = roboLoc.cross(Vector.Z);
//Vector roboPos = (new Vector(x,y,0)).scale(3.0);
        // Draw all four robots
        for (int i = 0; i < robots.length; i++) {
            robots[i].draw(robots[i].getLocation(), heading, false);
        }

        // Draw race track
        raceTrack.draw(gs.trackNr);

        // Draw terrain
        terrain.draw();

        // Unit box around origin.
        glut.glutWireCube(1f);

        // Move in x-direction.
        gl.glTranslatef(2f, 0f, 0f);

        // Rotate 30 degrees, around z-axis.
        gl.glRotatef(30f, 0f, 0f, 1f);

        // Scale in z-direction.
        gl.glScalef(1f, 1f, 2f);

        // Translated, rotated, scaled box.
        glut.glutWireCube(1f);
    }

    /**
     * Draws the x-axis (red), y-axis (green), z-axis (blue), and origin
     * (yellow).
     */
    public void drawAxisFrame() {
        // code goes here ...
        if (gs.showAxes) {

            // draw a red arrow along the X-axis
            gl.glMaterialfv(GL_FRONT_AND_BACK, GL_DIFFUSE, Material.RED.diffuse, 0);
            gl.glMaterialfv(GL_FRONT_AND_BACK, GL_SPECULAR, Material.RED.specular, 0);
            gl.glMaterialfv(GL_FRONT_AND_BACK, GL_SHININESS, Material.RED.shinyness, 0);
            cd.Rectangle(2f, 0, 0, 0.1f);

            // draw a cone at the end
            gl.glPushMatrix();
            cd.Transform(2f, 0f, 0f, 2.12f, 0f, 0f, 0.12f);
            glut.glutSolidCone(1f, 1.5f, 10, 10);
            gl.glPopMatrix();

            // draw a green arrow along the Y-axis
            gl.glMaterialfv(GL_FRONT_AND_BACK, GL_DIFFUSE, Material.GREEN.diffuse, 0);
            gl.glMaterialfv(GL_FRONT_AND_BACK, GL_SPECULAR, Material.GREEN.specular, 0);
            gl.glMaterialfv(GL_FRONT_AND_BACK, GL_SHININESS, Material.GREEN.shinyness, 0);
            cd.Rectangle(0, 2f, 0, 0.1f);

            // draw a cone at the end
            gl.glPushMatrix();
            cd.Transform(0f, 2f, 0f, 0f, 2.12f, 0f, 0.12f);
            glut.glutSolidCone(1f, 1.5f, 10, 10);
            gl.glPopMatrix();

            // draw a blue arrow along the Z-axis
            gl.glMaterialfv(GL_FRONT_AND_BACK, GL_DIFFUSE, Material.BLUE.diffuse, 0);
            gl.glMaterialfv(GL_FRONT_AND_BACK, GL_SPECULAR, Material.BLUE.specular, 0);
            gl.glMaterialfv(GL_FRONT_AND_BACK, GL_SHININESS, Material.BLUE.shinyness, 0);
            cd.Rectangle(0, 0, 2f, 0.1f);

            // draw a cone at the end
            gl.glPushMatrix();
            cd.Transform(0f, 0f, 2f, 0f, 0f, 2.12f, 0.12f);
            glut.glutSolidCone(1f, 1.5f, 10, 10);
            gl.glPopMatrix();

            // draw a yellow orb at the origin
            gl.glMaterialfv(GL_FRONT_AND_BACK, GL_DIFFUSE, Material.YELLOW.diffuse, 0);
            gl.glMaterialfv(GL_FRONT_AND_BACK, GL_SPECULAR, Material.YELLOW.specular, 0);
            gl.glMaterialfv(GL_FRONT_AND_BACK, GL_SHININESS, Material.YELLOW.shinyness, 0);
            glut.glutSolidSphere(0.2f, 20, 10);
        }
    }

    /**
     * Materials that can be used for the robots.
     */
    public enum Material {

        /**
         * Gold material properties.
         */
        GOLD(
                new float[]{0.75164f, 0.60648f, 0.22648f, 1f},
                new float[]{0.628281f, 0.555802f, 0.366065f, 1f},
                new float[]{51.2f}),
        /**
         * Silver material properties.
         */
        SILVER(
                new float[]{0.50754f, 0.50754f, 0.50754f, 1f},
                new float[]{0.508273f, 0.508273f, 0.508273f, 1f},
                new float[]{51.2f}),
        /**
         * Wood material properties.
         */
        WOOD(
                new float[]{0.227f, 0.13f, 0.065f, 1.0f},
                new float[]{0.3f, 0.14f, 0.071f, 1.0f},
                new float[]{2f}),
        FORCEFIELD(
                forceFieldColor(),
                forceFieldColor(),
                new float[]{0f}
        ),
        /**
         * Orange material properties.
         */
        ORANGE(
                new float[]{1f, 0.5f, 0f, 1.0f},
                new float[]{1f, 0.5f, 0f, 1.0f},
                new float[]{20f}),
        /**
         * Yellow material properties.
         */
        YELLOW(
                new float[]{0f, 1f, 1f, 1.0f},
                new float[]{0f, 1f, 1f, 1.0f},
                new float[]{20f}),
        /**
         * Blue material properties.
         */
        BLUE(
                new float[]{0f, 0f, 1f, 1.0f},
                new float[]{0f, 0f, 1f, 1.0f},
                new float[]{20f}),
        /**
         * Green material properties.
         */
        GREEN(
                new float[]{0f, 1f, 0f, 1.0f},
                new float[]{0f, 1f, 0f, 1.0f},
                new float[]{20f}),
        /* 
         *Red material properties
         */
        RED(
                new float[]{1f, 0f, 0f, 1.0f},
                new float[]{1f, 0f, 0f, 1.0f},
                new float[]{20f});

        /**
         * The diffuse RGBA reflectance of the material.
         */
        float[] diffuse;

        /**
         * The specular RGBA reflectance of the material.
         */
        float[] specular;

        // The shininess of the material in RGBA
        float[] shinyness;

        public void use(GL2 gl) {

            // set the material properties
            gl.glMaterialfv(GL_FRONT_AND_BACK, GL_DIFFUSE, diffuse, 0);
            gl.glMaterialfv(GL_FRONT_AND_BACK, GL_SPECULAR, specular, 0);
            gl.glMaterialfv(GL_FRONT_AND_BACK, GL_SHININESS, shinyness, 0);
            gl.glColor3f(0.1f, 0.1f, 0.1f);
        }

        public void update() {
            if (this == FORCEFIELD) {
                diffuse = forceFieldColor();
                specular = forceFieldColor();
            }
        }

        /**
         * Constructs a new material with diffuse and specular properties.
         */
        private Material(float[] diffuse, float[] specular, float[] shinyness) {
            this.diffuse = diffuse;
            this.specular = specular;
            this.shinyness = shinyness;
        }
    }

    static private float[] forceFieldColor() {

        double base = Math.random();
        double red = base * 0.2;
        double green = base * 0.2;
        double blue = base;
        double alpha = (Math.random() + 1) * 0.2;
        return new float[]{
            (float) red,
            (float) green,
            (float) blue,
            (float) alpha};
    }

    public Texture getBrick() {
        return brick;
    }

    /**
     * Represents a Robot, to be implemented according to the Assignments.
     */
    public class Robot {

        /**
         * The material from which this robot is built.
         */
        private final Material material;

        private Vector pos() {
            return new Vector(posX, posY, posZ);
        }

        private Vector heading() {
            return heading.normalized();
        }

        private Vector rightSide() {
            return Vector.Z.cross(heading).normalized();
        }

        private Vector normal() {
            return heading.cross(rightSide());
        }
        private float posX, posY, posZ;
        private RobotLeg[] legs = new RobotLeg[4];
        private RobotEye[] eyes = new RobotEye[2];
        private RobotArm[] arms = new RobotArm[2];
        private Vector heading;
        private float dGround = 1.2f;

        /**
         * Constructs the robot with initial parameters.
         *
         * @param material The main material the robot is made of
         * @param pos Position of the robot
         * @param heading The heading of the robot
         * @param rr The robotrace instance containing this robot
         */
        public Robot(Material material, Vector pos, Vector heading, RobotRace rr) {
            this.material = material;
            this.posX = (float) pos.x();
            this.posY = (float) pos.y();
            this.posZ = (float) pos.z();
            this.heading = heading.normalized();

            legs[0] = new RobotLeg(rr, cd, footPos(false, true),false, true);
            legs[1] = new RobotLeg(rr, cd, footPos(true, true),true, true);
            legs[2] = new RobotLeg(rr, cd, footPos(false, false),false, false);
            legs[3] = new RobotLeg(rr, cd, footPos(true, false),true, false);

            eyes[0] = new RobotEye(rr, this, cd, eyePos(false));
            eyes[1] = new RobotEye(rr, this, cd, eyePos(true));

            arms[0] = new RobotArm(rr, cd, new Vector(0.30, 0.8, -0.10), false);
            arms[1] = new RobotArm(rr, cd, new Vector(-0.30, 0.8, -0.10), true);
        }

        // returns the material of the robot
        public Material getMaterial() {
            return this.material;
        }

        // returns the current location of the robot
        public Vector getLocation() {
            return pos();
        }

        // calculates the absolute position of an eye
        private Vector eyePos(boolean rightEye) {
            double relX = rightEye ? 0.18 : -0.18;
            double relY = 1.14;
            double relZ = 0.6 ;
            return absolutePosition(relX, relY, relZ);
        }

        // returns an absolute position based on a relative position
        private Vector absolutePosition(double x, double y, double z) {
            return pos().add(new Vector(heading.x() * y, heading.y() * y, z+ dGround)).add(rightSide().scale(x));
        }

        private Vector legPos(boolean rightLeg, boolean front) {
            double relX = rightLeg ? 0.35 : -0.35;
            double relY = front ? 0.2 : -0.6;
            double relZ = 0;
            return absolutePosition(relX, relY, relZ);
        }

        private Vector footPos(boolean rightFoot, boolean front) {
            double relX = rightFoot ? 1.8 : -1.8;
            double relY = front ? 0.4 : -0.7;
            double relZ = -dGround;
            return absolutePosition(relX, relY, relZ);
        }

        /**
         * Draws this robot (as a {@code stickfigure} if specified).
         */
        public void draw(Vector loc, Vector h, boolean stickFigure) {

            double distance = loc.subtract(pos()).length();
            posX = (float) loc.x();
            posY = (float) loc.y();
            posZ = (float) loc.z();
            this.heading = h.normalized();

            gl.glPushMatrix();

            // set the material properties
            material.use(gl);


            gl.glTranslatef(posX, posY, posZ + dGround);
            cd.Transform(heading);
            gl.glRotated(90.0, 1, 0, 0);
            gl.glRotated(-90.0, 0, 1, 0);
            //Torso
            gl.glPushMatrix();
            gl.glTranslatef(0f, 0f, -0.1f);
            gl.glScalef(0.6f, 1.05f, 0.4f);
            glut.glutSolidSphere(1f, 20, 10);
            gl.glPopMatrix();


            //Arms
            for (RobotArm arm : arms) {
                arm.Draw();
            }

            //Head
            gl.glPushMatrix();
            gl.glTranslatef(0f, 0.9f, 0.2f);
            gl.glScalef(0.35f, 0.55f, 0.3f);
            glut.glutSolidSphere(1f, 20, 10);
            gl.glPopMatrix();

            //Left Eye Container
            gl.glPushMatrix();
            gl.glTranslatef(0.18f, 1.15f, 0.28f);
            glut.glutSolidCylinder(0.07f, 0.15f, 10, 10);
            gl.glPopMatrix();
            //Right Eye
            //Eye Container
            gl.glPushMatrix();
            gl.glTranslatef(-0.18f, 1.15f, 0.28f);
            glut.glutSolidCylinder(0.07f, 0.15f, 10, 10);
            gl.glPopMatrix();

            gl.glPopMatrix();

            //Legs
            for (RobotLeg leg : legs) {
                 leg.Advance(footPos(leg.isRight(), leg.isFront()),legPos(leg.isRight(), leg.isFront()));
            }
            
            
            //Left Eye
            gl.glPushMatrix();
            eyes[0].Draw(eyePos(false));
            gl.glPopMatrix();

            //Right Eye
            gl.glPushMatrix();
            eyes[1].Draw(eyePos(true));
            gl.glPopMatrix();
            
            //Left Eye
            gl.glPushMatrix();
            eyes[0].drawForceField(eyePos(false));
            gl.glPopMatrix();

            //Right Eye
            gl.glPushMatrix();
            eyes[1].drawForceField(eyePos(true));
            gl.glPopMatrix();

            
        }

    }

    /**
     * Implementation of a camera with a position and orientation.
     */
    private class Camera {

        /**
         * The position of the camera.
         */
        public Vector eye = new Vector(3f, 6f, 5f);

        /**
         * The point to which the camera is looking.
         */
        public Vector center = Vector.O;

        /**
         * The up vector.
         */
        public Vector up = Vector.Z;

        /**
         * Updates the camera viewpoint and direction based on the selected
         * camera mode.
         */
        public void update(int mode) {
            robots[0].toString();

            // draw a light above and to the left of the camera
            // calculate the direction in which the camera looks in the xy plane 
            Vector xyCameraDir = (new Vector(eye.subtract(center).x(), eye.subtract(center).y(), 0)).normalized();

            // take the cross product of that vector with the up vector to get a vector orthogonal to the direction vector in the xyplane
            Vector light1 = xyCameraDir.cross(up).normalized();

            // now we can look correct the vector if it points to the right instead of to the left
            // this part is only easy to explain when you visualize it on paper
            // basically we compare the x coordinates to find out whether light1 is pointing to the left or right
            // a switch happens at y=-x
            if (xyCameraDir.y() > -xyCameraDir.x()) {
                if (light1.x() > xyCameraDir.x()) {
                    light1 = Vector.O.subtract(light1);
                }
            } else if (xyCameraDir.y() < -xyCameraDir.x()) {
                if (light1.x() < xyCameraDir.x()) {
                    light1 = Vector.O.subtract(light1);
                }
            } else if (light1.x() != xyCameraDir.x()) {
                light1 = Vector.O.subtract(light1);
            }

            light1 = light1.add(eye);

            float light1co[] = new float[]{(float) light1.x(), (float) light1.y(), (float) light1.z(), 1.0f};

            // activate the spot
            gl.glLightfv(GL_LIGHT1, GL_POSITION, light1co, 0);

            // Helicopter mode
            if (1 == mode) {
                setHelicopterMode();

                // Motor cycle mode
            } else if (2 == mode) {
                setMotorCycleMode();

                // First person mode
            } else if (3 == mode) {
                setFirstPersonMode();

                // Auto mode
            } else if (4 == mode) {
                // code goes here...

                // Default mode
            } else {
                setDefaultMode();
            }

            glu.gluLookAt(
                    gs.vDist * Math.cos(gs.phi) * Math.sin(gs.theta) + gs.cnt.x() // X-camera
                    , gs.vDist * Math.cos(gs.phi) * Math.cos(gs.theta) + gs.cnt.y() // Y-camera
                    , gs.vDist * Math.sin(gs.phi) + gs.cnt.z(), // Z-camera
                    gs.cnt.x(), gs.cnt.y(), gs.cnt.z(),
                    0, 0, 1);
        }

        /**
         * Computes {@code eye}, {@code center}, and {@code up}, based on the
         * camera's default mode.
         */
        private void setDefaultMode() {
            // code goes here ...
        }

        /**
         * Computes {@code eye}, {@code center}, and {@code up}, based on the
         * helicopter mode.
         */
        private void setHelicopterMode() {
            // code goes here ...
        }

        /**
         * Computes {@code eye}, {@code center}, and {@code up}, based on the
         * motorcycle mode.
         */
        private void setMotorCycleMode() {
            // code goes here ...
        }

        /**
         * Computes {@code eye}, {@code center}, and {@code up}, based on the
         * first person mode.
         */
        private void setFirstPersonMode() {
            // code goes here ...
        }

    }

    /**
     * Implementation of a race track that is made from Bezier segments.
     */
    private class RaceTrack {

        /**
         * Array with control points for the O-track.
         */
        private Vector[] controlPointsOTrack;

        /**
         * Array with control points for the L-track.
         */
        private Vector[] controlPointsLTrack;

        /**
         * Array with control points for the C-track.
         */
        private Vector[] controlPointsCTrack;

        /**
         * Array with control points for the custom track.
         */
        private Vector[] controlPointsCustomTrack;

        /**
         * Constructs the race track, sets up display lists.
         */
        public RaceTrack() {
            // code goes here ...
        }

        /**
         * Draws this track, based on the selected track number.
         */
        public void draw(int trackNr) {

            // The test track is selected
            if (0 == trackNr) {
                // code goes here ...

                // The O-track is selected
            } else if (1 == trackNr) {
                // code goes here ...

                // The L-track is selected
            } else if (2 == trackNr) {
                // code goes here ...

                // The C-track is selected
            } else if (3 == trackNr) {
                // code goes here ...

                // The custom track is selected
            } else if (4 == trackNr) {
                // code goes here ...

            }
        }

        /**
         * Returns the position of the curve at 0 <= {@code t} <= 1.
         */
        public Vector getPoint(double t) {
            return Vector.O; // <- code goes here
        }

        /**
         * Returns the tangent of the curve at 0 <= {@code t} <= 1.
         */
        public Vector getTangent(double t) {
            return Vector.O; // <- code goes here
        }

    }

    /**
     * Implementation of the terrain.
     */
    private class Terrain {

        /**
         * Can be used to set up a display list.
         */
        public Terrain() {
            // code goes here ...
        }

        /**
         * Draws the terrain.
         */
        public void draw() {
            // code goes here ...
        }

        /**
         * Computes the elevation of the terrain at ({@code x}, {@code y}).
         */
        public float heightAt(float x, float y) {
            return 0; // <- code goes here
        }
    }

    /**
     * Main program execution body, delegates to an instance of the RobotRace
     * implementation.
     */
    public static void main(String args[]) {
        RobotRace robotRace = new RobotRace();
    }

}
