import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class ClientRunner {
    public static void main(String[] args) {
        Socket socket = null;
        try {
            InputStream in = System.in;
            byte[] datas = new byte[100];
            in.read(datas);

            socket = new Socket("localhost", 5001);
            OutputStream os = socket.getOutputStream();
            os.write(datas);
            os.flush();

            while(true) {
                in = socket.getInputStream();
                int dataSize = in.read(datas);
                String msg = new String(datas, 0, dataSize, "UTF-8");
                System.out.println(msg);
            }
        } catch (Exception e) {}
    }
}
