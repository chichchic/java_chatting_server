import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.concurrent.Executors;

public class ClientRunner {

    public static void main(String[] args) {
        ClientRunner clientRunner = new ClientRunner();
        clientRunner.startClient();
    }

    AsynchronousChannelGroup channelGroup;
    AsynchronousSocketChannel socketChannel;

    void startClient() {
        try {
            channelGroup = AsynchronousChannelGroup.withFixedThreadPool(
                    Runtime.getRuntime().availableProcessors(),
                    Executors.defaultThreadFactory()
            );
            socketChannel = AsynchronousSocketChannel.open(channelGroup);
            socketChannel.connect(new InetSocketAddress("localhost", 5001), null, new CompletionHandler<Void, Void>() {
                @Override
                public void completed(Void result, Void attachment) {
                    receive();
                    Runnable writeRunnable = () -> {
                        while(true) {
                            try{
                                System.out.println("System in block");
                                InputStream in = System.in;
                                byte[] datas = new byte[100];
                                int dataCount = in.read(datas);
                                byte[] slicedData = Arrays.copyOfRange(datas, 0, dataCount);
                                String sendString = new String(slicedData);
                                send(sendString);
                            } catch (Exception e) {
                                if(socketChannel.isOpen()) {
                                    stopClient();
                                }
                                break;
                            }
                        }
                    };
                    new Thread(writeRunnable).start();
                }

                @Override
                public void failed(Throwable e, Void attachment) {
                    if (socketChannel.isOpen()) {
                        stopClient();
                    }
                }
            });
        } catch (IOException e) {
        }
    }

    void stopClient() {
        try {
            if(channelGroup!=null && !channelGroup.isShutdown()) {
                channelGroup.shutdownNow();
            }
        } catch (IOException e) {}
    }

    void receive() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(100);
        socketChannel.read(byteBuffer, byteBuffer, new CompletionHandler<>() {
            @Override
            public void completed(Integer result, ByteBuffer attachment) {
                try {
                    attachment.flip();
                    Charset charset = Charset.forName("utf-8");
                    String data = charset.decode(attachment).toString();
                    System.out.println("received data: " + data);
                    ByteBuffer byteBuffer = ByteBuffer.allocate(100);
                    socketChannel.read(byteBuffer, byteBuffer, this);
                } catch (Exception e) {
                }
            }

            @Override
            public void failed(Throwable exc, ByteBuffer attachment) {
                stopClient();
            }
        });
    }

    void send(String data) {
        Charset charset = Charset.forName("utf-8");
        ByteBuffer byteBuffer = charset.encode(data);
        socketChannel.write(byteBuffer, null, new CompletionHandler<Integer, Void>() {
            @Override
            public void completed(Integer result, Void attachment) {
            }
            @Override
            public void failed(Throwable exc, Void attachment) {
                stopClient();
            }
        });
    }
}
