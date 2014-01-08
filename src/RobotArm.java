/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

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
 * @author Administrator
 */
public class RobotArm {
    public RobotArm(RobotRace rr,CartesianDraw cd,Vector start,boolean right){
        this.rr=rr;
        this.cd=cd;
        pos=start;
        elbowDir=new Vector(right?-1:1, 0, -1);
        this.right =right;
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
    
    private Vector pos;
    private double upperArmLength=0.4;
    private double lowerArmLength=0.6;
    private Vector elbowDir;
    private boolean right;
    
    public void Draw(){
        pre();
        
        
        
        Vector end = new Vector(right?-0.05:0.05, 0.8, -0.1);
        end=end.add(pos);
        cd.Joint(pos, end, (float)upperArmLength, (float)lowerArmLength, elbowDir, 0.08f, 10);
    }
}
