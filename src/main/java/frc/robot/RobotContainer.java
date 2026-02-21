// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import static edu.wpi.first.units.Units.*;

import java.util.Optional;

import com.ctre.phoenix6.swerve.SwerveModule.DriveRequestType;
import com.ctre.phoenix6.swerve.SwerveRequest;
import com.pathplanner.lib.auto.NamedCommands;
import com.pathplanner.lib.commands.PathPlannerAuto;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.wpilibj.shuffleboard.ShuffleboardTab;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.RunCommand;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;
import edu.wpi.first.wpilibj2.command.button.RobotModeTriggers;
import edu.wpi.first.wpilibj2.command.sysid.SysIdRoutine.Direction;

import frc.robot.generated.TunerConstants;
import frc.robot.subsystems.CommandSwerveDrivetrain;
import frc.robot.subsystems.Turret;

public class RobotContainer {

    private double maxSpeed = 1.0 * TunerConstants.kSpeedAt12Volts.in(MetersPerSecond); // kSpeedAt12Volts desired top speed
    private double maxAngularRate = RotationsPerSecond.of(0.75).in(RadiansPerSecond); // 3/4 of a rotation per second max angular velocity
    private double driveDeadband = 0.14;
    private double angleDeadband = 0.14;
    private Optional<Alliance> currentAlliance = DriverStation.getAlliance();
    public boolean isRedAlliance = (currentAlliance.isPresent() && (currentAlliance.get().equals(Alliance.Red))); 
    public boolean turretAutoLock = false;

    private final SwerveRequest.FieldCentric drive = new SwerveRequest.FieldCentric()
            .withDeadband(driveDeadband).withRotationalDeadband(angleDeadband) // Add a 10% deadband
            .withDriveRequestType(DriveRequestType.OpenLoopVoltage); // Use open-loop control for drive motors

    private final SwerveRequest.RobotCentric roboDrive = new SwerveRequest.RobotCentric()
        .withDeadband(driveDeadband).withRotationalDeadband(angleDeadband)
        .withDriveRequestType(DriveRequestType.OpenLoopVoltage);

    private final Telemetry logger = new Telemetry(maxSpeed);

    private final CommandXboxController joystick = new CommandXboxController(0);
    public static final CommandSwerveDrivetrain drivetrain = TunerConstants.createDrivetrain();
    public static final Turret turret = new Turret();

    private final SendableChooser<Command> autoChooser = new SendableChooser<>();
    private ShuffleboardTab tab1 = Shuffleboard.getTab("Tab1");

    public RobotContainer() {
        configureBindings();

        NamedCommands.registerCommand("Drive 1 meter", oneMeterAuto());

        autoChooser.setDefaultOption("Default Auto", oneMeterAuto());
        autoChooser.addOption("Drive 1 meter", oneMeterAuto());
        autoChooser.addOption("Turn 90 degrees", turnAuto());
        autoChooser.addOption("Swerve Test", swerveTestAuto());
        autoChooser.addOption("Vision Test", visionTestAuto());
        autoChooser.addOption("singlePos", singlePos());

        SmartDashboard.putData("Auto choices", autoChooser);
        tab1.add("Auto Chooser", autoChooser);

        // LimelightHelpers.setCameraPose_RobotSpace("limelight-right", -0.24, 0.32, 0.41, 0, 0, -180); // WORKS FOR RED (9, 10)
        // LimelightHelpers.setCameraPose_RobotSpace("limelight-right", 0.24, -0.32, 0.41, 0, 0, 0);  // WORKS FOR RED (25, 26)

        LimelightHelpers.setCameraPose_RobotSpace("limelight-front", 0.32, -0.24, 0.41, 0, 0, 0);
        LimelightHelpers.setCameraPose_RobotSpace("limelight-right", 0.24, 0.32, 0.41, 0, 0, -90);
        LimelightHelpers.setCameraPose_RobotSpace("limelight-back", -0.32, 0.24, 0.41, 0, 0, 180);
        LimelightHelpers.setCameraPose_RobotSpace("limelight-left", -0.24, -0.32, 0.41, 0, 0, 90);

        LimelightHelpers.setPipelineIndex("limelight-front", 0);
        LimelightHelpers.setPipelineIndex("limelight-right", 0);
        LimelightHelpers.setPipelineIndex("limelight-back", 0);
        LimelightHelpers.setPipelineIndex("limelight-left", 0);

        // CommandSwerveDrivetrain.m_poseEstimator.resetPose(new Pose2d(2, 0, new Rotation2d(Math.PI/2)));
    }

    public Command aimTurretHub() { // Change to suppliers inside the parameters, might work
        return Commands.run(() -> { turret.setYaw(
            -(CommandSwerveDrivetrain.m_poseEstimator.getEstimatedPosition().getRotation().getDegrees()) 
            + turret.getMotorYawOffset(
                CommandSwerveDrivetrain.m_poseEstimator.getEstimatedPosition().getX(), 
                CommandSwerveDrivetrain.m_poseEstimator.getEstimatedPosition().getY(), 
                isRedAlliance
            )
        ); }, turret);
    }

    public Command aimTurretStop() {
        return Commands.run(() -> { turret.setYaw(0); }, turret);
    }

    private void configureBindings() {
        drivetrain.setDefaultCommand(
            drivetrain.applyRequest(() ->
                drive.withVelocityX(MathUtil.applyDeadband(joystick.getLeftY(), driveDeadband) * -1 * maxSpeed) // Drive forward with negative Y (forward)
                    .withVelocityY(MathUtil.applyDeadband(joystick.getLeftX(), driveDeadband) * -1 * maxSpeed) // Drive left with negative X (left)
                    .withRotationalRate(MathUtil.applyDeadband(joystick.getRightX(), angleDeadband) * -1 * maxAngularRate) // Drive counterclockwise with negative X (left)
            )
        );

        joystick.a().whileTrue(orientToAprilTag());

        // Idle while the robot is disabled. This ensures the configured
        // neutral mode is applied to the drive motors while disabled.
        final var idle = new SwerveRequest.Idle();
        RobotModeTriggers.disabled().whileTrue(
            drivetrain.applyRequest(() -> idle).ignoringDisable(true)
        );

        // Run SysId routines when holding back/start and X/Y.
        // Note that each routine should be run exactly once in a single log.
        if (DriverStation.isTest()) {
            joystick.back().and(joystick.y()).whileTrue(drivetrain.sysIdDynamic(Direction.kForward));
            joystick.back().and(joystick.x()).whileTrue(drivetrain.sysIdDynamic(Direction.kReverse));
            joystick.start().and(joystick.y()).whileTrue(drivetrain.sysIdQuasistatic(Direction.kForward));
            joystick.start().and(joystick.x()).whileTrue(drivetrain.sysIdQuasistatic(Direction.kReverse));
        }

        // Reset the field-centric heading on left bumper press.
        joystick.leftBumper().onTrue(drivetrain.runOnce(drivetrain::seedFieldCentric));
        drivetrain.registerTelemetry(logger::telemeterize);

        // joystick.rightTrigger().whileTrue(turret.turretClockwise(.5));
        // joystick.leftTrigger().whileTrue(turret.turretCounterClockwise(.5));
        joystick.a().onTrue(turret.setYawCommand(180));
        joystick.x().onTrue(turret.setYawCommand(90));
        joystick.b().onTrue(turret.setYawCommand(-90));
        joystick.y().onTrue(turret.setYawCommand(0));

        joystick.povUp().onTrue(aimTurretHub());
        joystick.povDown().onTrue(aimTurretStop());
    }

    private double limelight_aim_proportional()
    {    
        // kP (constant of proportionality)
        // this is a hand-tuned number that determines the aggressiveness of our proportional control loop
        // if it is too high, the robot will oscillate.
        // if it is too low, the robot will never reach its target
        // if the robot never turns in the correct direction, kP should be inverted.
        double kP = .035;

        // tx ranges from (-hfov/2) to (hfov/2) in degrees. If your target is on the rightmost edge of 
        // your limelight 3 feed, tx should return roughly 31 degrees.
        double targetingAngularVelocity = LimelightHelpers.getTX("limelight-right") * kP;

        // convert to radians per second for our drive method
        targetingAngularVelocity *= maxAngularRate;

        //invert since tx is positive when the target is to the right of the crosshair
        targetingAngularVelocity *= -1.0;

        return targetingAngularVelocity;
    }

    // simple proportional ranging control with Limelight's "ty" value
    // this works best if your Limelight's mount height and target mount height are different.
    // if your limelight and target are mounted at the same or similar heights, use "ta" (area) for target ranging rather than "ty"
    double limelight_range_proportional()
    {    
        double kP = .1;
        double targetingForwardSpeed = LimelightHelpers.getTA("limelight-right") * kP;
        targetingForwardSpeed *= maxSpeed;
        // targetingForwardSpeed *= -1.0;
        return targetingForwardSpeed;
    }

    private Command orientToAprilTag() {
        String limelightName = "limelight-right";

        double calc_x = limelight_range_proportional();
        double calc_y = MathUtil.applyDeadband(joystick.getLeftX(), driveDeadband) * -1 * maxSpeed;
        double calc_r = limelight_aim_proportional();

        System.out.println(limelightName+": "+calc_x+", "+calc_y+", "+calc_r);

        return drivetrain.applyRequest(() ->
                roboDrive.withVelocityX(limelight_range_proportional()) 
                 .withVelocityY(MathUtil.applyDeadband(joystick.getLeftX(), driveDeadband) * -1 * maxSpeed)
                 .withRotationalRate(limelight_aim_proportional())
        );
    }

    public void printLimeLightData() {
        String limelightName = "limelight-right";
        double tx = LimelightHelpers.getTX(limelightName);
        double ty = LimelightHelpers.getTY(limelightName);
        double ta = LimelightHelpers.getTA(limelightName);

        System.out.println(limelightName+": "+tx+", "+ty+", "+ta+" ,");
    }

    public Command getAutonomousCommand() {
        return autoChooser.getSelected();
    }

    public CommandSwerveDrivetrain getDrivetrain(){
        return RobotContainer.drivetrain;
    }

    public Command oneMeterAuto() {
        return new PathPlannerAuto("OneMeterAuto");
    }

    public Command turnAuto() {
        return new PathPlannerAuto("90DegreeAuto");
    }

    public Command swerveTestAuto() {
        return new PathPlannerAuto("SwerveTestAuto");
    }
    public Command visionTestAuto() {
        return new PathPlannerAuto("VisionTestAuto");
    }

    public Command singlePos() {
        return new PathPlannerAuto("singlePos");
    }
}
