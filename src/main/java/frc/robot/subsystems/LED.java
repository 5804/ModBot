package frc.robot.subsystems;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;

import com.ctre.phoenix.led.*;
import com.ctre.phoenix.led.CANdle.LEDStripType;
import com.ctre.phoenix.led.CANdle.VBatOutputMode;
import com.ctre.phoenix.led.ColorFlowAnimation.Direction;
import com.ctre.phoenix.led.LarsonAnimation.BounceMode;
import com.ctre.phoenix.led.TwinkleAnimation.TwinklePercent;
import com.ctre.phoenix.led.TwinkleOffAnimation.TwinkleOffPercent;

public class LED extends SubsystemBase {
    public LED() {
        CANdle candle = new CANdle(61);

        HashMap<string, int[]> COLORS = new HashMap<>();
        COLORS.put("red", {255,0,0});
        COLORS.put("blue", {0,0,255});
        COLORS.put("orange", {255,165,0});
        COLORS.put("green", {0,255,0});
        COLORS.put("white", {255,255,255});
        COLORS.put("off", {0,0,0});
    }
    
    public void setColor(int[] color) {
        candle.animate(null);

        candle.setLEDs(color[0], color[1], color[2]);
    }
    
    public Commands off() {
        return Commands.run(() -> setColor(COLORS.get("off")))
    }

    public Command red() {
        return Commands.run(() -> setColor(COLORS.get("red")))
                       .finallyDo(off());
    }
    public Command blue() {
        return Commands.run(() -> setColor(COLORS.get("blue")))
                       .finallyDo(off());
    }
    public Command orange() {
        return Commands.run(() -> setColor(COLORS.get("orange")))
                       .finallyDo(off());
    }
    public Command green() {
        return Commands.run(() -> setColor(COLORS.get("green")))
                       .finallyDo(off());
    }
    public Command white() {
        return Commands.run(() -> setColor(COLORS.get("white")))
                       .finallyDo(off());
    }    
}
