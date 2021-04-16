import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

class Server{
    Selector selector;
    private ServerSocketChannel serverSocketChannel;
    private List<Client> connections = new Vector<>();

    Server(){
        startServer();
    }

    void startServer() {
        try {
            selector = Selector.open();
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.bind(new InetSocketAddress(5001));
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        } catch (Exception e) {
            if(serverSocketChannel.isOpen()) {
                stopServer();
                return;
            }
        }

        Thread thread = new Thread(() -> {
            while(true) {
                try {
                    int keyCount = selector.select();
                    if(keyCount == 0) {
                        continue;
                    }
                    Set<SelectionKey> selectionKeys = selector.selectedKeys();
                    Iterator<SelectionKey> iterator = selectionKeys.iterator();
                    while(iterator.hasNext()) {
                        SelectionKey selectionKey = iterator.next();
                        if(selectionKey.isAcceptable()) {
                            accept(selectionKey);
                        } else if (selectionKey.isReadable()) {
                            Client client = (Client)selectionKey.attachment();
                            client.receive();
                        } else if(selectionKey.isWritable()) {
                            Client client = (Client)selectionKey.attachment();
                            client.send(selectionKey);
                        }
                        iterator.remove();
                    }
                } catch (Exception e) {
                    if(serverSocketChannel.isOpen()) {
                        stopServer();
                        break;
                    }
                }
            }
        });
        thread.start();
    }

    private void stopServer() {
        try {
            Iterator<Client> iterator = connections.iterator();
            while(iterator.hasNext()) {
                Client client = iterator.next();
                client.socketChannel.close();
                iterator.remove();
            }
            if(serverSocketChannel != null && serverSocketChannel.isOpen()) {
                serverSocketChannel.close();
            }
            if(selector != null && selector.isOpen()) {
                selector.close();
            }
        } catch (Exception e) {

        }
    }

    private void accept(SelectionKey selectionKey) {
        try {
            ServerSocketChannel serverSocketChannel = (ServerSocketChannel) selectionKey.channel();
            SocketChannel socketChannel = serverSocketChannel.accept();

            System.out.println("new client accepted");

            Client client = new Client(socketChannel, connections, selector);
            connections.add(client);
        } catch (Exception e) {
            if(serverSocketChannel.isOpen()) {
                stopServer();
            }
        }
    }
}