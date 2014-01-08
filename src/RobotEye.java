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

    public RobotEye(RobotRace rr, CartesianDraw cd,Vector pos) {
        this.rr = rr;
        this.cd = cd;
        time = new Date();
        this.pos=pos;
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

    private double timeElapsedMax2 = 0;
    private Date time;
    private Vector pos;
    private Vector movement= new Vector(0, 0, 0);

    public void Draw(Vector idealPos) {
        pre();
        double drag = 0.01;
        double accelerationMultiplier=1.0;
        Date newTime = new Date();
        int timeE = (int) (newTime.getTime() - time.getTime());
        time=newTime;
       double timeElapsed=((double) timeE) * 0.001;
        timeElapsedMax2 += timeElapsed;
        timeElapsedMax2=timeElapsedMax2%2;
        
        // redefine the ideal position with a new Z for the bobbing effect
        idealPos= new Vector(idealPos.x(), idealPos.y(), idealPos.z()+(Math.sin(timeElapsedMax2*2*Math.PI)*0.2));
        // calculate the direction of the acceleration, this is essentially the vector from the current position to the ideal one
        Vector accelerationDir=idealPos.subtract(pos);
        // calculate the distance between the current position and the ideal one
        double seperation = accelerationDir.length();
        // normalize the acceleration Vector used to track the direction of acceleration
        accelerationDir=accelerationDir.normalized();
        // scale the acceleration vector to match the realistic magnitude
        Vector acceleration = accelerationDir.scale(Math.pow(seperation, 4)*timeElapsed*timeElapsed*accelerationMultiplier);
        
        // calculate the new speed vector
        movement=movement.add(acceleration);
        
        // incorporate drag
        movement=movement.add(movement.scale(-drag*timeElapsed));
        
        // calculate the new position using the movement vector
        pos=pos.add(movement.scale(timeElapsed));
        
        
        gl.glPushMatrix();
        gl.glTranslated(pos.x(), pos.y(), pos.z());
        glut.glutSolidSphere(0.07f, 10, 10);
        gl.glPopMatrix();
    }
}
