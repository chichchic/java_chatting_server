import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.List;

class Client {
    SocketChannel socketChannel;
    private String sendData;
    private List<Client> connections;
    Selector selector;

    Client(SocketChannel socketChannel, List<Client> connections, Selector selector) throws IOException{
        this.socketChannel = socketChannel;
        this.connections = connections;
        this.selector = selector;
        socketChannel.configureBlocking(false);
        SelectionKey selectionKey = socketChannel.register(selector, SelectionKey.OP_READ);
        selectionKey.attach(this);
    }

    void receive() {
        try {
            ByteBuffer byteBuffer = ByteBuffer.allocate(100);
            int byteCount = socketChannel.read(byteBuffer);

            if(byteCount == -1) {
                throw new IOException();
            }

            byteBuffer.flip();
            Charset charset = Charset.forName("UTF-8");
            String data = charset.decode(byteBuffer).toString();

            for(Client client : connections) {
                client.sendData = data;
                SelectionKey key = client.socketChannel.keyFor(selector);
                key.interestOps(SelectionKey.OP_WRITE);
            }
            selector.wakeup();
        } catch (Exception e) {
            try {
                connections.remove(this);
                socketChannel.close();
            } catch (IOException IOe) {
            }
        }
    }

    void send(SelectionKey selectionKey) {
        try {
            Charset charset = Charset.forName("UTF-8");
            ByteBuffer byteBuffer = charset.encode(sendData);
            socketChannel.write(byteBuffer);
            selectionKey.interestOps(SelectionKey.OP_READ);
            selector.wakeup();
        } catch (Exception e) {
            try {
                connections.remove(this);
                socketChannel.close();
            } catch (IOException IOe) {
            }
        }
    }
}
