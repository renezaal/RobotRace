/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import com.jogamp.opengl.util.gl2.GLUT;
import java.util.Date;
import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;
import robotrace.Vector;

/**
 *
 * @author rene
 */
public class RobotEye {

    public RobotEye(RobotRace rr,RobotRace.Robot r, CartesianDraw cd,Vector pos) {
        this.rr = rr;
        this.cd = cd;
        time = new Date();
        this.pos=pos;
        this.lastIdealPos=pos;
        this.r=r;
    }

    private RobotRace.Robot r;
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
    // the position of the eye
    private Vector pos;
    // is the vector of speed in units per second
    private Vector movement= new Vector(0, 0, 0);
    private Vector lastIdealPos;

    public void Draw(Vector idealPos) {
        if (Double.isNaN(pos.x())||Double.isNaN(pos.y())||Double.isNaN(pos.z())) {
            pos=idealPos;
            movement=Vector.O;
        }
        pre();
        double drag = 0.01;
        double accelerationMultiplier=1.0;
        // the max distance is when the eye touches the forcefield
        // so the radius of the forcefield minus the radius of the eye and minus a small value to avoid clipping
        double maxDistance = (0.05-(0.0175))-0.001;
        Date newTime = new Date();
        int timeE = (int) (newTime.getTime() - time.getTime());
        time=newTime;
       double timeElapsed=((double) timeE) * 0.001;
        timeElapsedMax2 += timeElapsed;
        timeElapsedMax2=timeElapsedMax2%2;
        
        // redefine the ideal position with a new Z for the bobbing effect
        idealPos= new Vector(idealPos.x(), idealPos.y(), idealPos.z()+(Math.sin(timeElapsedMax2*2*Math.PI)*0.005));
        // calculate the direction of the acceleration, this is essentially the vector from the current position to the ideal one
        Vector accelerationDir=idealPos.subtract(pos);
        // calculate the distance between the current position and the ideal one
        double separation = accelerationDir.length();
        // normalize the acceleration Vector used to track the direction of acceleration
        accelerationDir=accelerationDir.normalized();
        // scale the acceleration vector to match the realistic magnitude
        Vector acceleration = accelerationDir.scale(separation*timeElapsed*timeElapsed*accelerationMultiplier);
        
        // calculate the new speed vector
        movement=movement.add(acceleration);
        
        // incorporate drag
        movement=movement.subtract(movement.scale(drag*timeElapsed));
        
        // calculate the new position using the movement vector
        pos=pos.add(movement.scale(timeElapsed));
        
        // calculate the new separation
        separation=pos.subtract(idealPos).length();
        
        // if the orb is outside the forcefield
        if (separation>maxDistance) {
            // put it back inside
            pos=idealPos.add(accelerationDir.scale(-maxDistance));
            // and reset the speed to stimulate getting pushed by the forcefield with loss of all own kinetic energy
            movement=accelerationDir.scale(idealPos.subtract(lastIdealPos).length()/timeElapsed);
        }
        
        // draw the eye
        gl.glPushMatrix();
        gl.glTranslated(pos.x(), pos.y(), pos.z());
        glut.glutSolidSphere(0.0175f, 10, 10);
        gl.glPopMatrix();
        lastIdealPos=idealPos;
    }
    
    public void drawForceField(Vector idealPos){
        pre();
                
        // draw the forcefield
        gl.glPushMatrix();
        RobotRace.Material.FORCEFIELD.update();
        RobotRace.Material.FORCEFIELD.use(gl);
        cd.Orb(idealPos, 0.05f, 10);
        r.getMaterial().use(gl);
        gl.glPopMatrix();
    }
}
