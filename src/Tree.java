
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
    public Tree(RobotRace rr,CartesianDraw cd,double x,double y){
        this.rr=rr;
        this.cd=cd;
        this.x=x;
        this.y=y;
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
    
    private double x;
    private double y;
    
    public void Draw(){
        pre();
        gl.glPushMatrix();
        RobotRace.Material.WOOD.use(gl);
        cd.DrawShape((float)x, (float)y, 1, (float)x, (float)y, 2,(float) 0.4, CartesianDraw.Shape.Cylinder, 10);
        gl.glPopMatrix();
        gl.glPushMatrix();
        RobotRace.Material.GREEN.use(gl);
        cd.DrawShape((float)x, (float)y, (float)1.8, (float)x, (float)y,(float) 2.4,(float) 1, CartesianDraw.Shape.Cone, 10);
        gl.glPopMatrix();
        gl.glPushMatrix();
        cd.DrawShape((float)x, (float)y, (float)2.2, (float)x, (float)y, (float)2.9,(float) 0.8, CartesianDraw.Shape.Cone, 10);
        gl.glPopMatrix();
    }
}
