package org.firstinspires.ftc.teamcode.auto;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;

import org.firstinspires.ftc.teamcode.auto.support.Line;
import org.firstinspires.ftc.teamcode.auto.support.NeoPath;
import org.firstinspires.ftc.teamcode.auto.support.SplinePath;
import org.firstinspires.ftc.teamcode.auto.support.Turn;
import org.firstinspires.ftc.teamcode.auto.support.TwoWheelPathSequence;

import java.util.ArrayList;

@Autonomous(name="TestTurn")
public class TestTurn extends LinearOpMode {
    private DcMotorEx left, right;

    @Override
    public void runOpMode() throws InterruptedException {
        left = (DcMotorEx) hardwareMap.dcMotor.get("L");
        right = (DcMotorEx) hardwareMap.dcMotor.get("R");
        left.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        right.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);

        while(!opModeIsActive()) {
            telemetry.addLine("Initialized.");
            telemetry.update();
        }

        NeoPath turn = new Turn(90, 0.3683,0.5);
        NeoPath turn2 = new Turn(-90, 0.3683,0.5);
        NeoPath line = new Line(0.5,0.4);
        NeoPath line2 = new Line(-0.5,0.4);

        ArrayList<NeoPath> list = new ArrayList<NeoPath>();
        list.add(line);
        list.add(turn);
        list.add(line);
        list.add(line2);
        list.add(turn2);
        list.add(line2);

        TwoWheelPathSequence sequence = new TwoWheelPathSequence(list, left, right, 0.048);
        sequence.buildAll();
        sequence.follow();


    }
}
