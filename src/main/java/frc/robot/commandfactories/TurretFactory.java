package frc.robot.commandfactories;

import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.subsystems.CommandSwerveDrivetrain;
import frc.robot.subsystems.Turret;

public class TurretFactory {
    CommandSwerveDrivetrain drivetrain;
    Turret turret;
    boolean isRedAlliance;

    public TurretFactory(CommandSwerveDrivetrain drivetrain, Turret turret, boolean isRedAlliance) {
        this.drivetrain = drivetrain;
        this.turret = turret;
        this.isRedAlliance = isRedAlliance;
    }

    public Command aimTurretHub() { // Change to suppliers inside the parameters, might work
        return turret.setYawCommand(
            -(drivetrain.getEstimatedPose().getRotation().getDegrees()) 
            + turret.getMotorYawOffset(
                drivetrain.getEstimatedPose().getX(), 
                drivetrain.getEstimatedPose().getY(), 
                isRedAlliance
            )
        );//.finallyDo(() -> turret.setYawCommand(0));
    }
}
