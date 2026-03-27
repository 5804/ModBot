package frc.robot.subsystems;

import edu.wpi.first.wpilibj2.command.SubsystemBase;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import java.util.*;

import com.ctre.phoenix6.controls.SolidColor;
import com.ctre.phoenix6.hardware.CANdle;
import com.ctre.phoenix6.signals.RGBWColor;
import com.ctre.phoenix6.controls.StrobeAnimation;

import frc.robot.HubTracker;
import frc.robot.RobotContainer;
 
public class LED extends SubsystemBase {
    HashMap<String, int[]> COLORS;
    private final CANdle candle;
    int NUM_LEDS;
    double BLINKING_FREQUENCY;
    
    public LED() {
        candle = new CANdle(61);
        NUM_LEDS = 8;
        BLINKING_FREQUENCY = 2.5; // Hz


        COLORS = new HashMap<>();
        COLORS.put("red", new int[] {255,0,0});
        COLORS.put("blue", new int[] {0,0,255});
        COLORS.put("magenta", new int[] {255,0,255});
        COLORS.put("orange", new int[] {255,165,0});
        COLORS.put("green", new int[] {0,255,0});
        COLORS.put("white", new int[] {255,255,255});
        COLORS.put("off", new int[] {0,0,0});
    }
    
    public void setColor(int[] color) {
        SolidColor colorRequest = new SolidColor(0, NUM_LEDS-1);

        colorRequest = colorRequest.withColor(new RGBWColor(color[0], color[1], color[2]));

        candle.setControl(colorRequest);
    }

    public void setStrobeAnimation(int[] color, double frequency) {
        StrobeAnimation animationRequest = new StrobeAnimation(0, NUM_LEDS-1);

        animationRequest = animationRequest.withColor(new RGBWColor(color[0], color[1], color[2]));
        animationRequest = animationRequest.withFrameRate(frequency);

        candle.setControl(animationRequest);
    }
    
    public Command off() {
        int[] off = COLORS.get("off");
        return Commands.runOnce(() -> {
            setColor(off);
            setStrobeAnimation(off, 0);
        });
    }
    
    public Command red() {
        return Commands.runOnce(() -> setColor(COLORS.get("red")));
    }
    public Command blue() {
        return Commands.runOnce(() -> setColor(COLORS.get("blue")));
    }
    public Command magenta() {
        return Commands.runOnce(() -> setColor(COLORS.get("magenta")));
    }
    public Command orange() {
        return Commands.runOnce(() -> setColor(COLORS.get("orange")));
    }
    public Command green() {
        return Commands.runOnce(() -> setColor(COLORS.get("green")));
    }
    public Command white() {
        return Commands.runOnce(() -> setColor(COLORS.get("white")));
    }    

    public Command blinkBlue() {
        int[] blue = COLORS.get("blue");
        return Commands.runOnce(() -> setStrobeAnimation(blue, BLINKING_FREQUENCY));
    }
    public Command blinkRed() {
        int[] red = COLORS.get("red");
        return Commands.runOnce(() -> setStrobeAnimation(red, BLINKING_FREQUENCY));
    }
    public Command blinkMagenta() {
        int[] magenta = COLORS.get("magenta");
        return Commands.runOnce(() -> setStrobeAnimation(magenta, BLINKING_FREQUENCY));
    }
    
    public void periodic() {
        
    }
}
