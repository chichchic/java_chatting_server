import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Arrays;

public class WriteClientRunner {
    public static void main(String[] args) {
        try {
            SocketChannel socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(true);
            socketChannel.connect(new InetSocketAddress("localhost", 5001));


            while(true) {
                InputStream in = System.in;
                byte[] datas = new byte[100];
                System.out.println("서버로 보낼 값을 입력해주세요.");
                int inputSize = in.read(datas);
                byte[] slicedData = Arrays.copyOfRange(datas, 0, inputSize);
                ByteBuffer buffer = ByteBuffer.wrap(slicedData);
                socketChannel.write(buffer.slice());
            }
        } catch (Exception e) {}
    }
}
