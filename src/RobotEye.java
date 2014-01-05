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
 * @author rene
 */
public class RobotEye {

    public RobotEye(RobotRace rr, CartesianDraw cd) {
        this.rr = rr;
        this.cd = cd;
        time = new Date();
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

    private double timeElapsed = 0;
    private Date time;

    public void Draw() {
        pre();
        Date newTime = new Date();
        int timeE = (int) (newTime.getTime() - time.getTime());
        time=newTime;
        timeElapsed += ((double) timeE) * 0.002;
        timeElapsed=timeElapsed%2;
        
        Vector relPos = new Vector(0, 0, Math.sin(timeElapsed*Math.PI)*0.05);
        
        gl.glPushMatrix();
        gl.glTranslated(0, 0, relPos.z());
        glut.glutSolidSphere(0.07f, 10, 10);
        gl.glPopMatrix();
    }
}
