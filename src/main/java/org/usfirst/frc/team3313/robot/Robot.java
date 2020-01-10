package org.usfirst.frc.team3313.robot;

import com.revrobotics.ColorMatch;
import com.revrobotics.ColorMatchResult;
import com.revrobotics.ColorSensorV3;

import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.wpilibj.I2C;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Talon;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.wpilibj.shuffleboard.ShuffleboardTab;
import edu.wpi.first.wpilibj.util.Color;

//Import properly this time 
/**
 * @author logan The VM is configured to automatically run this class, and to
 *         call the functions corresponding to each mode, as described in the
 *         IterativeRobot documentation. If you change the name of this class or
 *         the package after creating this project, you must also update the
 *         manifest file in the resource directory.
 */
public class Robot extends TimedRobot {
	// Accelerated Movement Configurations
	private static final double DEFAULT_MOVEMENT_SPEED = 4; // Default speed multiplier, configured on Shuffleboard
	private static final double DEFAULT_TURN_SPEED = 1.5; // Default turn multiplier, configured on Shuffleboard

	// Tank Drive, see TankDrive.java
	TankDrive drive = new TankDrive(new Talon(1), new Talon(0));

	// Create motor controllers for ball shooting mechanism
	Talon Shooter = new Talon(2);
	Talon Feeder = new Talon(3);
	Joystick joy1 = new Joystick(0);

	// Shuffleboard configurations
	ShuffleboardTab tab = Shuffleboard.getTab("Drive");
	NetworkTableEntry SBmaxSpeed = tab.add("Drive Speed", DEFAULT_MOVEMENT_SPEED).getEntry();
	NetworkTableEntry SBmaxTurn = tab.add("Turn Speed", DEFAULT_TURN_SPEED).getEntry();
	NetworkTableEntry SBRed = tab.add("Red", 0).getEntry();
	NetworkTableEntry SBGreen = tab.add("Green", 0).getEntry();
	NetworkTableEntry SBBlue = tab.add("Blue", 0).getEntry();
	NetworkTableEntry SBColor = tab.add("Detected Color", "Unknown").getEntry();
	NetworkTableEntry SBConfidence = tab.add("Confidence", 0).getEntry();

	// Color Sensor
	I2C.Port i2cPort = I2C.Port.kOnboard;
	ColorSensorV3 m_colorSensor = new ColorSensorV3(i2cPort);

	private final ColorMatch colorMatcher = new ColorMatch();

	private final Color matchBlueTarget = ColorMatch.makeColor(0.143, 0.427, 0.429);
	private final Color matchGreenTarget = ColorMatch.makeColor(0.160, 0.581, 0.258);
	private final Color matchRedTarget = ColorMatch.makeColor(0.561, 0.232, 0.114);
	private final Color matchYellowTarget = ColorMatch.makeColor(0.316, 0.562, 0.121);

	// Accelerated Movement
	double incrementSpeed = 0; // DO NOT TOUCH
	int currentSpeed = 0; // DO NOT TOUCH
	int noMovement = 0; // DO NOT TOUCH
	int ticksToWaitAfterNoMovement = 40;
	int ticksTillFullSpeed = 7; // 20 ~= 1 sec

	boolean respectMax = true;

	/**
	 * This function is run when the robot is first started up and should be used
	 * for any initialization code.
	 */

	@Override
	public void robotInit() {
		// CameraServer.getInstance().startAutomaticCapture();
		// CameraServer.getInstance().startAutomaticCapture();

		// Accelerated Movement DEFAULTS DO NOT ERASE/UNCOMMENT
		// double incrementSpeed = 0; //DO NOT TOUCH
		// int currentSpeed = 0; //DO NOT TOUCH
		// int noMovement = 0; //DO NOT TOUCH
		// int ticksToWaitAfterNoMovement = 40;
		// int ticksTillFullSpeed = 7; //20 ~= 1 sec
		// boolean respectMax = true; //Whether or not to respect full movement of
		// joystick or not, meaning
		// End //max movement on joystick is the same as the maximum speed versus
		// deadzone
		colorMatcher.addColorMatch(matchRedTarget);
		colorMatcher.addColorMatch(matchGreenTarget);
		colorMatcher.addColorMatch(matchBlueTarget);
	}

	/**
	 * This autonomous (along with the chooser code above) shows how to select
	 * between different autonomous modes using the dashboard. The sendable chooser
	 * code works with the Java SmartDashboard. If you prefer the LabVIEW Dashboard,
	 * remove all of the chooser code and uncomment the getString line to get the
	 * auto name from the text box below the Gyro
	 *
	 * You can add additional auto modes by adding additional comparisons to the
	 * switch structure below with additional strings. If using the SendableChooser
	 * make sure to add them to the chooser code above as well.
	 */
	@Override
	public void autonomousInit() {
		// Do we even want an auton for the demo robot?? If so what should it do?
		// autoSelected = SmartDashboard.getString("Auto Selector",
		// defaultAuto);
	}

	@Override
	public void robotPeriodic() {
		Color detectedColor = m_colorSensor.getColor();

		String colorString;
		ColorMatchResult match = colorMatcher.matchClosestColor(detectedColor);

		if (match.color == matchBlueTarget) {
			colorString = "Blue";
		} else if (match.color == matchRedTarget) {
			colorString = "Red";
		} else if (match.color == matchGreenTarget) {
			colorString = "Green";
		} else if (match.color == matchYellowTarget) {
			colorString = "Yellow";
		} else {
			colorString = "Unknown";
		}

		SBRed.setDouble(detectedColor.red);
		SBGreen.setDouble(detectedColor.green);
		SBBlue.setDouble(detectedColor.blue);
		SBColor.setString(colorString);
		SBConfidence.setDouble(match.confidence);
	}

	/**
	 * This function is called periodically during autonomous
	 */
	@Override
	public void autonomousPeriodic() {
	}

	/**
	 * This function is called periodically during operator control
	 */
	@Override
	public void teleopPeriodic() {
		// Color Sensor

		// Drive
		// advancedDrive(joy1.getX(), joy1.getRawAxis(5));

		// Shooter
		// Button/Power directory: Right Bumper=100% Power (add as needed for minimal
		// confusion)

		// Shooter
		if (joy1.getRawButton(6)) {
			Shooter.set(1);
		} else {
			Shooter.set(0);
		}
		// Feed Double
		if (joy1.getRawButton(5)) {
			Feeder.set(1);
		} else {
			Feeder.set(0);
		}
	}

	/**
	 * This function is called periodically during test mode
	 */
	@Override
	public void testPeriodic() {
	}

	private void advancedDrive(double rightStick, double leftStick) {
		double movementSpeedMultiplier = SBmaxSpeed.getDouble(DEFAULT_MOVEMENT_SPEED);

		// rightStick uses Y axis, leftStick uses rawAxis(5)
		if (rightStick == 0 && leftStick == 0) {
			if (noMovement == ticksToWaitAfterNoMovement) {
				currentSpeed = 0; // Reset the speed when no movement
				noMovement = 0;
			} else {
				noMovement++;
			}
			return;
		}

		rightStick = rightStick * SBmaxTurn.getDouble(DEFAULT_TURN_SPEED); // Multiply by configured turn speed
		if (respectMax) {
			// if (controller.getRawButton(5)) { // Ignore the advanced drive
			// drive.tankDrive(-(controller.getY() / 1.25) + (-controller.getRawAxis(5) /
			// 2),
			// (-controller.getY() / 1.25) + -(-controller.getRawAxis(5) / 2));
			// }

			if (currentSpeed != ticksTillFullSpeed) {
				currentSpeed++; // Calculate the next tick speed based off maxSpeed / ticksTillFullSpeed
				if (rightStick <= (incrementSpeed * currentSpeed)) {
					drive.tankDrive(rightStick + (-leftStick * movementSpeedMultiplier),
							rightStick + (leftStick * movementSpeedMultiplier));
				} else {
					drive.tankDrive((incrementSpeed * currentSpeed) + (-leftStick * movementSpeedMultiplier),
							(incrementSpeed * currentSpeed) + (leftStick * movementSpeedMultiplier));
				}
			} else {
				drive.tankDrive(rightStick + (-leftStick * movementSpeedMultiplier),
						rightStick + (leftStick * movementSpeedMultiplier));
			}
			// double acclerationValue = (respectedValue / ticksTillFullSpeed) *
			// currentSpeed;
		}
	}
}
