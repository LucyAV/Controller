import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        String ip = "10.0.0.88";
        int port = 6969;

        try {
            Socket socket = new Socket(ip, port);
            PrintWriter printWriter = new PrintWriter(socket.getOutputStream());

            Scanner scanner = new Scanner(System.in);
            while (scanner.hasNext()) {
                printWriter.write(scanner.nextLine() + " 170");
                printWriter.flush();
            }

            socket.close();
        } catch (IOException ioe) {

        }

    }

}
