import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public static void main(String[] args) {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(5001);
            System.out.println("listening port:5001");
            while(true) {
                Socket socket = serverSocket.accept();
                System.out.println("accept client's connect request");

                InputStream in = socket.getInputStream();
                OutputStream out = socket.getOutputStream();

                byte[] bytes = new byte[100];

                int readByteCount = in.read(bytes);

                String message = new String(bytes, 0, readByteCount, "UTF-8");
                System.out.println(message +" :: client");

                out.write(bytes, 0, readByteCount);
                out.flush();

                in.close();
                out.close();
            }
        } catch (Exception e) {
            System.out.println("communication err :: " + e.getMessage());
        }

        if(!serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                System.out.println("socket close err :: " + e.getMessage());
            }
        }
    }
}
