import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;

public class ClientRunner {

    public static void main(String[] args) {
        ClientRunner clientRunner = new ClientRunner();
        clientRunner.startClient();
    }

    Selector selector;
    SocketChannel socketChannel;

    void startClient() {
        try {
            selector = Selector.open();
        } catch (Exception e) {
            if(socketChannel.isOpen()) {
                stopClient();
            }
            return;
        }

        try {
            socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
            socketChannel.register(selector, SelectionKey.OP_CONNECT);
            socketChannel.connect(new InetSocketAddress("localhost", 5001));
        } catch (Exception e) {
            if(socketChannel.isOpen()) {
                stopClient();
            }
            return;
        }

        Runnable writeRunnable = () -> {
            while(true) {
                try{
                    InputStream in = System.in;
                    byte[] datas = new byte[100];
                    int dataCount = in.read(datas);
                    byte[] slicedData = Arrays.copyOfRange(datas, 0, dataCount);
                    send(new String(slicedData));
                } catch (Exception e) {
                    if(socketChannel.isOpen()) {
                        stopClient();
                    }
                    break;
                }
            }
        };
        new Thread(writeRunnable).start();


        Runnable runnable = () -> {
            while(true) {
                try{
                    int keyCount = selector.select();
                    if(keyCount == 0) {
                        continue;
                    }
                    Set<SelectionKey> selectedKeys = selector.selectedKeys();
                    Iterator<SelectionKey> iterator = selectedKeys.iterator();
                    while (iterator.hasNext()) {
                        SelectionKey selectionKey = iterator.next();
                        if (selectionKey.isConnectable()) {
                            connect(selectionKey);
                        } else if(selectionKey.isReadable()) {
                            receive();
                        } else if(selectionKey.isWritable()) {
                            send(selectionKey);
                        }
                        iterator.remove();
                    }
                } catch (Exception e) {
                    if(socketChannel.isOpen()) {
                        stopClient();
                    }
                    break;
                }
            }
        };
        new Thread(runnable).start();
    }

    void stopClient() {
        try {
            if(socketChannel!=null && socketChannel.isOpen()) {
                socketChannel.close();
            }
        } catch (IOException e) {}
    }

    void connect(SelectionKey selectionKey) {
        try {
            socketChannel.finishConnect();
            selectionKey.interestOps(SelectionKey.OP_READ);
            selector.wakeup();
        } catch(Exception e) {
            if(socketChannel.isOpen()) {
                stopClient();
            }
        }
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
            System.out.println("클라이언트 출력: " + data);
        } catch(Exception e) {
            stopClient();
        }
    }

    void send(SelectionKey selectionKey) {
        try {
            ByteBuffer byteBuffer = (ByteBuffer) selectionKey.attachment();
            socketChannel.write(byteBuffer);
            selectionKey.interestOps(SelectionKey.OP_READ);
        } catch(Exception e) {
            stopClient();
        }
    }

    void send(String data) {
        Charset charset = Charset.forName("UTF-8");
        ByteBuffer byteBuffer = charset.encode(data);
        SelectionKey key = socketChannel.keyFor(selector);
        key.attach(byteBuffer);
        key.interestOps(SelectionKey.OP_WRITE);
        selector.wakeup();
    }
}
