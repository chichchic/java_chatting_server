import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

public class ReadClientRunner {
    public static void main(String[] args) {
        try {
            SocketChannel socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
            socketChannel.connect(new InetSocketAddress("localhost", 5001));

            Charset charset = Charset.forName("UTF-8");
            while(true) {
                ByteBuffer buffer = ByteBuffer.allocate(100);
                int bufferCount = socketChannel.read(buffer);
                if(bufferCount == -1) {
                    throw new IOException();
                }
                buffer.flip();
                System.out.println(charset.decode(buffer).toString());
            }
        } catch (Exception e) {}
    }
}
