package org.firstinspires.ftc.teamcode.auto.coyotesupport.enumerations;

public enum PathType {
    /**
     * The path to move in a line.
     */
    LINE,
    /**
     *  The path to move in a spline (SplinePath).
     */
    TURN,
    /**
     * The path to move in a turn.
     */
    SPLINE,

    /**
     * Move in a spline but at a constant heading on a differential swerve
     */
    CONSTANTSPLINE,

    /**
     * Turn only differential swerve pods
     */
    PODTURN,

    /**
     *  Move in a line at a constant heading for any given angle - only for mecanum!
     */
    MECLINE
}