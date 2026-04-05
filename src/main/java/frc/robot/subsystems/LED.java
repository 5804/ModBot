package frc.robot.subsystems;

import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.HubTracker;
import frc.robot.RobotContainer;
import frc.robot.HubTracker.Shift;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import edu.wpi.first.wpilibj.util.Color;
import frc.robot.subsystems.DriveSubsystem;

import java.security.AllPermission;
import java.util.stream.IntStream;

import org.ejml.equation.IntegerSequence.Range;

import com.ctre.phoenix6.hardware.CANdle;
import com.ctre.phoenix6.configs.*;
import com.ctre.phoenix6.signals.*;
import com.ctre.phoenix6.controls.*;
 
public class LED extends SubsystemBase {
    final CANdle candle;
    final int NUM_LEDS;
    final double BLINKING_FREQUENCY;
    final Alliance ALLIANCE;
    // Used for testing, cycled manually
    public static Shift currentSimulatedHubShift = Shift.AUTO;
    public static Alliance simulatedAutoWinner = Alliance.Blue;
    
    public LED() {
        candle = new CANdle(61);

        CANdleConfiguration config = new CANdleConfiguration();
        config.CANdleFeatures.Enable5VRail = Enable5VRailValue.Enabled;
        config.LED.StripType = StripTypeValue.GRB;
        config.LED.BrightnessScalar = 0.75;
        config.CANdleFeatures.VBatOutputMode = VBatOutputModeValue.Modulated;
        candle.getConfigurator().apply(config);

        NUM_LEDS = 180;
        BLINKING_FREQUENCY = 2.5; // Hz (amount of times turned on per second)
        ALLIANCE = RobotContainer.alliance.get();
        isClimbing = false;
    }
    
    // ---- Climbing ----

    public int getNumLitLEDs() {
        double heading = DriveSubsystem.getHeading();
        int headingModulus = (int) Math.round(heading) % 360;

        if (headingModulus < 160) {
            return (int) Math.round(Math.abs(DriveSubsystem.getPitch()));
        } else if (headingModulus >= 160) {
            return (int) Math.round(180 - ((Math.abs(DriveSubsystem.getPitch()))));
        } else {
            return 0;
        }
    }

    public SolidColor getRainbowLedRequest(int frame) {
        int startLED = 1;
        int endLED = getNumLitLEDs();
        SolidColor request = new SolidColor(startLED-1, endLED-1);

        int r;
        int g;
        int b;

        // Total of 1530 frames for rainbow animation (255*6)
        // 255,0,0 - 255,255,0 - 0,255,0 - 0,255,255 - 0,0,255 - 255,0,255
        if (frame >= 0 && frame <= 255*1) { // 255, 0^, 0;
            r = 255;
            g = frame;
            b = 0;
        } else if (frame > 255 && frame <= 255*2) { // 255v, 255, 0
            r = 255*2 - frame;
            g = 255;
            b = 0;
        } else if (frame > 255*2 && frame < 255*3) { // 0, 255, 0^
            r = 0;
            g = 255;
            b = frame - 255*2;
        } else if (frame > 255*3 && frame <= 255*4) { // 0, 255v, 255
            r = 0;
            g = 255*4 - frame;
            b = 255;
        } else if (frame > 255*4 && frame <= 255*5) { // 0^, 0, 255
            r = frame - 255*4;
            g = 0;
            b = 255;
        } else if (frame > 255*5 && frame <= 255*6) { // 255, 0, 255v
            r = 255;
            g = 0;
            b = 255*6 - frame;
        } else {
            r = 255;
            g = 255;
            b = 255;
        }

        request = request.withColor(new RGBWColor(r, g, b));
        return request;
    }
    public SolidColor getBlinkLedRequest(int frame) {
        int startLED = getNumLitLEDs();
        int endLED = NUM_LEDS;
        SolidColor request = new SolidColor(startLED-1, endLED-1);

        int framesPerBlink = (int) Math.round(50/endgameBlinkFrequencyGlobal);

        // f blinks/second
        // 50 frames/second
        // 50/f frames/blink

        IntStream onFrames = IntStream.range(1, framesPerBlink/2+1);
        
        if (onFrames.anyMatch(n -> n == frame)) {
            System.out.println("ON");
            request = request.withColor(new RGBWColor(Color.kWhite)); // On
        } else {
            System.out.println("OFF");
            request = request.withColor(new RGBWColor(Color.kBlack)); // Off
        }
        
        return request;
    }

    public void setClimb(int rainbowFrame, int blinkFrame) {
        candle.clearAllAnimations();

        // System.out.println("Pitch: " + DriveSubsystem.getPitch());
        // System.out.println("Roll: " + DriveSubsystem.getRoll());
        // System.out.println("Heading: " + DriveSubsystem.getHeading());
        // System.out.println("NumLitLEDs: " + numLitLEDs);

        SolidColor requestRainbow = getRainbowLedRequest(rainbowFrame);
        SolidColor requestBlink = getBlinkLedRequest(blinkFrame);

        candle.setControl(requestRainbow);
        candle.setControl(requestBlink);
    }

    // public void startClimbing() {
    //     isClimbing = true;
    // }
    // public void stopClimbing() {
    //     isClimbing = false;
    // }

    // ---- Normal color and animation methods ----

    public void setColor(Color color) {
        candle.clearAllAnimations();

        SolidColor colorRequest = new SolidColor(0, NUM_LEDS-1);
        colorRequest = colorRequest.withColor(ColorRGBW.getColor(color));

        candle.setControl(colorRequest);
    }
    public void setStrobeAnimation(Color color, double frequency) {
        StrobeAnimation animationRequest = new StrobeAnimation(0, NUM_LEDS-1);
        animationRequest = animationRequest.withColor(ColorRGBW.getColor(color));
        animationRequest = animationRequest.withFrameRate(frequency);
        
        candle.setControl(animationRequest);
    }
    public void setFireAnimation() {
        FireAnimation animationRequest = new FireAnimation(0, NUM_LEDS-1);
        animationRequest = animationRequest.withCooling(0.2);
        animationRequest = animationRequest.withSparking(0.3);
        animationRequest = animationRequest.withFrameRate(27);
        
        candle.setControl(animationRequest);
    }
    public void setRainbowAnimation() {
        RainbowAnimation animationRequest = new RainbowAnimation(0, NUM_LEDS-1);

        candle.setControl(animationRequest);
    }

    public void off() {
        setColor(Color.kBlack);
        setStrobeAnimation(Color.kBlack, 0);
    }
    
    // ---- Hub Shifting ----

    public void solidAlliance(Alliance alliance) {
        if (alliance == ALLIANCE) {
            setColor(Color.kLime);
        } else {
            setColor(Color.kRed);
        }
    }
    public void blinkAlliance(Alliance alliance) {
        if (alliance == ALLIANCE) {
            setStrobeAnimation(Color.kLime, BLINKING_FREQUENCY);
        } else {
            setStrobeAnimation(Color.kRed, BLINKING_FREQUENCY);
        }
    }

    public void solidBoth() {
        setColor(Color.kYellow);
    } 
    public void blinkBoth() {
        setStrobeAnimation(Color.kYellow, BLINKING_FREQUENCY);
    }

    public void blinkEndgame(double frequency) {
        setStrobeAnimation(Color.kWhite, frequency);
    }

    public void rainbow() {
        setRainbowAnimation();
    }
    public void fire() {
        setFireAnimation();
    }

    Alliance autoWinner = null;
    Alliance autoLoser = null;
    public void changeLED(HubTracker.Shift hubShift) {
        switch (hubShift) {
            case AUTO -> {
                isClimbing = false;
                solidBoth();
            }

            case TRANSITION -> {
                // autoWinner = HubTracker.getAutoWinner().get(); // Only update the auto winner and loser when it is actually necessary
                autoWinner = simulatedAutoWinner;
                autoLoser = (autoWinner == Alliance.Red) ? Alliance.Blue : Alliance.Red;
                solidBoth();
            }
            case TRANSITION_BLINK -> blinkAlliance(autoLoser);

            case SHIFT_1 -> solidAlliance(autoLoser); // Auto loser hub active
            case SHIFT_1_BLINK -> blinkAlliance(autoWinner);

            case SHIFT_2 -> solidAlliance(autoWinner); // Auto winner hub active
            case SHIFT_2_BLINK -> blinkAlliance(autoLoser);

            case SHIFT_3 -> solidAlliance(autoLoser);
            case SHIFT_3_BLINK -> blinkAlliance(autoWinner);

            case SHIFT_4 -> solidAlliance(autoWinner);
            case SHIFT_4_BLINK -> blinkBoth();

            case ENDGAME -> solidBoth(); // Blinking yellow faster as endgame progresses
            case ENDGAME_BLINK_1 -> {
                isClimbing = true;
                endgameBlinkFrequencyGlobal = 0.5;
            }
            case ENDGAME_BLINK_2 -> {
                endgameBlinkFrequencyGlobal = 1;
            }
            case ENDGAME_BLINK_3 -> {
                endgameBlinkFrequencyGlobal = 2;
            }
        }
    }

    int shiftIndex = 0;
    HubTracker.Shift[] shifts = HubTracker.Shift.values();
    public void cycleHubShift() {
        if (shiftIndex < 14) {
            shiftIndex++;
        } else {
            shiftIndex = 0;
        }
        currentSimulatedHubShift = shifts[shiftIndex];
        System.out.println(currentSimulatedHubShift);
    }

    // Shift currentHubShift = HubTracker.getCurrentShift().get(); // Real match
    Shift currentHubShift = currentSimulatedHubShift; // Testing
    // Shift currentHubShift = Shift.ENDGAME_BLINK_1; // Testing
    int rainbowFrameGlobal = 1;
    int blinkFrameGlobal = 1;
    double endgameBlinkFrequencyGlobal = 0;
    boolean isClimbing = false;
    public void periodic() { 
        if (isClimbing) {
            if (rainbowFrameGlobal >= 1530) {
                rainbowFrameGlobal = 1;
            } else {
                rainbowFrameGlobal += 4;
            }
            if (blinkFrameGlobal >= 50/endgameBlinkFrequencyGlobal) {
                blinkFrameGlobal = 1;
            } else {
                blinkFrameGlobal++;
            }

            System.out.println("CLIMB, Endgame hz: " + endgameBlinkFrequencyGlobal);
            setClimb(rainbowFrameGlobal, blinkFrameGlobal);
        }

        Shift initialHubShift = currentSimulatedHubShift;
        if (currentHubShift != initialHubShift) { // Only changes LED when the shift changes
            currentHubShift = initialHubShift;
            changeLED(currentHubShift);
        }
    }
    public static class ColorRGBW extends Color {
        public ColorRGBW(){
            super();
        }

        public static RGBWColor getColor(Color c){
            return new RGBWColor((int) Math.round(c.red*255), (int) Math.round(c.green*255), (int) Math.round(c.blue*255));
        }
    }
}
