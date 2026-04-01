package frc.robot.subsystems;

import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.HubTracker;
import frc.robot.HubTracker.Shift;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import edu.wpi.first.wpilibj.util.Color;
import com.ctre.phoenix6.controls.SolidColor;
import com.ctre.phoenix6.hardware.CANdle;
import com.ctre.phoenix6.signals.RGBWColor;
import com.ctre.phoenix6.controls.StrobeAnimation;
 
public class LED extends SubsystemBase {
    final CANdle candle;
    final int NUM_LEDS;
    final double BLINKING_FREQUENCY;
    public static HubTracker.Shift currentSimulatedHubShift; // Used for testing, cycled manually
    
    public LED() {
        candle = new CANdle(61);
        NUM_LEDS = 8;
        BLINKING_FREQUENCY = 2.5; // Hz (amount of times turned on per second)
        currentSimulatedHubShift = Shift.AUTO;
    }
    
    public void setColor(Color color) {
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
    
    public void off() {
        setColor(Color.kBlack);
        setStrobeAnimation(Color.kBlack, 0);
    }
    
    public void solidAlliance(Alliance alliance) {
        switch (alliance) {
            case Red -> setColor(Color.kRed);
            case Blue -> setColor(Color.kBlue);
        }
    }
    public void solidMagenta() {
        setColor(Color.kMagenta);
    } 

    public void blinkAlliance(Alliance alliance) {
        switch (alliance) {
            case Red -> setStrobeAnimation(Color.kRed, BLINKING_FREQUENCY);
            case Blue -> setStrobeAnimation(Color.kBlue, BLINKING_FREQUENCY);
        }
    }
    public void blinkMagenta() {
        setStrobeAnimation(Color.kMagenta, BLINKING_FREQUENCY);
    }
    public void blinkYellow(double frequency) {
        setStrobeAnimation(Color.kYellow, frequency);
    }

    Alliance autoWinner = null;
    Alliance autoLoser = null;
    public void changeLED() {
        // LED Commands
        switch (currentHubShift) {
            case AUTO -> solidMagenta();
            
            case TRANSITION -> {
                autoWinner = HubTracker.getAutoWinner().get(); // Only update the auto winner and loser when it is actually necessary
                autoLoser = (autoWinner == Alliance.Red) ? Alliance.Blue : Alliance.Red;
                solidMagenta();
            }
            case TRANSITION_BLINK -> blinkAlliance(autoLoser);

            case SHIFT_1 -> solidAlliance(autoLoser); // Auto loser hub active
            case SHIFT_1_BLINK -> blinkAlliance(autoWinner);

            case SHIFT_2 -> solidAlliance(autoWinner); // Auto winner hub active
            case SHIFT_2_BLINK -> blinkAlliance(autoLoser);

            case SHIFT_3 -> solidAlliance(autoLoser);
            case SHIFT_3_BLINK -> blinkAlliance(autoWinner);

            case SHIFT_4 -> solidAlliance(autoWinner);
            case SHIFT_4_BLINK -> blinkMagenta();

            case ENDGAME -> solidMagenta(); // Blinking yellow faster as endgame progresses
            case ENDGAME_BLINK_1 -> blinkYellow(1);
            case ENDGAME_BLINK_2 -> blinkYellow(2);
            case ENDGAME_BLINK_3 -> blinkYellow(4);
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
    }

    // HubTracker.Shift currentHubShift = HubTracker.getCurrentShift().get(); // Real match
    HubTracker.Shift currentHubShift = currentSimulatedHubShift; // Testing

    public void periodic() { 
        HubTracker.Shift initialHubShift = currentSimulatedHubShift;
        if (currentHubShift != initialHubShift) { // Only changes LED when the shift changes
            changeLED();
            currentHubShift = initialHubShift;
        }
    }

    public static class ColorRGBW extends Color {
        public ColorRGBW(){
            super();
        }

        public static RGBWColor getColor(Color c){
            return new RGBWColor((int) c.red*255, (int) c.green*255, (int) c.blue*255);
        }
    }
}
