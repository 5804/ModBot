// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import static edu.wpi.first.units.Units.*;

import com.ctre.phoenix6.swerve.SwerveModule.DriveRequestType;
import com.ctre.phoenix6.swerve.SwerveRequest;
import com.pathplanner.lib.auto.NamedCommands;
import com.pathplanner.lib.commands.PathPlannerAuto;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.kinematics.Odometry;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.wpilibj.shuffleboard.ShuffleboardTab;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
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

    /* Setting up bindings for necessary control of the swerve drive platform */
    private final SwerveRequest.FieldCentric drive = new SwerveRequest.FieldCentric()
            .withDeadband(driveDeadband).withRotationalDeadband(angleDeadband) // Add a 10% deadband
            .withDriveRequestType(DriveRequestType.OpenLoopVoltage); // Use open-loop control for drive motors
    private final SwerveRequest.SwerveDriveBrake brake = new SwerveRequest.SwerveDriveBrake();
    private final SwerveRequest.PointWheelsAt point = new SwerveRequest.PointWheelsAt();

    private final Telemetry logger = new Telemetry(maxSpeed);

    private final CommandXboxController joystick = new CommandXboxController(0);

    public final CommandSwerveDrivetrain drivetrain = TunerConstants.createDrivetrain();
    public final Turret turret = new Turret();
    private final SendableChooser<Command> autoChooser = new SendableChooser<>();
        private ShuffleboardTab tab1 = Shuffleboard.getTab("Tab1");
        
    public double getOdometryRotation() {
        return drivetrain.getState().Pose.getRotation().getDegrees();
    }

    public RobotContainer() {
        configureBindings();

        NamedCommands.registerCommand("Drive 1 meter", oneMeterAuto());

        autoChooser.setDefaultOption("Default Auto", oneMeterAuto());
        autoChooser.addOption("Drive 1 meter", oneMeterAuto());
        autoChooser.addOption("Turn 90 degrees", turnAuto());
        autoChooser.addOption("Swerve Test", swerveTestAuto());

        SmartDashboard.putData("Auto choices", autoChooser);
        tab1.add("Auto Chooser", autoChooser);
    }

    private void configureBindings() {
        // Note that X is defined as forward according to WPILib convention,
        // and Y is defined as to the left according to WPILib convention.
        drivetrain.setDefaultCommand(
            // Drivetrain will execute this command periodically
            drivetrain.applyRequest(() ->
                drive.withVelocityX(-MathUtil.applyDeadband(joystick.getLeftY(), driveDeadband)*maxSpeed) // Drive forward with negative Y (forward)
                    .withVelocityY(-MathUtil.applyDeadband(joystick.getLeftX(), driveDeadband)*maxSpeed) // Drive left with negative X (left)
                    .withRotationalRate(-MathUtil.applyDeadband(joystick.getRightX(), angleDeadband)*maxAngularRate) // Drive counterclockwise with negative X (left)
            )
        );

        // Idle while the robot is disabled. This ensures the configured
        // neutral mode is applied to the drive motors while disabled.
        final var idle = new SwerveRequest.Idle();
        RobotModeTriggers.disabled().whileTrue(
            drivetrain.applyRequest(() -> idle).ignoringDisable(true)
        );

        // Run SysId routines when holding back/start and X/Y.
        // Note that each routine should be run exactly once in a single log.
        joystick.back().and(joystick.y()).whileTrue(drivetrain.sysIdDynamic(Direction.kForward));
        joystick.back().and(joystick.x()).whileTrue(drivetrain.sysIdDynamic(Direction.kReverse));
        joystick.start().and(joystick.y()).whileTrue(drivetrain.sysIdQuasistatic(Direction.kForward));
        joystick.start().and(joystick.x()).whileTrue(drivetrain.sysIdQuasistatic(Direction.kReverse));

        // Reset the field-centric heading on left bumper press.
        joystick.leftBumper().onTrue(drivetrain.runOnce(drivetrain::seedFieldCentric));

        joystick.a().onTrue(turret.setYawCommand(180));
        joystick.x().onTrue(turret.setYawCommand(90));
        joystick.b().onTrue(turret.setYawCommand(-90));
        joystick.y().onTrue(turret.setYawCommand(0));
        joystick.povUp().whileTrue(Commands.run(() -> turret.setYaw(-getOdometryRotation()), turret));

        drivetrain.registerTelemetry(logger::telemeterize);
    }

    public Command getAutonomousCommand() {
        return autoChooser.getSelected();
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
}
