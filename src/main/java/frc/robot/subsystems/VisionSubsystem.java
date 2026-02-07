// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;

import java.lang.reflect.Array;

import edu.wpi.first.apriltag.AprilTagFieldLayout;
import edu.wpi.first.math.VecBuilder;
import edu.wpi.first.math.estimator.DifferentialDrivePoseEstimator;
import edu.wpi.first.math.estimator.PoseEstimator;
import edu.wpi.first.math.estimator.SwerveDrivePoseEstimator;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.kinematics.SwerveModulePosition;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import java.nio.file.Path;

import frc.robot.LimelightHelpers;
import frc.robot.RobotContainer;
import frc.robot.LimelightHelpers.LimelightResults;
import frc.robot.LimelightHelpers.PoseEstimate;
import frc.robot.subsystems.CommandSwerveDrivetrain;

public class VisionSubsystem extends SubsystemBase {
  /** Creates a new VisionSubsystem. */
  public SwerveDrivePoseEstimator poseEstimator;
  String limelightName201 = "one";

  public VisionSubsystem() {
    // LimelightHelpers.setCameraPose_RobotSpace("200", 0.325, 0.535, 0.405, 0, 0, 0); // Camera 200
    LimelightHelpers.setCameraPose_RobotSpace(limelightName201, -0.24, 0.535, 0.405, 0, 0, 90); // Camera 201
    // LimelightHelpers.setCameraPose_RobotSpace("202", -0.325, -0.115, 0.405, 0, 0, 180); // Camera 202
    // LimelightHelpers.setCameraPose_RobotSpace("203", 0.24, -0.115, 0.405, 0, 0, -90); // Camera 203

    // LimelightHelpers.setPipelineIndex("200", 0);
    LimelightHelpers.setPipelineIndex(limelightName201, 0);
    // LimelightHelpers.setPipelineIndex("202", 0);
    // LimelightHelpers.setPipelineIndex("203", 0);

    Path aprilTagFieldLayoutFilePath = Path.of("/home/lvuser/deploy/aprilTag/N108.json");

    try {
      this.aprilTagLayout = new AprilTagFieldLayout(aprilTagFieldLayoutFilePath);
    } catch (Exception e){
      System.out.println("FILE LOAD FAILED!");
    }
    
    this.poseEstimator = RobotContainer.drivetrain.m_poseEstimator;
  }

  AprilTagFieldLayout aprilTagLayout;


  SwerveModulePosition[] swerveModulePositions = new SwerveModulePosition[] {
            DriveSubsystem.m_frontLeft.getPosition(),
            DriveSubsystem.m_frontRight.getPosition(),
            DriveSubsystem.m_rearLeft.getPosition(),
            DriveSubsystem.m_rearRight.getPosition()
   };

  private static final String[] LIMELIGHTS = {"200", "201", "202", "203"};

  @Override
  public void periodic() {
    // for (String name : LIMELIGHTS) {
      LimelightHelpers.SetRobotOrientation(limelightName201, poseEstimator.getEstimatedPosition().getRotation().getDegrees(), 0, 0, 0, 0, 0);
      LimelightHelpers.PoseEstimate poseEstimate = LimelightHelpers.getBotPoseEstimate_wpiRed_MegaTag2(limelightName201);
      // if our angular velocity is greater than 360 degrees per second, ignore vision updates
      boolean doRejectUpdate = false;
      if(Math.abs(DriveSubsystem.m_gyro.getRate()) > 360)
      {
        doRejectUpdate = true;
      }
      if(poseEstimate.tagCount == 0)
      {
        doRejectUpdate = true;
      }
      if(!doRejectUpdate)
      {
        poseEstimator.setVisionMeasurementStdDevs(VecBuilder.fill(.7,.7,9999999));
        poseEstimator.addVisionMeasurement(
            poseEstimate.pose,
            poseEstimate.timestampSeconds);
      }
    // }

  }

  public Pose2d getEstimatedRobotPosition() {
    return poseEstimator.getEstimatedPosition();
  }

}
