/*
        CatVisionHW.java

    A "hardware" class intended to contain common code for accessing
    camera and vision related situations.  While previous versions were
    made to mostly to test various forms of machine vision, this version
    uses the Tensor Flow system from the FTC SDK to detect the Gold
    mineral during init in our Autonomous routines. Previous versions
    also used the DogeCV library which uses OpenCV.


    This file is a modified version from the FTC SDK.
    Modifications by FTC Team #10273, The Cat in the Hat Comes Back.
*/

package org.firstinspires.ftc.teamcode;

import android.util.Log;

import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.ClassFactory;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
import org.firstinspires.ftc.robotcore.external.tfod.Recognition;
import org.firstinspires.ftc.robotcore.external.tfod.TFObjectDetector;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

import static org.firstinspires.ftc.robotcore.external.tfod.TfodRoverRuckus.LABEL_GOLD_MINERAL;
import static org.firstinspires.ftc.robotcore.external.tfod.TfodRoverRuckus.LABEL_SILVER_MINERAL;
import static org.firstinspires.ftc.robotcore.external.tfod.TfodRoverRuckus.TFOD_MODEL_ASSET;

/**
 * This is NOT an opmode.
 *
 * This class can be used to define all vision coding we use.
 *
 */
public class CatVisionHW
{
    private static final float mmPerInch        = 25.4f;
    private static final float mmFTCFieldWidth  = (12*6) * mmPerInch;       // the width of the FTC field (from the center point to the outer panels)
    private static final float mmTargetHeight   = (6) * mmPerInch;          // the height of the center of the target image above the floor

    HardwareMap hwMap   = null;

    enum samplingPos {
        LEFT,
        CENTER,
        RIGHT
    }

    Deque<samplingPos> samplingValues;

    public int loopCount = 0;

    // Objects and Detectors
    private VuforiaLocalizer vuforia;
    public TFObjectDetector tfod;



    public void initVision(HardwareMap ahwMap) {
        hwMap = ahwMap;
        samplingValues = new ArrayDeque<samplingPos>(30);

        /**
         * Configure Vuforia by creating a Parameter object, and passing it to the Vuforia engine.
         */
        VuforiaLocalizer.Parameters parameters = new VuforiaLocalizer.Parameters();

        parameters.vuforiaLicenseKey = "AWbfTmn/////AAABmY0xuIe3C0RHvL3XuzRxyEmOT2OekXBSbqN2jot1si3OGBObwWadfitJR/D6Vk8VEBiW0HG2Q8UAEd0//OliF9aWCRmyDJ1mMqKCJZxpZemfT5ELFuWnJIZWUkKyjQfDNe2RIaAh0ermSxF4Bq77IDFirgggdYJoRIyi2Ys7Gl9lD/tSonV8OnldIN/Ove4/MtEBJTKHqjUEjC5U2khV+26AqkeqbxhFTNiIMl0LcmSSfugGhmWFGFtuPtp/+flPBRGoBO+tSl9P2sV4mSUBE/WrpHqB0Jd/tAmeNvbtgQXtZEGYc/9NszwRLVNl9k13vrBcgsiNxs2UY5xAvA4Wb6LN7Yu+tChwc+qBiVKAQe09\n";
        parameters.cameraName = hwMap.get(WebcamName.class, "Webcam 1");

        //  Instantiate the Vuforia engine
        vuforia = ClassFactory.getInstance().createVuforia(parameters);

        // Loading trackables is not necessary for the Tensor Flow Object Detection engine.

        // Now init the tfod
        int tfodMonitorViewId   = ahwMap.appContext.getResources().getIdentifier(
                "tfodMonitorViewId", "id", ahwMap.appContext.getPackageName());
        TFObjectDetector.Parameters tfodParameters = new TFObjectDetector.Parameters(tfodMonitorViewId);
        tfod                    = ClassFactory.getInstance().createTFObjectDetector(tfodParameters, vuforia);
        tfod.loadModelFromAsset(TFOD_MODEL_ASSET, LABEL_GOLD_MINERAL, LABEL_SILVER_MINERAL);

        // And now ACTIVATE!!!
        tfod.activate();
    }

    public void findGoldPos() {
        /**
         * Newest way to continuously look for the gold
         * while looping inside the autonomous init mode
         * but limits the amount of occurrences we keep
         * using a Deque.
         */


        if (samplingValues.size() > 99) {
            // Make sure we keep the size at a reasonable level
            samplingValues.removeFirst();
        }
        // getUpdatedRecognitions() will return null if no new information is available since
        // the last time that call was made.
        List<Recognition> updatedRecognitions = tfod.getUpdatedRecognitions();
        if (updatedRecognitions != null) {
            loopCount++;
            for (Recognition recognition : updatedRecognitions) {
                if (recognition.getLabel().equals(LABEL_GOLD_MINERAL) && recognition.getConfidence() > 0.5) {
                    int goldMineralX = (int) recognition.getLeft();
                    // Look for the Gold Pos and decide which side of the sampling field the gold lies
                    if (goldMineralX > 450) {
                        //***Inverted this since the camera was recently placed upside down***//
                        samplingValues.add(samplingPos.LEFT);
                        return;
                    }
                    samplingValues.add(samplingPos.CENTER);
                    return;
                }
            }
            samplingValues.add(samplingPos.RIGHT);
        }
        // Since camera is only looking at the LEFT and CENTER values, it will return RIGHT
        // if is doesn't see the gold (just basic logic)
        return;
    }

    public samplingPos giveSamplePos() {
        /**
         * A new way to take the all the values during the init
         * and choosing the value in the deque that has the most
         * occurrences.
         */

        Log.d("catbot", String.format("giveSamplePos amounts:  LEFT: %d, CENTER: %d, RIGHT: &d",
                Collections.frequency(samplingValues, samplingPos.LEFT),
                Collections.frequency(samplingValues, samplingPos.CENTER),
                Collections.frequency(samplingValues, samplingPos.RIGHT)));

        // Check to see which value has the most occurrences in the deque
        if (Collections.frequency(samplingValues, samplingPos.LEFT) > Collections.frequency(samplingValues, samplingPos.CENTER) &&
                Collections.frequency(samplingValues, samplingPos.LEFT) > Collections.frequency(samplingValues, samplingPos.RIGHT)) {
            // If the amount of LEFT readings is the most in the past 30 readings, return LEFT
            return samplingPos.LEFT;
        } else if (Collections.frequency(samplingValues, samplingPos.CENTER) > Collections.frequency(samplingValues, samplingPos.LEFT) &&
                Collections.frequency(samplingValues, samplingPos.CENTER) > Collections.frequency(samplingValues, samplingPos.RIGHT)) {
            // If the amount of CENTER readings is the most in the past 30 readings, return CENTER
            return samplingPos.CENTER;
        } else {
            // Just return back RIGHT since it is the last possible value
            return samplingPos.RIGHT;
        }
    }
}// End of class bracket