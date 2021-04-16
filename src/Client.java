import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.util.List;

class Client {
    AsynchronousSocketChannel socketChannel;
    private List<Client> connections;

    Client(AsynchronousSocketChannel socketChannel, List<Client> connections) {
        this.socketChannel = socketChannel;
        this.connections = connections;
        receive();
    }

    void receive() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(100);
        socketChannel.read(byteBuffer, byteBuffer, new CompletionHandler<>() {
            @Override
            public void completed(Integer result, ByteBuffer attachment) {
                try {
                    attachment.flip();
                    Charset charset = Charset.forName("UTF-8");
                    String data = charset.decode(attachment).toString();

                    for (Client client : connections) {
                        client.send(data);
                    }

                    ByteBuffer byteBuffer = ByteBuffer.allocate(100);
                    socketChannel.read(byteBuffer, byteBuffer, this);
                } catch (Exception e) {

                }
            }

            @Override
            public void failed(Throwable exc, ByteBuffer attachment) {
                try {
                    connections.remove(Client.this);
                    socketChannel.close();
                } catch (IOException IOe) {
                }
            }
        });
    }

    void send(String data) {
        Charset charset = Charset.forName("UTF-8");
        ByteBuffer byteBuffer = charset.encode(data);
        socketChannel.write(byteBuffer, null, new CompletionHandler<Integer, Void>() {
            @Override
            public void completed(Integer result, Void attachment) {
            }

            @Override
            public void failed(Throwable exc, Void attachment) {
                try {
                    connections.remove(Client.this);
                    socketChannel.close();
                } catch (IOException e) {
                }
            }
        });
    }
}
