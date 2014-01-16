
import com.jogamp.opengl.util.gl2.GLUT;
import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;
import robotrace.Vector;

public class Tree {

    public Tree(RobotRace rr, CartesianDraw cd, Terrain t, double x, double y) {
        this.terrain=t;
        this.rr = rr;
        this.cd = cd;
        this.x = (float) x;
        this.y = (float) y;
        double terrainHeight=t.heightAt(x, y);
        if (terrainHeight<0) {
            isValid=false;
        }
        Vector loc = new Vector(x,y,0);
        if (rr.getTrack().getClosestPoint(loc).subtract(loc).length()<4) {
            isValid=false;
        }
        this.z = (float) (terrainHeight - 0.3);
        if (z < -0.3) {
            z = -0.3f;
        }
        treeType = Math.random() < 0.5;
        isGlitch = Math.random() < 0.1;
        randomize();
    }

    private void randomize() {
        //random values to draw the tree
        radius = (Math.random() * 0.6) + 0.2;
        height = Math.random() + 2.2;
        r1 = (float) (Math.random() + 0.15);
        r2 = (float) ((Math.random()) + 0.15);
        r3 = (float) (Math.random() + 0.15);
        r4 = (float) (Math.random() + 0.15);
        r5 = (float) ((Math.random() * 0.5) + 0.3);
        r6 = (float) ((Math.random() * 0.5) + 0.3);
        r7 = (float) ((Math.random() * 0.5) + 0.3);
        r8 = (float) ((Math.random() * 0.5) + 0.3);
        r9 = (float) ((Math.random() * 0.5) + 0.3);
        r10 = (float) ((Math.random() * 0.5) + 0.3);
        r11 = (float) ((Math.random() * 0.3) + 0.1);
        r12 = (float) ((Math.random() * 0.3) + 0.1);
        r13 = (float) ((Math.random() * 0.3) + 0.1);
    }
    private RobotRace rr;
    private CartesianDraw cd;
    private GL2 gl;
    private GLU glu;
    private GLUT glut;

    private void pre() {
        gl = rr.getGL();
        glu = rr.getGLU();
        glut = rr.getGLUT();
    }
    //variables
    private float x;
    private float y;
    private float z;
    private float r1, r2, r3, r4, r5, r6, r7, r8, r9, r10, r11, r12, r13;
    private double radius;
    private double height;
    private boolean treeType;
    private boolean isGlitch;
    private boolean isValid=true;
    private Terrain terrain;

    public boolean isValid(){
        return isValid;
    }
    public void Draw() {
        if (!isValid) {
            return;
        }
        if (isGlitch) {
            randomize();
        }
        Vector loc = new Vector(x,y,0);
        if (loc.length()>terrain.getSize()) {
            return;
        }
        pre();
        gl.glPushMatrix();
        RobotRace.Material.WOOD.use(gl);
        //drawing trunk of the tree
        cd.DrawShape((float) x, (float) y, z, (float) x, (float) y, (float) height, (float) radius, CartesianDraw.Shape.Cylinder, 10);
        gl.glPopMatrix();
        //selecting one of the two trees
        if (treeType) {
            //tree 1
            gl.glPushMatrix();
            RobotRace.Material.LEAVES.use(gl);
            //drawing the two leaves
            cd.DrawShape((float) x, (float) y, (float) (height - 0.6), (float) x, (float) y, (float) (height + 0.4), (float) (radius + 1.2), CartesianDraw.Shape.Cone, 10);
            gl.glPopMatrix();
            gl.glPushMatrix();
            cd.DrawShape((float) x, (float) y, (float) (height - 0.1), (float) x, (float) y, (float) (height + 0.7), (float) (radius + 0.9), CartesianDraw.Shape.Cone, 10);
            gl.glPopMatrix();
        } else {
            //tree 2
            gl.glPushMatrix();
            //drawing the branch  of the tree
            cd.DrawShape((float) x, (float) y, (float) (height - r5), (float) (x + r1), (float) (y), (float) (height - r8), (float) (radius * r11), CartesianDraw.Shape.Cylinder, 10);
            gl.glPopMatrix();
            gl.glPushMatrix();
            cd.DrawShape((float) x, (float) y, (float) (height - r6), (float) (x), (float) (y + r2), (float) (height - r9), (float) (radius * r12), CartesianDraw.Shape.Cylinder, 10);
            gl.glPopMatrix();
            gl.glPushMatrix();
            cd.DrawShape((float) x, (float) y, (float) (height - r7), (float) (x - r3), (float) (y - r4), (float) (height - r10), (float) (radius * r13), CartesianDraw.Shape.Cylinder, 10);
            gl.glPopMatrix();
            gl.glPushMatrix();
            RobotRace.Material.LEAVES.use(gl);
            //drawing the leaves of the tree
            cd.DrawShape((float) x, (float) y, (float) (height - 0.3), (float) x, (float) y, (float) (height + 0.7), (float) (radius + 1), CartesianDraw.Shape.Sphere, 10);
            gl.glPopMatrix();
            gl.glPushMatrix();
            cd.Orb((float) (x + r1), (float) y, (float) (height - r8), (float) (radius * r11 * 1.3), 10);
            gl.glPopMatrix();
            gl.glPushMatrix();
            cd.Orb((float) x, (float) (y + r2), (float) (height - r9), (float) (radius * r12 * 1.3), 10);
            gl.glPopMatrix();
            gl.glPushMatrix();
            cd.Orb((float) (x - r3), (float) (y - r4), (float) (height - r10), (float) (radius * r13 * 1.3), 10);
            gl.glPopMatrix();
            gl.glPushMatrix();
            cd.Orb((float) (x + r1), (float) y, (float) (height-r8), (float) (radius*r11*1.3), 10);
            gl.glPopMatrix();
            gl.glPushMatrix();
            cd.Orb((float) x, (float) (y + r2), (float) (height-r9), (float) (radius*r12*1.3), 10);
            gl.glPopMatrix();
            gl.glPushMatrix();
            cd.Orb((float) (x - r3), (float) (y - r4), (float) (height-r10), (float) (radius*r13*1.3), 10);
            gl.glPopMatrix();
        }
    }
}
