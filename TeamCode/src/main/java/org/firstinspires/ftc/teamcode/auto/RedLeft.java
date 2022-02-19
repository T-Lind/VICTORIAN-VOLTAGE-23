package org.firstinspires.ftc.teamcode.auto;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.trajectory.Trajectory;
import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.teamcode.CameraPipelines.CubeDetectionPipeline;
import org.firstinspires.ftc.teamcode.CameraPipelines.DuckDetectionPipeline;
import org.firstinspires.ftc.teamcode.CameraPipelines.TSEDetectionPipeline;
import org.firstinspires.ftc.teamcode.PIDS.LiftPID;
import org.firstinspires.ftc.teamcode.CameraPipelines.NewDetectionPipeline;
import org.firstinspires.ftc.teamcode.drive.SampleMecanumDrive;
import org.firstinspires.ftc.teamcode.trajectorysequence.TrajectorySequence;
import org.firstinspires.ftc.teamcode.trajectorysequence.TrajectorySequenceBuilder;
import org.openftc.easyopencv.OpenCvCamera;
import org.openftc.easyopencv.OpenCvCameraFactory;
import org.openftc.easyopencv.OpenCvCameraRotation;
import com.acmerobotics.roadrunner.geometry.Vector2d;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


@Autonomous(name = "RedLeft")
public class RedLeft extends LinearOpMode //creates class
{ //test test
    BNO055IMU imu;

    private int level = 0;

    private DcMotorEx lift, liftB, intake, intakeB;
    private Servo v4b1, v4b2, dep;
    private double targetDeposit;
    private CRServo duccL, duccR;
    private boolean delay = false;


    private ElapsedTime extend = new ElapsedTime();

    final int liftGrav = (int) (9.8 * 3);
    private LiftPID liftPID = new LiftPID(.05, 0, 0);
    private int liftError = 0;
    private int liftTargetPos = 0;

    private final int top = 620;
    private final int med = 225;


    private WebcamName weCam;
    private OpenCvCamera camera;
    private TSEDetectionPipeline pipeline;
    private DuckDetectionPipeline pipeline2 = new DuckDetectionPipeline();

    private SampleMecanumDrive drive; //d

    public void initialize() {

        drive = new SampleMecanumDrive(hardwareMap);
        drive.setPoseEstimate(new Pose2d(-36, -63, Math.toRadians(90)));

          intake = (DcMotorEx) hardwareMap.dcMotor.get("IN");
        lift = (DcMotorEx) hardwareMap.dcMotor.get("LI");
        intake.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        lift.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        intake.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        lift.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        intake.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        lift.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);

        lift.setDirection(DcMotor.Direction.REVERSE);

        intakeB = (DcMotorEx) hardwareMap.dcMotor.get("INB");
        intakeB.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        intakeB.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        intakeB.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);

        liftB = (DcMotorEx) hardwareMap.dcMotor.get("LIB");
        liftB.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        liftB.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        liftB.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);

        liftB.setDirection(DcMotor.Direction.REVERSE);

        v4b1 = hardwareMap.servo.get("v4b1");
        v4b2 = hardwareMap.servo.get("v4b2");
        dep = hardwareMap.servo.get("dep");
        duccL = hardwareMap.crservo.get("DL");
        duccR = hardwareMap.crservo.get("DR");

        drive = new SampleMecanumDrive(hardwareMap);


        duccL.setDirection(DcMotorSimple.Direction.FORWARD);

        v4b1.setDirection(Servo.Direction.REVERSE);


        weCam = hardwareMap.get(WebcamName.class, "Webcam 1");


        int cameraMonitorViewId = hardwareMap.appContext.getResources().getIdentifier("cameraMonitorViewId", "id", hardwareMap.appContext.getPackageName());


        camera = OpenCvCameraFactory.getInstance().createWebcam(weCam, cameraMonitorViewId);


        pipeline = new TSEDetectionPipeline();
        camera.setPipeline(pipeline);

        camera.openCameraDeviceAsync(new OpenCvCamera.AsyncCameraOpenListener() {
            // @Override
            public void onOpened() {
                telemetry.update();


                camera.startStreaming(640, 480, OpenCvCameraRotation.UPRIGHT);
            }

            @Override
            public void onError(int errorCode) {

            }
        });

        while (!opModeIsActive()) {
            level = pipeline.getLevel();
            telemetry.addData("DETECTED LEVEL: ",level);

            if(gamepad1.a)
                delay = true;


            telemetry.addData("Is delay turned on?", delay);
            telemetry.update();
        }

        // if the latest level was 0 then it must be in the 3 position.
        if(level == 0)
            level = 3;
        telemetry.addData("DETECTED LEVEL: ",level);
        telemetry.update();

        camera.setPipeline(pipeline2);
        liftError = liftTargetPos - lift.getCurrentPosition();


    }


    public void  heartbeat() throws InterruptedException {
        //if opMode is stopped, will throw and catch an InterruptedException rather than resulting in red text and program crash on phone
        if (!opModeIsActive()) {
            throw new InterruptedException();
        }
        telemetry.update();
    }

    public void keepLiftAlive(){
        if(true) {
            liftError = liftTargetPos - lift.getCurrentPosition();

            //Takes the lift up

            lift.setPower(Range.clip(liftPID.getCorrection(liftError), 0, 1));
            liftB.setPower(lift.getPower());
            telemetry.addData("Target Position", liftTargetPos);
            telemetry.addData("Current position", lift.getCurrentPosition());
            telemetry.update();
        }
    }


    public void liftAndDeposit() throws InterruptedException{
        double targetV4B = 0;
        telemetry.addLine("enteredLift");
        telemetry.update();
        if(level == 1) {
            targetV4B = .595;
            targetDeposit = .3;

        }
        else if(level==2){
                targetV4B=.675;
                liftTargetPos=med;
                targetDeposit = .3;
        }

            else if(level==3) {
            targetV4B = .81;
            liftTargetPos=top;
            targetDeposit = .3;
        }

        liftError = liftTargetPos - lift.getCurrentPosition();

        boolean depositRun = true;

        while(liftError > 50){
            liftError = liftTargetPos - lift.getCurrentPosition();

            //Takes the lift up
                lift.setPower(Range.clip(liftPID.getCorrection(liftError), -1, 1));
                liftB.setPower(lift.getPower());

        }

        extend.reset();

        while(depositRun){
            telemetry.addData("time", extend.milliseconds());
            telemetry.update();
            keepLiftAlive();
                //while(!die) {
                    if (extend.milliseconds() < 1000) {
                        keepLiftAlive();

                        //Moves the virtual bars forward
                        v4b1.setPosition(targetV4B);
                        v4b2.setPosition(targetV4B);
                    }

                    if (extend.milliseconds() > 1000 && extend.milliseconds() < 2000) {
                        keepLiftAlive();

                        //Opens the deposit
                        dep.setPosition(targetDeposit);
                    }

                    if (extend.milliseconds() > 2000 && extend.milliseconds() < 2500) {
                        keepLiftAlive();
                        dep.setPosition(.52);
                        //Moves the virtual bars backward
                        v4b1.setPosition(.19);
                        v4b2.setPosition(.19);
                    }
                    if (extend.milliseconds() > 2500 && extend.milliseconds() < 3500) {

                        //Gravity pulls the lift down
                        liftTargetPos = 0;
                        keepLiftAlive();



                    }
                    if(extend.milliseconds() > 3500){
                        depositRun = false;
                    }

                //    depositRun = false;
                }
           // }



    }


    public void starts(){
        v4b1.setPosition(.19);
        v4b2.setPosition(.19);
        dep.setPosition(.52);
    }

    @Override
    public void runOpMode() throws InterruptedException {

        initialize();
        if(delay){
            sleep(5000);
        }
        starts();
        drive.setPoseEstimate(new Pose2d(-36, -63, Math.toRadians(90)));
        redLeft();
        //liftAndDeposit();
    }

    public void redLeft() throws InterruptedException{


        if (isStopRequested()) return;


        drive.setPoseEstimate(new Pose2d(-36, -63, Math.toRadians(90)));
        Trajectory traj1 = drive.trajectoryBuilder(new Pose2d(-36, -63, Math.toRadians(90)))
                .splineTo(new Vector2d(-33, -21), Math.toRadians(90))

                .build();
        drive.followTrajectory(traj1);
        drive.turn(Math.toRadians(90));
      // level=1;
        //liftAndDeposit();

        Trajectory traj2 = drive.trajectoryBuilder(new Pose2d(-34, -21, Math.toRadians(-180)))//
                .splineTo(new Vector2d(-63, -58.5), Math.toRadians(-90))

                .build();
        drive.followTrajectory(traj2);
        spinDuck();

        Trajectory traj3 = drive.trajectoryBuilder(new Pose2d(-63, -58.5, Math.toRadians(-90)))
                .lineTo(new Vector2d(-70,-33.5))

                .build();
        drive.followTrajectory(traj3);

        double strafe_amount = pipeline2.getDucc_x();
        for(int i=0;i<10;i++) {
            strafe_amount = pipeline2.getDucc_x();
            if(strafe_amount == Integer.MIN_VALUE)
                i--;
        }
        telemetry.addData("sa: ",strafe_amount);
        telemetry.update();
        Trajectory traj23 = drive.trajectoryBuilder(new Pose2d(-70,-33.5),drive.getPoseEstimate().getHeading())
                .strafeRight(strafe_amount)

                .build();
        drive.followTrajectory(traj23);

        /*
        Trajectory traj3 = drive.trajectoryBuilder(new Pose2d(-64, -60, Math.toRadians(-90)),true)
                .splineTo(new Vector2d(-49, -35),Math.toRadians(-270))

                .build();
        drive.followTrajectory(traj3);


        // code to intake duck - dx and dy might need to be swapped
        // cause based on what I know, dx and dy should be right but in my testing it was not.
        // also dy should be the right amount but we'll see, that's easy to adjust
    //    intake.setPower(-.7);
     //   intakeB.setPower(-.7);


        double dx = pipeline2.getDucc_x();
        for(int i = 0;i<35;i++){
            double dx_add = pipeline2.getDucc_x();
            if(dx_add != Integer.MIN_VALUE)
                dx+=dx_add;
        }
        dx/=10;
        dx*=-1;

        double dy = -25;

        telemetry.addData("dx: ",dx);
        telemetry.update();

        Trajectory traj4 = drive.trajectoryBuilder(new Pose2d(-49, -35, Math.toRadians(-90)))
                .lineTo(new Vector2d(-49-dx, -35+dy))

                .build();

        drive.followTrajectory(traj4);

        // duck intake movement ends

        Trajectory traj5 = drive.trajectoryBuilder(new Pose2d(-49-dx, -35+dy, Math.toRadians(-90)),true)
                .splineTo(new Vector2d(-34.7,-18.5),Math.toRadians(0))

                .build();
        drive.followTrajectory(traj5);

        intake.setPower(0);
        intakeB.setPower(0);
        // I turn off intake here instead of earlier so that if the duck gets yeeted there is a possibility this yoinks it up
        liftAndDeposit();*/
        /*Trajectory traj6 = drive.trajectoryBuilder(new Pose2d(-63+strafe_amount, -58.5, Math.toRadians(-90)))
                .lineTo(new Vector2d(-70,-29))//,Math.toRadians(-180))

                .build();
        drive.followTrajectory(traj6);

*/




/*

        Trajectory traj1 = drive.trajectoryBuilder(new Pose2d(),true)
                .splineToLinearHeading(new Pose2d(-20, 6), Math.toRadians(0))
                .build();
        drive.followTrajectory(traj1);
        drive.turn(Math.toRadians(110));
        drive.turn(Math.toRadians(70));
        Trajectory traj2 = drive.trajectoryBuilder(new Pose2d(-6,20),true)
                .splineToLinearHeading(new Pose2d(-6, -25), Math.toRadians(180))
                .build();
        drive.followTrajectory(traj2);

        Trajectory traj3 = drive.trajectoryBuilder(new Pose2d())
                .back(3)
                .build();

        drive.followTrajectory(traj3);

        Trajectory traj = drive.trajectoryBuilder(new Pose2d(-3,0))
                .strafeRight(21.75)
                .build();

        drive.followTrajectory(traj);



        Trajectory traj2 = drive.trajectoryBuilder(new Pose2d(-3,-21.75))
                .back(11.5)
                .build();

        drive.followTrajectory(traj2);
*/
/*
        liftAndDeposit();

        Trajectory traj4 = drive.trajectoryBuilder(new Pose2d(-17.5,-29.5))
                .forward(12.5)
                .build();

        //DO NOT MESS WITH ANYTHING HERE AFTER
        drive.followTrajectory(traj4);

        Trajectory traj5 = drive.trajectoryBuilder(new Pose2d(-4.5,-29.5))
                .strafeLeft(55)
                .build();

        drive.followTrajectory(traj5);

        spinDuck();
        Trajectory traj9 = drive.trajectoryBuilder(new Pose2d(-4.5, 26))
                .back(2.5)
                .build();
        drive.followTrajectory(traj9);

        intake.setPower(-.55);
        intakeB.setPower(-.55);

       /*  Trajectory traj10 = drive.trajectoryBuilder(new Pose2d(-6.5, 23.5))
                .forward(6.5)
                .build();
        drive.followTrajectory(traj9);

       Trajectory traj = drive.trajectoryBuilder(new Pose2d(-4.5, 23.5))
                .forward(4.5)
                .build();
        drive.followTrajectory(traj);*/

      /*  Trajectory traj6 = drive.trajectoryBuilder(new Pose2d(-1.5, 25))
                .strafeRight(52)
                .build();
        drive.followTrajectory(traj6);
        Trajectory traj7 = drive.trajectoryBuilder(new Pose2d(-1.5, -26.5))
                .back(12)
                .build();
        drive.followTrajectory(traj7);
        intake.setPower(0);
        intakeB.setPower(0);
        liftTargetPos = top;
        liftAndDeposit();
        Trajectory traj8 = drive.trajectoryBuilder(new Pose2d(-13.5,-26.5),true)
                .lineTo(new Vector2d(-23.75, 33.75))
                .build();
        drive.followTrajectory(traj8);

     /*   Trajectory traj6 = drive.trajectoryBuilder(new Pose2d(3.5,27.75))
                .back(29.5)
                .build();

        drive.followTrajectory(traj6);
        */
/*
        Trajectory traj7 = drive.trajectoryBuilder(new Pose2d(-14,-27))
                .strafeLeft(2)
                .build();

        drive.followTrajectory(traj7);

        TrajectorySequence traj1 = drive.trajectorySequenceBuilder(new Pose2d())
                .back(30)
                .strafeRight(6.5)
                .build();

        drive.followTrajectorySequence(traj1);
        drive.turn(Math.toRadians(-50));
        level = 1;
        liftAndDeposit();
        drive.turn(Math.toRadians(50));
        //TrajectorySequence traj2 = drive.trajectorySequenceBuilder(new Pose2d(-30,-6.5))
            //    .forward(25)
              //  .strafeLeft(29)
              //  .build();

        //drive.followTrajectorySequence(traj2);
        TrajectorySequence traj2 = drive.trajectorySequenceBuilder(new Pose2d(-30,-6.5))
               // .lineTo(new Vector2d(-6,24.5))
                .forward(24)
                .strafeLeft(33)
                        .build();
        drive.followTrajectorySequence(traj2);
       /* Trajectory traj3 = drive.trajectoryBuilder(new Pose2d(-6,25.5))
                .lineTo(new Vector2d(-13, 11))
                .build();

        drive.followTrajectory(traj3);*/
          //where tiernan's code comes in
        //TrajectorySequence traj4 = drive.trajectorySequenceBuilder(new Pose2d(-13,11)) //edit coordinates accordingly to tiernan's positioning
          //    .back(17)
            //   .strafeRight(17)
              //  .build();
        //drive.followTrajectorySequence(traj4);
        //drive.turn(Math.toRadians(-70));
       // liftTargetPos=top;
        //liftAndDeposit();
        //drive.turn(Math.toRadians(70));
       // Trajectory traj5 = drive.trajectoryBuilder(new Pose2d(-30,-5))
         //       .lineTo(new Vector2d(-27,25.5))
          //      .build();
        //drive.followTrajectory(traj5);
        /*spinDuck();
TrajectorySequence traj5 = drive.trajectorySequenceBuilder(new Pose2d(-6,27.5))
        .back(23)
        .strafeLeft(2)
        .build();
drive.followTrajectorySequence(traj5);*/
    }




    public void spinDuck() throws InterruptedException{
        ElapsedTime spinTime = new ElapsedTime();
        duccL.setPower(-.25);
        duccR.setPower(-.25);


        while (spinTime.milliseconds() <= 2500)
            heartbeat();
        duccL.setPower(0);
        duccR.setPower(0);
        while (spinTime.milliseconds() <= 4000) {

            heartbeat();
            duccL.setPower(-.7);
            duccR.setPower(-.7);
        }
        duccL.setPower(0);
        duccR.setPower(0);


     // duckIntake();


    }
    public void duckIntake() throws InterruptedException{

        ElapsedTime spinTime = new ElapsedTime();



        while (spinTime.milliseconds() <= 2000)
            heartbeat();
        intake.setPower(0);
        intakeB.setPower(0);
    }
    public static double get_dist(DuckDetectionPipeline pipeline){
        int cnt = 0;
        double mean = 0;
        while(10 > cnt){
            double dist_x = pipeline.getDucc_x();
            if(dist_x != Integer.MIN_VALUE) {
                cnt++;
                mean += dist_x;
            }
        }
        mean /= cnt;
        return mean;
    }

}

