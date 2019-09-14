/*
        CatIntakeHW.java

    A "hardware" class containing common code accessing hardware specific
    to the movement and rotation of the intake.  This is a modified and
    stripped down version of CatSingleOverallHW to run all of intake
    movements.  This file is used by the new autonomous OpModes to run
    multiple operations at once.


    This file is a modified version from the FTC SDK.
    Modifications by FTC Team #10273, The Cat in the Hat Comes Back.
*/

package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.ElapsedTime;

/**
 * This is NOT an OpMode.
 *
 * This class is used to define all the intake specific hardware for the robot to
 * allow for multiple operations during autonomous.  In this case, that robot is //todo Change this name
 * Jack from the Cat in the Hat Comes Back team during the 2018-2019 season.
 *
 * This hardware class assumes the following device names have been configured on the robot.
 *
 *
 * Note:  All names are lower case and have underscores between words.
 *
 * Motor channel:  Left  drive motor:        "left_rear"  & "left_front"
 * Motor channel:  Right drive motor:        "right_rear" & "right_front"
 * And so on...
 */
public class CatIntakeHW extends CatSubsystemHW
{

    /* Public OpMode members. */

    // Motors:
    public DcMotor intakeMotor = null;


    // Timers:
    ElapsedTime runtime = new ElapsedTime();

    /* local OpMode members. */



    /* Constructor */
    public CatIntakeHW(CatAsyncHW mainHardware){
        super(mainHardware);

    }


    /* Initialize standard Hardware interfaces */
    public void init()  throws InterruptedException  {

        // Define and Initialize Motors //
        intakeMotor = hwMap.dcMotor.get("intake_motor");

        // Set Motor and Servo Modes //
        intakeMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        intakeMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
    }


    public void pacManMonster() {
        /**
         * During autonomous, we use this to turn on the
         * intake and eat the stones!
         */
    }


    /* isDone stuff for intakeHW */
    static  double TIMEOUT = 3.0;
    @Override
    public boolean isDone() {
        return !(intakeMotor.isBusy() && (runtime.seconds() < TIMEOUT));
    }
}// End of class bracket