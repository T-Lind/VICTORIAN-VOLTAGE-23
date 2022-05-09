package org.firstinspires.ftc.teamcode.auto.support;
/**
 * Program to take linear velocities from each wheel and translate
 * them into 2wd
 * Created by
 * @author Tiernan Lindauer
 * for FTC team 7797.
 * @license MIT License
 * Last edited 5/5/22
 */

import static org.firstinspires.ftc.robotcore.external.navigation.AngleUnit.RADIANS;

import java.util.ArrayList;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.hardware.DcMotorEx;

public class TwoWheelPathSequence {

    private ArrayList<NeoPath> trajectory;

    private double wheelRadius;

    private DcMotorEx left;
    private DcMotorEx right;

    MarkerList markerList;
    /**
     *
     * @param d is the ArrayList of paths
     * @param left is the left motor (presumed to be negative to go forward)
     * @param right is the right motor (presumed to be positive to go forward)
     * @param wheelR is the wheel's radius
     *
     * @Precondition the left and right motors are objects that have been externally created
     */
    public TwoWheelPathSequence(ArrayList<NeoPath> d, DcMotorEx left, DcMotorEx right, double wheelR){
        trajectory = d;
        wheelRadius = wheelR;

        this.left = left;
        this.right = right;

        markerList = null;
    }
    /**
     *
     * @param d is the ArrayList of paths
     * @param left is the left motor (presumed to be negative to go forward)
     * @param right is the right motor (presumed to be positive to go forward)
     * @param wheelR is the wheel's radius
     * @param mL is the MarkerList object, which can be constructed directly in the constructor of this object
     *
     * @Precondition the left and right motors are objects that have been externally created
     * @Precondition mL is not null
     */
    public TwoWheelPathSequence(ArrayList<NeoPath> d, DcMotorEx left, DcMotorEx right, double wheelR, MarkerList mL){
        trajectory = d;
        wheelRadius = wheelR;

        this.left = left;
        this.right = right;

        markerList = mL;
    }

    /**
     * Build each NeoPath in the trajectory.
     */
    public void buildAll(){
        for(NeoPath p : trajectory)
            p.build();
    }

    /**
     * Build a specific trajectory
     * @param i the index of the NeoPath that you want to build.
     */
    public void build(int i){
        trajectory.get(i).build();
    }

    /**
     * @Precondition all paths have been build using the buildAll() method.
     * Note that this is not technically necessary but reduces lag time.
     */

    public void reset(){
        for(NeoPath p : trajectory)
            p.setCompleted(false);
    }

    /**
     * Actually moves the robot along the specified NeoPaths.
     * Also adheres to InsertMarkers if any.
     */
    public void follow(){
        ElapsedTime t = new ElapsedTime();
        t.reset();
        for(NeoPath p : trajectory){
            if(!p.getBuilt())
                p.build();

            KalmanFilter k3 = new KalmanFilter(0);
            PIDController pid3 = new PIDController(0);

            KalmanFilter k4 = new KalmanFilter(0);
            PIDController pid4 = new PIDController(0);

            double offset = t.milliseconds();

            while(!p.getCompleted()){
                double leftV = p.convert(wheelRadius, p.getLeftVelocity((t.milliseconds()-offset)/1000));
                double rightV = p.convert(wheelRadius, p.getRightVelocity((t.milliseconds()-offset)/1000));

                double corL = pid3.update((long)leftV, (long)k3.filter(left.getVelocity(RADIANS)));
                double corR = pid4.update((long)rightV, (long)k4.filter(right.getVelocity(RADIANS)));

                left.setVelocity(corL+leftV, RADIANS);
                right.setVelocity(corR+rightV, RADIANS);
                if(markerList != null)
                    for(InsertMarker m : markerList.getMarkers())
                        m.execute(t.milliseconds()/1000);
            }
            reset();
        }
    }
}