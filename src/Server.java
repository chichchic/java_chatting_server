import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class Server{
    private ExecutorService executorService;
    private ServerSocketChannel serverSocketChannel;
    private List<Client> connections = new Vector<>();

    Server(){
        startServer();
    }

    void startServer(){
        executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        try {
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(true);
            serverSocketChannel.bind(new InetSocketAddress(5001));
        } catch (Exception e) {
            if(serverSocketChannel.isOpen()) {
                stopServer();
                return;
            }
        }

        Runnable runnable = () -> {
            while(true) {
                try {
                    SocketChannel socketChannel = serverSocketChannel.accept();
                    System.out.println("new client connected");
                    Client client = new Client(socketChannel, connections.size()+1, connections, executorService);
                    connections.add(client);
                } catch (Exception e) {
                    if(serverSocketChannel.isOpen()) {
                        stopServer();
                    }
                }
            }
        };
        executorService.submit(runnable);
    }

    private void stopServer() {
        try {
            Iterator<Client> iterator = connections.iterator();
            while(iterator.hasNext()) {
                Client client = iterator.next();
                client.socketChannel.close();
                iterator.remove();
            }
            if(serverSocketChannel != null &&
            !executorService.isShutdown()) {
                executorService.shutdown();
            }
        } catch (Exception e) {

        }
    }
}