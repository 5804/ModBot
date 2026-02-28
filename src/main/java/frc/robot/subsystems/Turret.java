package frc.robot.subsystems;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.filter.SlewRateLimiter;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.trajectory.TrapezoidProfile;
import edu.wpi.first.wpilibj.motorcontrol.Talon;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

import java.util.function.BooleanSupplier;

import com.ctre.phoenix6.*;
import com.ctre.phoenix6.hardware.*;
import com.ctre.phoenix6.signals.InvertedValue;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import com.ctre.phoenix6.configs.TalonFXConfiguration;
import com.ctre.phoenix6.controls.MotionMagicVoltage;
import com.ctre.phoenix6.controls.MotionMagicExpoVoltage;
import edu.wpi.first.math.trajectory.TrapezoidProfile;

public class Turret extends SubsystemBase {
  public TalonFX yawMotor = new TalonFX(51);

  public Turret() {
    // in init function
    var talonFXConfigs = new TalonFXConfiguration();

    talonFXConfigs.MotorOutput.Inverted = InvertedValue.Clockwise_Positive;

    // set slot 0 gains
    var slot0Configs = talonFXConfigs.Slot0;

    slot0Configs.kP = 4;
    slot0Configs.kI = 0;
    slot0Configs.kD = 0.2;

    slot0Configs.kS = 0.2;
    slot0Configs.kV = 0.117 / (2 * Math.PI);
    slot0Configs.kA = 0;

    // set Motion Magic settings
    var motionMagicConfigs = talonFXConfigs.MotionMagic;
    motionMagicConfigs.MotionMagicCruiseVelocity = 100; // Target cruise velocity of 80 rps
    motionMagicConfigs.MotionMagicAcceleration = 1000; // Target acceleration of 160 rps/s (0.5 seconds)
    motionMagicConfigs.MotionMagicJerk = 10000; // Target jerk of 1600 rps/s/s (0.1 seconds)

    yawMotor.getConfigurator().apply(talonFXConfigs);
    
  }


  final MotionMagicVoltage motionMagicRequest = new MotionMagicVoltage(0);
  private static final double YAW_DEADBAND_DEG = 0;
  private double lastCommandedYaw = 0.0;
  
  // Rotations of kraken for one rotation of turret
  private double TURRET_GEAR_RATIO = 6.4; // 7.67 for 3d printed turret, 6.4 for comp turret

  public double revToDeg(double rev) {
    return rev * 360;
  }
  public double degToRev(double deg) {
    return deg / 360;
  }

  public double getTurretYaw() {
    double yawPosition = yawMotor.getPosition(false).getValueAsDouble();
    return revToDeg(yawPosition);
  }

  public double getMotorYawOffset(double robotX, double robotY, boolean isRedAlliance) {
    double pointTowardsX; // Meters
    double pointTowardsY = 4.034663;

    if (isRedAlliance) {
      pointTowardsX = 4.625594; // Red
    } else {
      pointTowardsX = 11.915394; // Blue
    }


    double robotOffsetX = pointTowardsX - robotX;
    double robotOffsetY = pointTowardsY - robotY;

    double turretYawOffsetRad = Math.atan2(robotOffsetY, robotOffsetX);
    double turretYawOffsetDeg = Math.toDegrees(turretYawOffsetRad);
    double motorYawOffset = turretYawOffsetDeg; 
    // System.out.println("Motor:" + motorYawOffset);
    // System.out.println("X: " + robotOffsetX);
    // System.out.println("Y: " + robotOffsetY);

    return motorYawOffset;
  }

  public static BooleanSupplier isYawRightAngle(double correctAngle, double currentAngle) {
    return () -> correctAngle == currentAngle;
  }
  // double kVel = 10.0;
  // double kAcc = 10.0;
  // private final SlewRateLimiter yawLimiter = new SlewRateLimiter(240); // deg/sec

  private double applyDeadband(double targetDeg) {
    if (Math.abs(targetDeg - lastCommandedYaw) < YAW_DEADBAND_DEG) {
        return lastCommandedYaw;
    }
    lastCommandedYaw = targetDeg;
    return targetDeg;
  }
  private double normalizeAngle(double deg) {
    return MathUtil.inputModulus(deg, -180.0, 180.0);
  }

  public void setYaw(double angleDeg) { // Angle in degrees in respect to pointing towards front of the robot
    
    double targetAngle = TURRET_GEAR_RATIO * degToRev(normalizeAngle(angleDeg));

    // edu.wpi.first.math.trajectory.TrapezoidProfile.Constraints  max = new edu.wpi.first.math.trajectory.TrapezoidProfile.Constraints(kVel, kAcc);
    // TrapezoidProfile limit = new TrapezoidProfile(max);
    // edu.wpi.first.math.trajectory.TrapezoidProfile.State current = new edu.wpi.first.math.trajectory.TrapezoidProfile.State(angleDeg, kVel);
    // edu.wpi.first.math.trajectory.TrapezoidProfile.State targState = new edu.wpi.first.math.trajectory.TrapezoidProfile.State(targetAngle, kVel);
    // double time = limit.timeLeftUntil(0.0);
    // System.out.println("Current position and Veocity: " + limit.calculate(time, current, targState));


    //pos = limit.calculate(time,current,targState); //I don't know how to fix this as of 1/30
 
    yawMotor.setControl(new MotionMagicExpoVoltage(targetAngle));
    // System.out.println("Target Angle in Rev: "+targetAngle);
  }

  public Command setYawCommand(double angleDeg) {
    double normalized = normalizeAngle(angleDeg);

    double targetAngle = TURRET_GEAR_RATIO * degToRev(normalized);
    
    return run(() -> { yawMotor.setControl(new MotionMagicExpoVoltage(targetAngle)); });
  }

public Command turretClockwise(double speed) {
  return run(() -> {  } );
}

public Command turretCounterClockwise(double speed) {
  return run(() -> {  } );
}

  @Override
  public void periodic() {
    // This method will be called once per scheduler run
  }
}