package frc.robot.subsystems;

import edu.wpi.first.wpilibj2.command.SubsystemBase;
import edu.wpi.first.units.measure.Frequency;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import edu.wpi.first.wpilibj.util.Color;
import edu.wpi.first.wpilibj.util.Color8Bit;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import java.util.*;

import com.ctre.phoenix6.controls.SolidColor;
import com.ctre.phoenix6.hardware.CANdle;
import com.ctre.phoenix6.signals.RGBWColor;
import com.ctre.phoenix6.controls.StrobeAnimation;
 
public class LED extends SubsystemBase {
    HashMap<String, int[]> COLORS;
    private final CANdle candle;
    final int NUM_LEDS;
    final double BLINKING_FREQUENCY;
    
    public LED() {
        candle = new CANdle(61);
        NUM_LEDS = 8;
        BLINKING_FREQUENCY = 2.5; // Hz (amount of times turned on per second)
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
    
    public Command off() {
        return Commands.runOnce(() -> {
            setColor(Color.kBlack);
            setStrobeAnimation(Color.kBlack, 0);
        });
    }
    
    public Command solidAlliance(Alliance alliance) {
        switch (alliance) {
            case Red:
                return Commands.runOnce(() -> setColor(Color.kRed));
            case Blue:
                return Commands.runOnce(() -> setColor(Color.kBlue));
            default:
                return Commands.runOnce(() -> {});
        }
    }
    public Command solidMagenta() {
        return Commands.runOnce(() -> setColor(Color.kMagenta));
    } 

    public Command blinkAlliance(Alliance alliance) {
        switch (alliance) {
            case Red:
                return Commands.runOnce(() -> setStrobeAnimation(Color.kRed, BLINKING_FREQUENCY));
            case Blue:
                return Commands.runOnce(() -> setStrobeAnimation(Color.kBlue, BLINKING_FREQUENCY));
            default:
                return Commands.runOnce(() -> {});
        }
    }
    public Command blinkMagenta() {
        return Commands.runOnce(() -> setStrobeAnimation(Color.kMagenta, BLINKING_FREQUENCY));
    }
    public Command blinkYellow(double frequency) {
        return Commands.runOnce(() -> setStrobeAnimation(Color.kYellow, frequency));
    }
    
    public void periodic() {
        
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
