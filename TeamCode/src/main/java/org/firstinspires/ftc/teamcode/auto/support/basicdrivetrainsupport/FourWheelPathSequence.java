package org.firstinspires.ftc.teamcode.auto.support.basicdrivetrainsupport;

import static org.firstinspires.ftc.robotcore.external.navigation.AngleUnit.RADIANS;

import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.auto.support.broadsupport.KalmanFilter;
import org.firstinspires.ftc.teamcode.auto.support.broadsupport.NeoPath;
import org.firstinspires.ftc.teamcode.auto.support.broadsupport.PIDController;
import org.firstinspires.ftc.teamcode.auto.support.broadsupport.PathSequenceFather;

import java.util.ArrayList;

/**
 * Program to take linear velocities from each wheel and translate
 * them into 4wd
 * Created by
 * @author Tiernan Lindauer
 * for FTC team 7797.
 */
public class FourWheelPathSequence extends PathSequenceFather {
    private DcMotorEx left1;
    private DcMotorEx left2;
    private DcMotorEx right1;
    private DcMotorEx right2;

    /**
     *
     * @param paths is the ArrayList of paths
     * @param left1 and is a left motor (presumed to be negative to go forward) does not matter which
     * @param left2 and is a left motor (presumed to be negative to go forward) does not matter which
     * @param right1 is the right motor (presumed to be positive to go forward) does not matter which
     * @param right2 is the right motor (presumed to be positive to go forward) does not matter which
     * @param wheelR is the wheel's radius
     *
     * @Precondition the left and right motors are objects that have been externally created
     */
    public FourWheelPathSequence(ArrayList<NeoPath> paths, DcMotorEx left1, DcMotorEx left2, DcMotorEx right1, DcMotorEx right2, double wheelR){
        trajectory = paths;
        wheelRadius = wheelR;

        this.left1 = left1;
        this.left2 = left2;
        this.right1 = right1;
        this.right2= right2;

    }

    /**
     * Actually moves the robot along the specified NeoPaths.
     * Also adheres to InsertMarkers if any.
     */
    public final void follow(){
        ElapsedTime t = new ElapsedTime();
        t.reset();
        for(NeoPath p : trajectory){
            if(!p.getBuilt())
                p.build();

            // Create kalman filter and PID objects
            KalmanFilter kLeft1 = new KalmanFilter(0);
            PIDController pidLeft1 = new PIDController(0);

            KalmanFilter kLeft2 = new KalmanFilter(0);
            PIDController pidLeft2 = new PIDController(0);

            KalmanFilter kRight1 = new KalmanFilter(0);
            PIDController pidRight1 = new PIDController(0);

            KalmanFilter kRight2 = new KalmanFilter(0);
            PIDController pidRight2 = new PIDController(0);

            double offset = t.milliseconds();

            // Execute the path
            while(!p.getCompleted()){
                // Get the velocities from what the path says the end result velocities should be
                double leftV = NeoPath.convert(wheelRadius, p.getLeftVelocity((t.milliseconds()-offset)/1000));
                double rightV = NeoPath.convert(wheelRadius, p.getRightVelocity((t.milliseconds()-offset)/1000));

                // Correct based on PID and kalman filter
                double corL1 = pidLeft1.update((long)leftV, (long)kLeft1.filter(left1.getVelocity(RADIANS)));
                double corL2 = pidLeft2.update((long)leftV, (long)kLeft2.filter(left2.getVelocity(RADIANS)));
                double corR1 = pidRight1.update((long)rightV, (long)kRight1.filter(right1.getVelocity(RADIANS)));
                double corR2 = pidRight2.update((long)rightV, (long)kRight2.filter(right2.getVelocity(RADIANS)));

                // Write the corrected values
                left1.setVelocity(corL1+leftV, RADIANS);
                left2.setVelocity(corL2+leftV, RADIANS);

                right1.setVelocity(corR1+rightV, RADIANS);
                right2.setVelocity(corR2+rightV, RADIANS);
            }
            resetPaths();
        }
    }
}
