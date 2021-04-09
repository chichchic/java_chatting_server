import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class Client {
    public static void main(String[] args) {
        Socket socket = null;
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress("localhost", 5001));

            OutputStream out = socket.getOutputStream();
            InputStream in = socket.getInputStream();

            String message = "Hello Server";
            out.write(message.getBytes("UTF-8"));
            out.flush();

            byte[] bytes = new byte[100];
            int readByteCount = in.read(bytes);
            String echo = new String(bytes, 0, readByteCount, "UTF-8");
            System.out.println(echo + " :: echo");

            out.close();
            in.close();
        } catch (Exception e) {
        }

        if (!socket.isClosed()) {
            try {
                socket.close();
            } catch (IOException e) {
            }
        }
    }
}
