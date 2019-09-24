package org.usfirst.frc.team3313.robot;

import edu.wpi.first.wpilibj.Talon;

public class TankDrive {
	private Talon t1;
	private Talon t2;

	public TankDrive(Talon left, Talon right) {
		t1 = left;
		t2 = right;
	}

	public void tankDrive(double speedLeft, double speedRight) {
		t1.set(speedLeft);
		t2.set(speedRight);
	}
}
