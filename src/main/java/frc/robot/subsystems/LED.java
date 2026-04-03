package frc.robot.subsystems;

import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.HubTracker;
import frc.robot.RobotContainer;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import edu.wpi.first.wpilibj.util.Color;

import java.security.AllPermission;

import com.ctre.phoenix6.controls.SolidColor;
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
    public static HubTracker.Shift currentSimulatedHubShift = HubTracker.Shift.AUTO;
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
    }
    
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
        setColor(Color.kOrange);
    } 
    public void blinkBoth() {
        setStrobeAnimation(Color.kOrange, BLINKING_FREQUENCY);
    }

    public void blinkEndgame(double frequency) {
        setStrobeAnimation(Color.kMagenta, frequency);
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
            case AUTO -> solidBoth();
            
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
            case ENDGAME_BLINK_1 -> blinkEndgame(1);
            case ENDGAME_BLINK_2 -> blinkEndgame(2);
            case ENDGAME_BLINK_3 -> blinkEndgame(4);
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

    // HubTracker.Shift currentHubShift = HubTracker.getCurrentShift().get(); // Real match
    HubTracker.Shift currentHubShift = currentSimulatedHubShift; // Testing

    public void periodic() { 
        HubTracker.Shift initialHubShift = currentSimulatedHubShift;
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
