import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Executors;

class Server{
    AsynchronousChannelGroup channelGroup;
    AsynchronousServerSocketChannel serverSocketChannel;
    private List<Client> connections = new Vector<>();

    Server(){
        startServer();
    }

    void startServer() {
        try {
            channelGroup = AsynchronousChannelGroup.withFixedThreadPool(
                    Runtime.getRuntime().availableProcessors(),
                    Executors.defaultThreadFactory()
            );
            serverSocketChannel = AsynchronousServerSocketChannel.open(channelGroup);
            serverSocketChannel.bind(new InetSocketAddress(5001));
        } catch (Exception e) {
            if(serverSocketChannel.isOpen()) {
                stopServer();
                return;
            }
        }

        serverSocketChannel.accept(null, new CompletionHandler<AsynchronousSocketChannel, Void>() {
            @Override
            public void completed(AsynchronousSocketChannel socketChannel, Void attachment) {

                Client client = new Client(socketChannel, connections);
                connections.add(client);
                serverSocketChannel.accept(null, this);
            }
            @Override
            public void failed(Throwable exc, Void attachment) {
                if(serverSocketChannel.isOpen()) {
                    stopServer();
                }
            }
        });
    }

    private void stopServer() {
        try {
            connections.clear();
            if(channelGroup != null && !channelGroup.isShutdown()) {
                channelGroup.shutdownNow();
            }
        } catch (Exception e) {

        }
    }
}