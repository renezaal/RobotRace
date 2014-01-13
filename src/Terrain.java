
import com.jogamp.opengl.util.gl2.GLUT;
import javax.media.opengl.GL2;
import static javax.media.opengl.GL2.*;
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
public class Terrain {
    public Terrain(RobotRace rr){
        this.rr=rr;
        prepare();
    }
    RobotRace rr;
    private GL2 gl;
    private GLU glu;
    private GLUT glut;

    private void pre() {
        gl = rr.getGL();
        glu = rr.getGLU();
        glut = rr.getGLUT();
    }
    
    private int displayListIndex= 1;
    private double size=20.0;
    
    private void prepare(){
        pre();
        
        
        gl.glNewList(displayListIndex, GL_COMPILE);
        RobotRace.Material.GREEN.use(gl);
        gl.glVertex3d(size, size,0);
        gl.glVertex3d(size, -size,0);
        gl.glVertex3d(-size, -size,0);
        gl.glVertex3d(-size, size,0);
        gl.glEndList();
    }
    
    public void Draw(){
        pre();
        
        gl.glCallList(displayListIndex);
    }
    
    public double GetHeightAt(double x,double y){
        return 1.0;
    }
}
