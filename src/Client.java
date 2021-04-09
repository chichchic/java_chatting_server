import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class Client {
    Socket socket;
    int num;

    Client(Socket socket, int num) {
        this.socket = socket;
        this.num = num;
    }

    Runnable send(byte[] data) {
        Runnable runnable = () -> {
            try {
                OutputStream out = socket.getOutputStream();
                out.write(data);
                out.flush();
            } catch (Exception e) {
            }
        };
        return runnable;
    }
}
