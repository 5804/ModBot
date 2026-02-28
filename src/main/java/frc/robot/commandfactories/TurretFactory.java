package frc.robot.commandfactories;

import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.wpilibj.shuffleboard.ShuffleboardTab;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import frc.robot.subsystems.CommandSwerveDrivetrain;
import frc.robot.subsystems.Turret;

public class TurretFactory {
    CommandSwerveDrivetrain drivetrain;
    Turret turret;
    boolean isRedAlliance;

    double motorYawOffset;
    double odometryX;
    double odometryY;
    double odometryRot;

    public TurretFactory(CommandSwerveDrivetrain drivetrain, Turret turret, boolean isRedAlliance) {
        this.drivetrain = drivetrain;
        this.turret = turret;
        this.isRedAlliance = isRedAlliance;

    }

    public Command aimTurretHub() { // Change to suppliers inside the parameters, might work
        return Commands.run(() -> turret.setYaw(
            // The turret is continuously running the opposite of the robot rotation to point in a field-relative angle
            -(drivetrain.getState().Pose.getRotation().getDegrees() - 90) // Turret will start at -90 degrees to fit inside of frame perimeter
            // An offset angle is added to actually point in a specified direction towards the turret
            + turret.getMotorYawOffset(
                drivetrain.getState().Pose.getX(),
                drivetrain.getState().Pose.getY(),
                isRedAlliance
            )
        ), turret);
    }

    // Used for logging
    public double getMotorYawOffset() {
        return turret.getMotorYawOffset(
                drivetrain.getState().Pose.getX(),
                drivetrain.getState().Pose.getY(),
                isRedAlliance
        );
    }
}
