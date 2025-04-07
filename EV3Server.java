package PackageDiRamon;

import lejos.hardware.motor.Motor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.Button;
import lejos.hardware.ev3.EV3;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import lejos.utility.Delay;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.DataInputStream;

public class EV3Server {

    private static EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(MotorPort.C);
    private static EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(MotorPort.B);

    public static void main(String[] args) {
        EV3 ev3 = LocalEV3.get();
        TextLCD lcd = ev3.getTextLCD();
       
        try (ServerSocket serverSocket = new ServerSocket(1234)) {
            lcd.drawString("Waiting for client...", 0, 4);
            Socket clientSocket = serverSocket.accept();
            lcd.drawString("Client connected!", 0, 5);
           
            DataInputStream dis = new DataInputStream(clientSocket.getInputStream());
            while (true) {
                int command = dis.readInt();
                int speed = dis.readInt(); // Read the speed value sent from the client
                if (command == -1) {
                    break;
                }
                executeCommand(command, speed);
            }
        } catch (IOException e) {
            lcd.drawString("Error: " + e.getMessage(), 0, 6);
        }

        leftMotor.close();
        rightMotor.close();
        lcd.clear();
        lcd.drawString("Server stopped", 0, 4);
    }

    private static void executeCommand(int command, int speed) {
        switch (command) {
            case 1: // Avanti
                leftMotor.setSpeed(speed);
                rightMotor.setSpeed(speed);
                leftMotor.forward();
                rightMotor.forward();
                break;
            case 2: // Indietro
                leftMotor.setSpeed(speed);
                rightMotor.setSpeed(speed);
                leftMotor.backward();
                rightMotor.backward();
                break;
            case 3: // Sinistra
            leftMotor.setSpeed(1100);
            rightMotor.setSpeed(450);
            leftMotor.forward();
            rightMotor.forward();
                break;
            case 4: // Destra
                leftMotor.setSpeed(450);
                rightMotor.setSpeed(1100);
                rightMotor.forward();
            leftMotor.forward();

                break;
            case 0: // Stop
                leftMotor.stop(true);
                rightMotor.stop(true);
                break;
        }
    }
}