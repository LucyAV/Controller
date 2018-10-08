import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class NetworkConnector extends Thread {

    // Network connection and timing variables
    private final String HOST = "10.0.0.232";       //HOME
//    private final String HOST = "10.68.59.232";     //SCHOOL
//    private final String HOST = "192.168.43.232";   //HOTSPOT
    private final int PORT = 23232;
    private Socket socket;
    private PrintWriter printWriter;
    private final int TRANSMISSION_TIME_MS = 50;

    // Current servo and motor values
    public int servoValue;
    public int motorValue;

    //TESTS
    ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

    Runnable periodicTask = () -> {
        printWriter.write("" + ( ((servoValue - 100) * 100) + (motorValue - 100) ) );
        printWriter.flush();
        System.out.println("SENT DATA");
    };

    // Initialize network Socket and PrintWriter, then begin to transmit data when connected
    public void run() {
        System.out.println("Waiting for connection...");

        try {
            socket = new Socket(HOST, PORT);
            socket.setTcpNoDelay(true);
            socket.setTrafficClass(3);
            printWriter = new PrintWriter(socket.getOutputStream());
            System.out.println("Connected to " + socket.getInetAddress());

            scheduledExecutorService.scheduleAtFixedRate(periodicTask, 0, TRANSMISSION_TIME_MS, TimeUnit.MILLISECONDS);
        } catch (IOException ioe) { }
    }

    // End the connection by closing the network Socket
    public void close() {
        try {
            scheduledExecutorService.shutdown();
            socket.close();
            System.out.println("Socket Closed");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setMotorValue(int motorValue) {
        this.motorValue = motorValue;
    }

    public void setServoValue(int servoValue) {
        this.servoValue = servoValue;
    }
}
