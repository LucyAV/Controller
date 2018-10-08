import ch.aplu.xboxcontroller.*;

import javax.swing.JOptionPane;

public class DriveController {

    // Servo fixed variables
    private final int SERVO_CENTER = 140;
    private final double SERVO_MULTIPLIER = 20;
    private final double SERVO_THUMB_DEAD_ZONE = 0.4;


    // Motor fixed variables
    private final int MOTOR_STOP = 150;
    private final int MOTOR_BRAKE_HARD = 130;
    private final double MOTOR_TRIGGER_DEAD_ZONE = 0.2;

    private final int MOTOR_BACKWARDS_MIN = 139;
    private final int MOTOR_BACKWARDS_MAX = 135;
    private final int MOTOR_BACKWARDS_MULTIPLIER = MOTOR_BACKWARDS_MAX - MOTOR_BACKWARDS_MIN - 1;

    private final int MOTOR_FORWARDS_MIN = 159;
    private final int MOTOR_FORWARDS_MAX = 174;
    private final int MOTOR_FORWARDS_MULTIPLIER = MOTOR_FORWARDS_MAX - MOTOR_FORWARDS_MIN;


    // Connection to Xbox Controller and Raspberry Pi (via Network)
    private XboxController xboxController;
    private NetworkConnector networkConnector = null;


    // Controller input variables
    private double leftStickDirection;
    private double leftStickMagnitude;
    private double leftTriggerValue;
    private double rightTriggerValue;
    private boolean leftShoulderButton = false;


    public DriveController() {
        // Initialize controller and its input dead zones
        xboxController = new XboxController(10, 10);
        xboxController.setLeftThumbDeadZone(SERVO_THUMB_DEAD_ZONE);
        xboxController.setLeftTriggerDeadZone(MOTOR_TRIGGER_DEAD_ZONE);
        xboxController.setRightTriggerDeadZone(MOTOR_TRIGGER_DEAD_ZONE);

        // Display error if no controller is connected
        if (!xboxController.isConnected()) {
            JOptionPane.showMessageDialog(null, "Xbox controller not connected.",
                    "Fatal error", JOptionPane.ERROR_MESSAGE);
            // Stop the connection to the controller and the Raspberry Pi
            xboxController.release();
            System.exit(1);
        }

        // Initialize network connection to Raspberry Pi
        networkConnector = new NetworkConnector();
        networkConnector.motorValue = MOTOR_STOP;
        networkConnector.servoValue = SERVO_CENTER;
        networkConnector.start();

        // Add all listeners and their functions to the controller
        xboxController.addXboxControllerListener(new XboxControllerAdapter() {
            @Override
            public void leftThumbDirection(double direction) {
                super.leftThumbDirection(direction);

                leftStickDirection = direction;
                updateServo();
            }

            @Override
            public void leftThumbMagnitude(double magnitude) {
                super.leftThumbMagnitude(magnitude);

                leftStickMagnitude = magnitude;
                updateServo();
            }

            @Override
            public void leftTrigger(double value) {
                super.leftTrigger(value);

                leftTriggerValue = value;
                updateMotor();
            }

            @Override
            public void rightTrigger(double value) {
                super.rightTrigger(value);

                rightTriggerValue = value;
                updateMotor();
            }

            @Override
            public void leftShoulder(boolean pressed) {
                super.leftShoulder(pressed);

                leftShoulderButton = pressed;
                updateMotor();
            }

            // For safety reasons, stop the software if the controller disconnects.
            // The vehicle will stop in this case, just as if the network connection was lost.
            @Override
            public void isConnected(boolean connected) {
                super.isConnected(connected);

                if (!connected) {
                    networkConnector.close();
                    xboxController.release();
                    System.exit(1);
                }
            }
        });

        // Display running message if everything works
        JOptionPane.showMessageDialog(null, "Xbox controller connected.\n" +
                        "Press left or right trigger, Ok to quit.", "DriveController",
                JOptionPane.PLAIN_MESSAGE);
        // Stop the connection to the controller and the Raspberry Pi on exit
        networkConnector.close();
        xboxController.release();
        System.exit(1);
    }

    // Update the value for the servo
    private void updateServo() {
        if (leftStickMagnitude == 0.0) {
            networkConnector.setServoValue(SERVO_CENTER);
        } else {
            if (leftStickDirection > 0 && leftStickDirection < 180) {
                networkConnector.setServoValue( SERVO_CENTER + ( (int) (leftStickMagnitude * SERVO_MULTIPLIER) ) );
            } else {
                networkConnector.setServoValue( SERVO_CENTER + ( (int) (leftStickMagnitude * -SERVO_MULTIPLIER) ) );
            }
        }
    }

    // Update the value for the motor
    private void updateMotor() {
        if (leftShoulderButton) {
            networkConnector.setMotorValue(MOTOR_BRAKE_HARD);
        } else if (leftTriggerValue > 0.0) {
            networkConnector.setMotorValue( (int) ( (MOTOR_BACKWARDS_MIN + 1) + (leftTriggerValue * MOTOR_BACKWARDS_MULTIPLIER) ) );
        } else if (rightTriggerValue > 0.0) {
            networkConnector.setMotorValue( (int) ( MOTOR_FORWARDS_MIN + (rightTriggerValue * MOTOR_FORWARDS_MULTIPLIER) ) );
        } else {
            networkConnector.setMotorValue(MOTOR_STOP);
        }
    }

    public static void main(String[] args) {
        new DriveController();
    }
}
