
import com.jogamp.opengl.util.gl2.GLUT;
import com.jogamp.opengl.util.texture.Texture;
import java.awt.geom.Point2D;
import java.util.Date;
import static javax.media.opengl.GL.GL_TEXTURE_2D;
import static javax.media.opengl.GL.GL_TRIANGLES;
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

        // Initialize robots
        for (int i = 0; i < 4; i++) {
            robots[i] = new Robot(Material.GOLD, new Vector(0, i * 10, 0), new Vector(0, 1, 0), this,i);
        }

        // Initialize the camera
        camera = new Camera();

        // Initialize the race track
        raceTrack = new RaceTrack();

        // Initialize the terrain
        terrain = new Terrain(this);
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
        
        gl.glDisable(GL_CULL_FACE);
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
            drawAxisFrame();
            
        // Draw all four robots
        for (int i = 0; i < robots.length; i++) {
            robots[i].draw(false);
        }

        // Draw race track
        raceTrack.draw(gs.trackNr);

        // Draw terrain
        terrain.Draw();
    }

    /**
     * Draws the x-axis (red), y-axis (green), z-axis (blue), and origin
     * (yellow).
     */
    public void drawAxisFrame() {
        // code goes here ...
        if (gs.showAxes) {

            // draw a red arrow along the X-axis
            Material.RED.use(gl);
            cd.Rectangle(2f, 0, 0, 0.1f);

            // draw a cone at the end
            gl.glPushMatrix();
            cd.Transform(2f, 0f, 0f, 2.12f, 0f, 0f, 0.12f);
            glut.glutSolidCone(1f, 1.5f, 10, 10);
            gl.glPopMatrix();

            // draw a green arrow along the Y-axis
            Material.GREEN.use(gl);
            cd.Rectangle(0, 2f, 0, 0.1f);

            // draw a cone at the end
            gl.glPushMatrix();
            cd.Transform(0f, 2f, 0f, 0f, 2.12f, 0f, 0.12f);
            glut.glutSolidCone(1f, 1.5f, 10, 10);
            gl.glPopMatrix();

            // draw a blue arrow along the Z-axis
            Material.BLUE.use(gl);
            cd.Rectangle(0, 0, 2f, 0.1f);

            // draw a cone at the end
            gl.glPushMatrix();
            cd.Transform(0f, 0f, 2f, 0f, 0f, 2.12f, 0.12f);
            glut.glutSolidCone(1f, 1.5f, 10, 10);
            gl.glPopMatrix();

            // draw a yellow orb at the origin
            Material.YELLOW.use(gl);
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
        /*
        Transparent blue flickering color to emulate some sort of forcefield
        */
        FORCEFIELD(
                forceFieldColor(),
                forceFieldColor(),
                new float[]{0f}
        ),
        /*
        Water color
        */
        WATER(
                new float[]{0.50754f, 0.50754f, 0.50754f, 0.5f},
                new float[]{0.508273f, 0.508273f, 0.508273f, 0.3f},
                new float[]{80f}
        ),
        /*
        Nice clear material for non-interference with textures
        */
        NONE(
                new float[]{1f, 1f, 1f, 1f},
                new float[]{1f, 1f, 1f, 1f},
                new float[]{10f}
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
                new float[]{1f, 1f, 0f, 1.0f},
                new float[]{1f, 1f, 0f, 1.0f},
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
        private float dGround = 0.3f;
        private double distance=0;
        private double speed=1;
        private RobotRace rr;
        private double time;
        private int lane;
       

        /**
         * Constructs the robot with initial parameters.
         *
         * @param material The main material the robot is made of
         * @param pos Position of the robot
         * @param heading The heading of the robot
         * @param rr The robotrace instance containing this robot
         */
        public Robot(Material material, Vector pos, Vector heading, RobotRace rr,int lane) {
            this.rr=rr;
            this.lane=lane;
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

            arms[0] = new RobotArm(rr, cd, new Vector(0.075, 0.2, -0.025), false);
            arms[1] = new RobotArm(rr, cd, new Vector(-0.075, 0.2, -0.025), true);
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
            double relX = rightEye ? 0.045 : -0.045;
            double relY = 0.285;
            double relZ = 0.15 ;
            return absolutePosition(relX, relY, relZ);
        }

        // returns an absolute position based on a relative position
        private Vector absolutePosition(double x, double y, double z) {
            return pos().add(new Vector(heading.x() * y, heading.y() * y, z+ dGround)).add(rightSide().scale(x));
        }

        private Vector legPos(boolean rightLeg, boolean front) {
            double relX = rightLeg ? 0.0875 : -0.0875;
            double relY = front ? 0.05 : -0.15;
            double relZ = 0;
            return absolutePosition(relX, relY, relZ);
        }

        private Vector footPos(boolean rightFoot, boolean front) {
            double relX = rightFoot ? 0.45 : -0.45;
            double relY = front ? 0.1 : -0.175;
            double relZ = -dGround;
            return absolutePosition(relX, relY, relZ);
        }

        /**
         * Draws this robot (as a {@code stickfigure} if specified).
         */
        public void draw(boolean stickFigure) {

            time=rr.getTime();
            
            distance+=time*speed;
            
            Vector loc = rr.raceTrack.getPoint(distance);
            Vector h = rr.raceTrack.getTangent(distance);
           loc= loc.add(h.cross(Vector.Z).normalized().scale(((double)lane)-1.5));
            
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
            gl.glTranslatef(0f, 0f, -0.025f);
            gl.glScalef(0.15f, 0.2625f, 0.1f);
            glut.glutSolidSphere(1f, 20, 10);
            gl.glPopMatrix();


            //Arms
            for (RobotArm arm : arms) {
                arm.Draw();
            }

            //Head
            gl.glPushMatrix();
            gl.glTranslatef(0f, 0.225f, 0.05f);
            gl.glScalef(0.0875f, 0.1375f, 0.075f);
            glut.glutSolidSphere(1f, 20, 10);
            gl.glPopMatrix();

            //Left Eye Container
            gl.glPushMatrix();
            gl.glTranslatef(0.045f, 0.2875f, 0.07f);
            glut.glutSolidCylinder(0.0175f, 0.0375f, 10, 10);
            gl.glPopMatrix();
            //Right Eye
            //Eye Container
            gl.glPushMatrix();
            gl.glTranslatef(-0.045f, 0.2875f, 0.07f);
            glut.glutSolidCylinder(0.0175f, 0.0375f, 10, 10);
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
        int testTrack = -1;
        double testTrackDistance=0;
        int oTrack = -1;
        double oTrackDistance=0;
        int lTrack = -1;
        double lTrackDistance=0;
        int cTrack = -1;
        double cTrackDistance=0;
        int customTrack = -1;
        double customTrackDistance=0;
        double distance=0;
        int lastTrackNr=0;
        
        /**
         * Array with control points for the O-track.
         */
        private Vector[] controlPointsOTrack = {                                
                                new Vector(0, -20, 0),   new Vector(30, -20, 0),    new Vector(30, 20, 0),  new Vector(0, 20, 0),
                                new Vector(0, 20, 0),   new Vector(-30, 20, 0),   new Vector(-30, -20, 0), new Vector(0, -20, 0)
                                };
        /**
         * Array with control points for the L-track.
         */
        private Vector[] controlPointsLTrack = {                                
                                new Vector(-20, 40, 0),   new Vector(-20, -20, 0),  new Vector(-20, -20, 0),  new Vector(20, -20, 0),
                                new Vector(20, -20, 0),     new Vector(30, -20, 0),      new Vector(30, 0, 0),      new Vector(20, 0, 0),
                                new Vector(20, 0, 0),     new Vector(0, 0, 0),      new Vector(0, 0, 0),      new Vector(0, 40, 0),
                                new Vector(0, 40, 0),     new Vector(0, 50, 0),      new Vector(-20, 50, 0),      new Vector(-20, 40, 0),
                                };

        /**
         * Array with control points for the C-track.
         */
        private Vector[] controlPointsCTrack  = {                                
                                new Vector(10, 10, 0),   new Vector(0, 10, 0),  new Vector(0, -10, 0),  new Vector(10, -10, 0),
                                new Vector(10, -10, 0),     new Vector(20, -10, 0),      new Vector(20, -30, 0),      new Vector(10, -30, 0),
                                new Vector(10, -30, 0),     new Vector(-30, -30, 0),      new Vector(-30, 30, 0),      new Vector(10, 30, 0),
                                new Vector(10, 30, 0),     new Vector(20, 30, 0),      new Vector(20, 10, 0),      new Vector(10, 10, 0)
                                };

        /**
         * Array with control points for the custom track.
         */
        private Vector[] controlPointsCustomTrack = {
                                new Vector(0, 40, 0),   new Vector(0, 30, 0),  new Vector(0, 30, 0),  new Vector(-10, 30, 0),
                                new Vector(-10, 30, 0),   new Vector(-20, 30, 0),  new Vector(-20, 30, 0),  new Vector(-20, 10, 0),
                                new Vector(-20, 10, 0),   new Vector(-20, -10, 0),  new Vector(-20,-10, 0),  new Vector(-50, -10, 0),
                                new Vector(-50, -10, 0),   new Vector(-60, -10, 0),  new Vector(-60, -40, 0),  new Vector(-50, -40, 0),
                                new Vector(-50, -40, 0),   new Vector(-30, -40, 0),  new Vector(-30, -40, 0),  new Vector(-30, -30, 0),
                                new Vector(-30, -30, 0),   new Vector(-30, -20, 0),  new Vector(-30, -20, 0),  new Vector(-20, -20, 0),
                                new Vector(-20, -20, 0),   new Vector(-10, -20, 0),  new Vector(-10, -20, 0),  new Vector(-10, -60, 0),
                                new Vector(-10, -60, 0),   new Vector(-10, -70, 0),  new Vector(10, -70, 0),  new Vector(10, -60, 0),
                                new Vector(10, -60, 0),   new Vector(10, -40, 0),  new Vector(10, -40, 0),  new Vector(20, -40, 0),
                                new Vector(20, -40, 0),   new Vector(30, -40, 0),  new Vector(30, -40, 0),  new Vector(30, 0, 0),
                                new Vector(30, 0, 0),   new Vector(30, 10, 0),  new Vector(10, 10, 0),  new Vector(10, 0, 0),
                                new Vector(10, 0, 0),   new Vector(10, -20, 0),  new Vector(10, -20, 0),  new Vector(0, -20, 0),
                                new Vector(0, -20, 0),   new Vector(-10, -20, 0),  new Vector(-10, 20, 0),  new Vector(0, 20, 0),
                                new Vector(0, 20, 0),   new Vector(20, 20, 0),  new Vector(20, 20, 0),  new Vector(20, 30, 0),
                                new Vector(20, 30, 0),   new Vector(20, 50, 0),  new Vector(20, 50, 0),  new Vector(10, 50, 0),
                                new Vector(10, 50, 0),   new Vector(0, 50, 0),  new Vector(0, 50, 0),  new Vector(0, 40, 0),
        };

        /**
         * Constructs the race track, sets up display lists.
         */
        public RaceTrack() {

        }
        
        
        /**
         * Draws this track, based on the selected track number.
         */
        public void draw(int trackNr) {
           
            lastTrackNr=trackNr;
            double numberOfSteps = 200;
            double step = 1 / numberOfSteps;
            
            // The test track is selected
            if (0 == trackNr) {
                // Checks if display list is allready created                
                if (testTrack == -1) {
                    // Resets the distance
                    distance = 0;
                    // Creates a display list for the test track 
                   testTrack = gl.glGenLists(1);
                    gl.glNewList(testTrack, GL_COMPILE);
                   
                    gl.glBegin(GL_TRIANGLES);
                        // Loops true all te points from t = 0  to t = 1
                        for (double t = 0; t < 1; t += step) {
                            // Calculates current point
                            Vector current = testTrackGetPoint(t);
                            // Calculates next point
                            Vector next = testTrackGetPoint(t + step);
                            // Calculates current tangent 
                            Vector currentTangent = testTrackGetTangent(t);
                            // Calculates next tangent
                            Vector nextTangent = testTrackGetTangent(t + step);
                            
                            // Calculates the distance from point to point with squarroot((x2-x1)^2 + (y2-y1)^2 + (z2-z1)^2) and adds it to distance collected so far
                            distance += Math.sqrt(Math.pow(next.x()-current.x(),2) + Math.pow(next.y()-current.y(),2) + Math.pow(next.z()-current.z(),2));
                                                   
                            // Calculates the perpendicular vector (outside and inside) to the current tangent vector
                            Vector currentPerpendicular = currentTangent.cross(Vector.Z).normalized();
                            Vector currentPerpendicularInner = currentTangent.cross(Vector.Z).normalized().scale(-1);
                            // Calculates the perpendicular vector (outside and inside) to the next tangent vector
                            Vector nextPerpendicular = nextTangent.cross(Vector.Z).normalized();
                            // Calculates the inner and outer points for the current and next points (z=1)
                            Vector currentTrackOuter = current.add(currentPerpendicular.scale(2)).add(Vector.Z);
                            Vector currentTrackInner = current.add(currentPerpendicular.scale(-2)).add(Vector.Z);
                            Vector nextTrackOuter = next.add(nextPerpendicular.scale(2)).add(Vector.Z);
                            Vector nextTrackInner = next.add(nextPerpendicular.scale(-2)).add(Vector.Z);
                            
                            // Calculates the inner and outer points for the current and next base points (z=0)
                            Vector currentBaseOuter = current.add(currentPerpendicular.scale(2));
                            Vector currentBaseInner = current.add(currentPerpendicular.scale(-2));
                            Vector nextBaseOuter = next.add(nextPerpendicular.scale(2));
                            Vector nextBaseInner = next.add(nextPerpendicular.scale(-2));
                            
                            // Calculates a normal vector (in the z direction) the the plane
                            Vector currentNormal = currentPerpendicular.cross(currentTangent).normalized();
                            
                            // Draw the track
                            drawTrack(  currentNormal, 
                                        currentPerpendicular, 
                                        currentPerpendicularInner,
                                        currentTrackOuter,
                                        currentTrackInner,
                                        nextTrackOuter,
                                        nextTrackInner,
                                        currentBaseOuter,
                                        currentBaseInner,
                                        nextBaseOuter,
                                        nextBaseInner   );
                            
                        }
                        gl.glEnd();
                        gl.glDisable(GL_TEXTURE_2D);
                        // Ends the display list
                        gl.glEndList();
                        // Sets the distance for the test track
                        testTrackDistance=distance;
                } else {
                        // Calls the display list
                        gl.glCallList(testTrack);
                }         
            } else if (1 == trackNr) {
                // Checks if display list is allready created
                if (oTrack == -1) {
                    // Creates a display list for the O track 
                    oTrack = gl.glGenLists(2);
                    gl.glNewList(oTrack, GL_COMPILE);                     
                    // Calculates the points
                    drawCubicBezier(controlPointsOTrack);
                    // Ends the display list
                    gl.glEndList();                    
                    // Sets the distance for the O track
                    oTrackDistance=distance;
                } else {
                    // Calls the display list
                    gl.glCallList(oTrack);
                }


                // The L-track is selected
            } else if (2 == trackNr) {
                // Checks if display list is allready created
                if (lTrack == -1) {
                    // Creates a display list for the L track 
                    lTrack = gl.glGenLists(1);
                    gl.glNewList(lTrack, GL_COMPILE);
                    // Calculates the points                            
                    drawCubicBezier(controlPointsLTrack);
                    // Ends the display list                    
                    gl.glEndList();                    
                    // Sets the distance for the L track
                    lTrackDistance=distance;
                } else {
                    // Calls the display list
                    gl.glCallList(lTrack);
                }

                // The C-track is selected
            } else if (3 == trackNr) {
                // Checks if display list is allready created
                if (cTrack == -1) {
                    // Creates a display list for the C track 
                    cTrack = gl.glGenLists(1);
                    gl.glNewList(cTrack, GL_COMPILE);
                    // Calculates the points                           
                    drawCubicBezier(controlPointsCTrack);
                    // Ends the display list                   
                    gl.glEndList();
                    // Sets the distance for the C track                   
                    cTrackDistance=distance;
                } else {
                    // Calls the display list
                    gl.glCallList(cTrack);
                }

                // The custom track is selected
            } else if (4 == trackNr) {
                // Checks if display list is allready created
                if (customTrack == -1) {
                    // Creates a display list for the custom track 
                    customTrack = gl.glGenLists(1);
                    gl.glNewList(customTrack, GL_COMPILE);
                    // Calculates the points
                    drawCubicBezier(controlPointsCustomTrack);
                    // Ends the display list                                        
                    gl.glEndList();
                    // Sets the distance for the custom track
                    customTrackDistance=distance;
                } else {
                    // Calls the display list
                    gl.glCallList(customTrack);
                }

            }
        }

        // Return the point of the curve at a specified t
        private Vector testTrackGetPoint(double t) {
            // for point.x 10cos(2PI*t), for point.y 14sin(2PI*t), for point.z 0
            return new Vector(  10*Math.cos(2*Math.PI*t),
                                14*Math.sin(2*Math.PI*t),
                                0);
        }
        
        public Vector getPoint(double t) {
            t=t/getDistance();
            t%=1;
            if (lastTrackNr==0) {
                
            return new Vector(  10*Math.cos(2*Math.PI*t),
                                14*Math.sin(2*Math.PI*t),
                                1);
            } else {
                return getPointOnBezierTrack(t, getTrack());
            }
        }
        
        
        // gets a point on a bezier track
        private Vector getPointOnBezierTrack(double t,Vector[] track){
            // get the number of points the track exists of
           double parts = track.length;
           // divide by four to get the number of parts
           parts/=4.0;
           // decide which part is the one to use by rounding down parts*t
           int part = (int)(parts*t);
           part *= 4.0;
           // calculate the size of a part compared to the whole
           double partSize = 1.0/parts;
           // modulate t to be smaller that a partSize, essentialy discarding all uninteresting parts
           t%=partSize;
           // divide by the partsize to know where on the current part this is
           t/=partSize;
           // call the getCubicBezierPnt method with our newly found values
           return getCubicBezierPnt(t,track[part],track[part+1],track[part+2],track[part+3]).add(Vector.Z.scale(1));
                   }

        // gets the tangent for a certain point on a bezier track
        private Vector getTangentOnBezierTrack(double t,Vector[] track){
            // get the number of points the track exists of
           double parts = track.length;
           // divide by four to get the number of parts
           parts/=4.0;
           // decide which part is the one to use by rounding down parts*t
           int part = (int)(parts*t);
           part *= 4.0;
           // calculate the size of a part compared to the whole
           double partSize = 1.0/parts;
           // modulate t to be smaller that a partSize, essentialy discarding all uninteresting parts
           t%=partSize;
           // divide by the partsize to know where on the current part this is
           t/=partSize;
           // call the getCubicBezierPnt method with our newly found values
           return getCubicBezierTng(t,track[part],track[part+1],track[part+2],track[part+3]);
                   }
        
        // Return the tangent point of the curve at a specified t
        private Vector testTrackGetTangent(double t) {
            // for point.x 20PI*-sin(2PI*t), for point.y 28PI*cos(2PI*t), for point.z 0
            return new Vector(  20*Math.PI*-Math.sin(2*Math.PI*t), 
                                28*Math.PI*Math.cos(2*Math.PI*t), 
                                0);
        }
        
        public Vector getTangent(double t) {
            t=t/getDistance();
            t%=1;
            if (lastTrackNr==0) {
                
            return new Vector(  20*Math.PI*-Math.sin(2*Math.PI*t), 
                                28*Math.PI*Math.cos(2*Math.PI*t), 
                                1).normalized();
            } else    {
                return getTangentOnBezierTrack(t, getTrack());
            }
        }
        
        private double getDistance(){
            switch(lastTrackNr){
                case 0:
                    return testTrackDistance;
                case 1:
                    return oTrackDistance;
                case 2:
                    return lTrackDistance;
                case 3:
                    return cTrackDistance;
                case 4:
                    return customTrackDistance;
                default:
                    return 1d;
            }
        }
        
        private Vector[] getTrack(){
            switch(lastTrackNr){
                case 1:
                    return controlPointsOTrack;
                case 2:
                    return controlPointsLTrack;
                case 3:
                    return controlPointsCTrack;
                case 4:
                    return controlPointsCustomTrack;
                default:
                    return null;
            }
        }
        
        // Return the point of the bezier curve at a specified t
        // B(t) = (1-t)^3 * Point0 + 3 * (1-t)^2 * t * Point 1 + 3 * (1-t) * t^2 * Point2 + t^3 * Point3
        public Vector getCubicBezierPnt(double t, Vector Point0, Vector Point1, Vector Point2, Vector Point3) {
             return new Vector( Math.pow(1 - t, 3) * Point0.x() + 3 * Math.pow(1 - t, 2) * t * Point1.x() + 3 * (1 - t) * Math.pow(t, 2)
                                * Point2.x() + Math.pow(t, 3) * Point3.x(), 
                                Math.pow(1 - t, 3) * Point0.y() + 3 * Math.pow(1 - t, 2) * t * Point1.y() + 3 * (1 - t) * Math.pow(t, 2)
                                * Point2.y() + Math.pow(t, 3) * Point3.y(), 
                                Math.pow(1 - t, 3) * Point0.z() + 3 * Math.pow(1 - t, 2) * t * Point1.z() + 3 * (1 - t) * Math.pow(t, 2)
                                * Point2.z() + Math.pow(t, 3) * Point3.z());
        }
        
        // Return the tangent point of the bezier curve at a specified t
        // B'(t) = 3 * (1-t)^2 * (Point1 - Point 0) + 6 * (1-t) * t * (Point2 - Point1) + 3 * t^^2 * (Point3 - Point 2)
        public Vector getCubicBezierTng(double t, Vector Point0, Vector Point1, Vector Point2, Vector Point3) {
            return new Vector(  (3 * Math.pow(1 - t, 2) * (Point1.x() - Point0.x())) + (6 * (1 - t) * t * (Point2.x() 
                                - Point1.x())) + (3 * Math.pow(t, 2) * (Point3.x() - Point2.x())),
                                (3 * Math.pow(1 - t, 2) * (Point1.y() - Point0.y())) + (6 * (1 - t) * t * (Point2.y() 
                                - Point1.y())) + (3 * Math.pow(t, 2) * (Point3.y() - Point2.y())),
                                (3 * Math.pow(1 - t, 2) * (Point1.z() - Point0.z())) + (6 * (1 - t) * t * (Point2.z() 
                                - Point1.z())) + (3 * Math.pow(t, 2) * (Point3.z() - Point2.z())));
         }
        
        public void drawCubicBezier(Vector[] controlPoints){
            double numberOfSteps = 200;
            double step = 1 / numberOfSteps;
            
            gl.glBegin(GL_TRIANGLES);
                                        for (int i = 0; i < controlPoints.length; i += 4){
                                            for (double t = 0; t < 1; t += step) {
                                                        // Calculates the first point.
                                                        Vector current = getCubicBezierPnt(t, controlPoints[i+0],
                                                                        controlPoints[i+1], controlPoints[i+2],
                                                                        controlPoints[i+3]);
                                                        // Calculates the second point.
                                                        Vector next = getCubicBezierPnt(t + step, controlPoints[i+0],
                                                                        controlPoints[i+1], controlPoints[i+2],
                                                                        controlPoints[i+3]);
                                                        // Calculates the normal to the first point.
                                                        Vector currentTangent = getCubicBezierTng(t, controlPoints[i+0],
                                                                        controlPoints[i+1], controlPoints[i+2],
                                                                        controlPoints[i+3]);
                                                        // Calculates the normal to the second point.
                                                        Vector nextTangent = getCubicBezierTng(t + step, controlPoints[i+0],
                                                                        controlPoints[i+1], controlPoints[i+2],
                                                                        controlPoints[i+3]);
                                                        // Calculates all the vertices of the intersection of the track at the first point.
                                                         
                                                        distance += Math.sqrt(Math.pow(next.x()-current.x(),2) + Math.pow(next.y()-current.y(),2) + Math.pow(next.z()-current.z(),2));
                            
                                                        
                            Vector currentPerpendicular = currentTangent.cross(Vector.Z).normalized();
                            Vector currentPerpendicularInner = currentTangent.cross(Vector.Z).normalized().scale(-1);
                            Vector nextPerpendicular = nextTangent.cross(Vector.Z).normalized();
                            Vector currentTrackOuter = current.add(currentPerpendicular.scale(2)).add(Vector.Z);
                            Vector currentTrackInner = current.add(currentPerpendicular.scale(-2)).add(Vector.Z);
                            Vector nextTrackOuter = next.add(nextPerpendicular.scale(2)).add(Vector.Z);
                            Vector nextTrackInner = next.add(nextPerpendicular.scale(-2)).add(Vector.Z);
                            
                            Vector currentBaseOuter = current.add(currentPerpendicular.scale(2));
                            Vector currentBaseInner = current.add(currentPerpendicular.scale(-2));
                            Vector nextBaseOuter = next.add(nextPerpendicular.scale(2));
                            Vector nextBaseInner = next.add(nextPerpendicular.scale(-2));
                            
                            Vector currentNormal = currentPerpendicular.cross(currentTangent).normalized();
                            
                            // Draws the track
                            drawTrack(  currentNormal, 
                                        currentPerpendicular, 
                                        currentPerpendicularInner,
                                        currentTrackOuter,
                                        currentTrackInner,
                                        nextTrackOuter,
                                        nextTrackInner,
                                        currentBaseOuter,
                                        currentBaseInner,
                                        nextBaseOuter,
                                        nextBaseInner   );
                            
                            
                                                
                             }
                          }
                    gl.glEnd();
                    gl.glDisable(GL_TEXTURE_2D);
        
        }
        
        public void drawTrack(  Vector currentNormal, 
                                Vector currentPerpendicular, 
                                Vector currentPerpendicularInner,
                                Vector currentTrackOuter,
                                Vector currentTrackInner,
                                Vector nextTrackOuter,
                                Vector nextTrackInner,
                                Vector currentBaseOuter,
                                Vector currentBaseInner,
                                Vector nextBaseOuter,
                                Vector nextBaseInner
                                ){
                            
                            gl.glNormal3d(currentNormal.x(), currentNormal.y(), currentNormal.z());
                            Material.NONE.use(gl);
    
                            gl.glEnable(GL_TEXTURE_2D);
                            track.bind(gl);
                            gl.glTexCoord2d(1, 0);
                            gl.glVertex3d(currentTrackOuter.x(), currentTrackOuter.y(), currentTrackOuter.z());
                            gl.glTexCoord2d(1, 1);
                            gl.glVertex3d(nextTrackOuter.x(), nextTrackOuter.y(), nextTrackOuter.z());
                            gl.glTexCoord2d(0, 1);
                            gl.glVertex3d(nextTrackInner.x(), nextTrackInner.y(), nextTrackInner.z());
                           
                            gl.glTexCoord2d(1, 1);
                            gl.glVertex3d(currentTrackOuter.x(), currentTrackOuter.y(), currentTrackOuter.z());
                            gl.glTexCoord2d(0, 0);
                            gl.glVertex3d(currentTrackInner.x(), currentTrackInner.y(), currentTrackInner.z());
                            gl.glTexCoord2d(0, 1);
                            gl.glVertex3d(nextTrackInner.x(), nextTrackInner.y(), nextTrackInner.z());
                            
                            gl.glNormal3d(currentPerpendicular.x(), currentPerpendicular.y(), currentPerpendicular.z());
                            brick.bind(gl);
                            gl.glTexCoord2d(0, 1);
                            gl.glVertex3d(currentTrackOuter.x(), currentTrackOuter.y(), currentTrackOuter.z());
                            gl.glTexCoord2d(0, 0);
                            gl.glVertex3d(currentBaseOuter.x(), currentBaseOuter.y(), currentBaseOuter.z());
                            gl.glTexCoord2d(1, 1);
                            gl.glVertex3d(nextTrackOuter.x(), nextTrackOuter.y(), nextTrackOuter.z());
                            
                            gl.glTexCoord2d(0, 0);
                            gl.glVertex3d(currentBaseOuter.x(), currentBaseOuter.y(), currentBaseOuter.z());
                            gl.glTexCoord2d(1, 0);
                            gl.glVertex3d(nextBaseOuter.x(), nextBaseOuter.y(), nextBaseOuter.z());
                            gl.glTexCoord2d(1, 1);
                            gl.glVertex3d(nextTrackOuter.x(), nextTrackOuter.y(), nextTrackOuter.z());
                            
                            gl.glNormal3d(currentPerpendicularInner.x(), currentPerpendicularInner.y(), currentPerpendicularInner.z());
                            
                            gl.glTexCoord2d(0, 1);
                            gl.glVertex3d(currentTrackInner.x(), currentTrackInner.y(), currentTrackInner.z());
                            gl.glTexCoord2d(1, 0);
                            gl.glVertex3d(nextBaseInner.x(), nextBaseInner.y(), nextBaseInner.z());
                            gl.glTexCoord2d(1, 1);
                            gl.glVertex3d(nextTrackInner.x(), nextTrackInner.y(), nextTrackInner.z());
                            
                            gl.glTexCoord2d(0, 1);
                            gl.glVertex3d(currentTrackInner.x(), currentTrackInner.y(), currentTrackInner.z());
                            gl.glTexCoord2d(0, 0);
                            gl.glVertex3d(currentBaseInner.x(), currentBaseInner.y(), currentBaseInner.z());
                            gl.glTexCoord2d(1, 0);
                            gl.glVertex3d(nextBaseInner.x(), nextBaseInner.y(), nextBaseInner.z());
                            

            
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
