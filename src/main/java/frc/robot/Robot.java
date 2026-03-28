// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import static edu.wpi.first.units.Units.Seconds;

import com.ctre.phoenix6.HootAutoReplay;

import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import au.grapplerobotics.CanBridge;

import frc.robot.subsystems.LED;

public class Robot extends TimedRobot {
    private Command m_autonomousCommand;

    private final RobotContainer m_robotContainer;
    private final LED m_led;

    /* log and replay timestamp and joystick data */
    private final HootAutoReplay m_timeAndJoystickReplay = new HootAutoReplay()
        .withTimestampReplay()
        .withJoystickReplay();

    public Robot() {
        m_robotContainer = new RobotContainer();
        m_led = new LED();

        CanBridge.runTCP();
    }

    @Override
    public void robotPeriodic() {
        m_timeAndJoystickReplay.update();
        CommandScheduler.getInstance().run(); 

        HubTracker.Shift currentShift = HubTracker.getCurrentShift().get();
        double timeRemainingInCurrentShift = HubTracker.timeRemainingInCurrentShift().get().in(Seconds);
        Alliance autoWinner = HubTracker.getAutoWinner().get();
        Alliance autoLoser = (autoWinner == Alliance.Red) ? Alliance.Blue : Alliance.Red;

        // LED Commands
        if (currentShift == HubTracker.Shift.AUTO) {
            m_led.solidMagenta();
        } else if (currentShift == HubTracker.Shift.TRANSITION) {  
            if (timeRemainingInCurrentShift > 5) { // If <5 seconds left, start blinking to indicate the next shift
                m_led.solidMagenta();
            } else {
                m_led.blinkAlliance(autoLoser);
            }
        } else if (currentShift == HubTracker.Shift.SHIFT_1) {
            if (timeRemainingInCurrentShift > 5) {
                m_led.solidAlliance(autoLoser);
            } else {
                m_led.blinkAlliance(autoWinner);
            }
        } else if (currentShift == HubTracker.Shift.SHIFT_2) {
            if (timeRemainingInCurrentShift > 5) {
                m_led.solidAlliance(autoWinner);
            } else {
                m_led.blinkAlliance(autoLoser);
            }
        } else if (currentShift == HubTracker.Shift.SHIFT_3) {
            if (timeRemainingInCurrentShift > 5) {
                m_led.solidAlliance(autoLoser);
            } else {
                m_led.blinkAlliance(autoWinner);
            }
        } else if (currentShift == HubTracker.Shift.SHIFT_4) {
            if (timeRemainingInCurrentShift > 5) {
                m_led.solidAlliance(autoWinner);
            } else {
                m_led.blinkMagenta();
            }
        } else if (currentShift == HubTracker.Shift.ENDGAME) { // Blinking yellow faster as endgame progresses
            if (timeRemainingInCurrentShift > 20) {
                m_led.solidMagenta();
            } else if (timeRemainingInCurrentShift > 10) {
                m_led.blinkYellow(1);
            } else if (timeRemainingInCurrentShift > 5) {
                m_led.blinkYellow(2);
            } else {
                m_led.blinkYellow(4);
            }
        }
    }

    @Override
    public void disabledInit() {}

    @Override
    public void disabledPeriodic() {}

    @Override
    public void disabledExit() {}

    @Override
    public void autonomousInit() {
        CommandScheduler.getInstance().cancelAll();
        m_autonomousCommand = m_robotContainer.getAutonomousCommand();

        if (m_autonomousCommand != null) {
            CommandScheduler.getInstance().schedule(m_autonomousCommand);
        }
    }

    @Override
    public void autonomousPeriodic() {}

    @Override
    public void autonomousExit() {}

    @Override
    public void teleopInit() {
        if (m_autonomousCommand != null) {
            CommandScheduler.getInstance().cancel(m_autonomousCommand);
        }
    }

    @Override
    public void teleopPeriodic() {}

    @Override
    public void teleopExit() {}

    @Override
    public void testInit() {
        CommandScheduler.getInstance().cancelAll();
    }

    @Override
    public void testPeriodic() {}

    @Override
    public void testExit() {}

    @Override
    public void simulationPeriodic() {}
}
