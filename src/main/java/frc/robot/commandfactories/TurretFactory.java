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

    private final ShuffleboardTab odometryTab = Shuffleboard.getTab("Odometry");
    double motorYawOffset;
    double odometryX;
    double odometryY;
    double odometryRot;

    public TurretFactory(CommandSwerveDrivetrain drivetrain, Turret turret, boolean isRedAlliance) {
        this.drivetrain = drivetrain;
        this.turret = turret;
        this.isRedAlliance = isRedAlliance;

        shuffleBoardInit();
    }

    private void shuffleBoardInit(){
        odometryTab.addNumber("Turret Motor Yaw Offset", () -> Math.round(motorYawOffset) * 1000.0 / 1000.0);

        odometryTab.addNumber("Turret odometry X", () -> Math.round(odometryX) * 1000.0 / 1000.0);
        odometryTab.addNumber("Turret odometry Y", () -> Math.round(odometryY) * 1000.0 / 1000.0);
        odometryTab.addNumber("Turret odometry Rot", () -> Math.round(odometryRot) * 1000.0 / 1000.0);

    }

    public Command aimTurretHub() { // Change to suppliers inside the parameters, might work
        odometryX = drivetrain.getState().Pose.getX();
        odometryY = drivetrain.getState().Pose.getY();
        odometryRot = drivetrain.getState().Pose.getRotation().getDegrees();

        System.out.println(odometryX);
        System.out.println(odometryY);
        System.out.println(odometryRot);
        
        motorYawOffset = turret.getMotorYawOffset(
                drivetrain.getState().Pose.getX(),
                drivetrain.getState().Pose.getY(),
                isRedAlliance
        );

        return Commands.run(() -> turret.setYaw(
            -(drivetrain.getState().Pose.getRotation().getDegrees()) 
            + motorYawOffset
        ), turret);//.finallyDo(() -> turret.setYawCommand(0));
    }
}
