// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;

import edu.wpi.first.wpilibj.motorcontrol.Talon;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

import java.util.function.BooleanSupplier;

import com.ctre.phoenix6.*;
import com.ctre.phoenix6.hardware.*;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import com.ctre.phoenix6.configs.TalonFXConfiguration;
import com.ctre.phoenix6.controls.MotionMagicVoltage;
import com.ctre.phoenix6.controls.MotionMagicExpoVoltage;

public class Turret extends SubsystemBase {
  public TalonFX yawMotor = new TalonFX(51);

  public Turret() {
    // in init function
    var talonFXConfigs = new TalonFXConfiguration();

    // set slot 0 gains
    var slot0Configs = talonFXConfigs.Slot0;
    slot0Configs.kS = 0.25; // Add 0.25 V output to overcome static friction
    slot0Configs.kV = 0.12; // A velocity target of 1 rps results in 0.12 V output
    slot0Configs.kA = 0.01; // An acceleration of 1 rps/s requires 0.01 V output
    slot0Configs.kP = 4.8; // A position error of 2.5 rotations results in 12 V output
    slot0Configs.kI = 0; // no output for integrated error
    slot0Configs.kD = 0.1; // A velocity error of 1 rps results in 0.1 V output

    // set Motion Magic settings
    var motionMagicConfigs = talonFXConfigs.MotionMagic;
    motionMagicConfigs.MotionMagicCruiseVelocity = 1; // Target cruise velocity of 80 rps
    motionMagicConfigs.MotionMagicAcceleration = 160; // Target acceleration of 160 rps/s (0.5 seconds)
    motionMagicConfigs.MotionMagicJerk = 1600; // Target jerk of 1600 rps/s/s (0.1 seconds)

    yawMotor.getConfigurator().apply(talonFXConfigs);
  }

  final MotionMagicVoltage motionMagicRequest = new MotionMagicVoltage(0);

  public double revToDeg(double rev) {
    return rev * 360;
  }
  public double degToRev(double deg) {
    return deg / 360;
  }

  public double getYaw() {
    double yawPosition = yawMotor.getPosition(false).getValueAsDouble();
    return revToDeg(yawPosition);
  }
  public static BooleanSupplier isYawRightAngle(double correctAngle, double currentAngle) {
    return () -> correctAngle == currentAngle;
  }
  public Command setYaw(/*double angle */) { // Angle in degrees in respect to pointing towards front of the robot
    //double correctAngle = degToRev(angle * (135/18));
    //return run(() -> { yawMotor.setPosition(0.7); });
    MotionMagicExpoVoltage pos = new MotionMagicExpoVoltage(7.67); // as of 1/23/26 7.67 is exactly 1 rotation of turret wheel (not motor)
    return run(() -> { yawMotor.setControl(pos);});
  
  }

  @Override
  public void periodic() {
    // This method will be called once per scheduler run
  }
}
