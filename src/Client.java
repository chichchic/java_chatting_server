import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.ExecutorService;

class Client {
    private ExecutorService executorService;
    SocketChannel socketChannel;
    private int num;
    private List<Client> connections;

    Client(SocketChannel socketChannel, int num, List<Client> connections, ExecutorService executorService ) {
        this.socketChannel = socketChannel;
        this.num = num;
        this.connections = connections;
        this.executorService = executorService;
        receive();
    }

    private void receive() {
        Runnable runnable = () -> {
            while(true) {
                try{
                    ByteBuffer byteBuffer = ByteBuffer.allocate(100);
                    int readByteCount = socketChannel.read(byteBuffer);
                    if(readByteCount == -1) {
                        throw new IOException();
                    }

                    byteBuffer.flip();
                    Charset charset = Charset.forName("UTF-8");
                    String data = charset.decode(byteBuffer).toString();
                    for(Client client : connections) {
                        client.send(data);
                    }
                } catch (Exception e) {
                    break;
                }
            }
        };
        executorService.submit(runnable);
    }

    private void send(String data) {
        Runnable runnable = () -> {
            try {
                Charset charset = Charset.forName("UTF-8");
                ByteBuffer byteBuffer = charset.encode(data);
                socketChannel.write(byteBuffer);
            } catch (Exception e) {
                try {
                    socketChannel.close();
                } catch (IOException e2) {
                }
            }
        };
        executorService.submit(runnable);
    }
}
