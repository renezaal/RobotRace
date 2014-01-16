
import com.jogamp.opengl.util.gl2.GLUT;
import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Administrator
 */
public class Tree {

    public Tree(RobotRace rr, CartesianDraw cd, Terrain t, double x, double y) {
        this.rr = rr;
        this.cd = cd;
        this.x = (float) x;
        this.y = (float) y;
        this.z = (float)(t.heightAt(x, y)-0.3);
        radius = (Math.random()*0.6)+ 0.2;
        height = Math.random() + 2.2;
        r1 = (float)(Math.random()+0.5);
        r2 = (float)((Math.random())+0.5);
        r3 = (float)(Math.random()+0.5);
        r4 = (float)(Math.random()+0.5);
        treeType = false;
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
    private float x;
    private float y;
    private float z;
    private float r1,r2,r3,r4;
    private double radius;
    private double height;
    private boolean treeType;

    public void Draw() {
        pre();
        if (treeType) {
            gl.glPushMatrix();
            RobotRace.Material.WOOD.use(gl);
            cd.DrawShape((float) x, (float) y, z, (float) x, (float) y, (float) height, (float) radius, CartesianDraw.Shape.Cylinder, 10);
            gl.glPopMatrix();
            gl.glPushMatrix();
            RobotRace.Material.LEAVES.use(gl);
            cd.DrawShape((float) x, (float) y, (float) (height-0.6), (float) x, (float) y, (float) (height+0.4), (float) (radius+1.2), CartesianDraw.Shape.Cone, 10);
            gl.glPopMatrix();
            gl.glPushMatrix();
            cd.DrawShape((float) x, (float) y, (float) (height-0.1), (float) x, (float) y, (float) (height+0.7), (float) (radius+0.9), CartesianDraw.Shape.Cone, 10);
            gl.glPopMatrix();
        } else {
            gl.glPushMatrix();
            RobotRace.Material.WOOD.use(gl);
            cd.DrawShape((float) x, (float) y, z, (float) x, (float) y, (float) height, (float) radius, CartesianDraw.Shape.Cylinder, 10);
            gl.glPopMatrix();
            gl.glPushMatrix();
            cd.DrawShape((float) x, (float) y, (float) (height-0.5), (float) (x + r1), (float) (y + r2), (float) (height-0.5), (float) (radius*0.3), CartesianDraw.Shape.Cylinder, 10);
            gl.glPopMatrix();
            gl.glPushMatrix();
            cd.DrawShape((float) x, (float) y, (float) (height-0.7), (float) (x - r3), (float) (y - r4), (float) (height-0.7), (float) (radius*0.5), CartesianDraw.Shape.Cylinder, 10);
            gl.glPopMatrix();
            gl.glPushMatrix();
            RobotRace.Material.LEAVES.use(gl);
            cd.DrawShape((float) x, (float) y, (float) (height-0.3), (float) x, (float) y, (float) (height+0.7), (float) (radius+1), CartesianDraw.Shape.Sphere, 10);
            gl.glPopMatrix();
        }
    }
}
