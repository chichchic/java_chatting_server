import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    ExecutorService executorService;
    ServerSocket serverSocket = null;
    List<Client> clientList = new Vector<>();
    InputStream in;

    Server(){
        createServer();
    }

     public void createServer() {
        executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()); //cpu 코어 개수만큼
        try {
            serverSocket = new ServerSocket(5001);
        } catch (Exception e) {
            if (!serverSocket.isClosed()) {
                stopServer();
            }
            return;
        }

        Runnable runnable = () -> {
            while(true) {
                try{
                    Socket socket = serverSocket.accept();
                    System.out.println((clientList.size()+1) + "번째 클라이언트가 연결되었습니다.");
                    in = socket.getInputStream();
                    byte[] bytes = new byte[100];
                    int readByteCount = in.read(bytes);
                    for(Client client : clientList) {
                        Runnable broadcast = client.send(bytes);
                        executorService.submit(broadcast);
                    }
                    Client client = new Client(socket, clientList.size() + 1);
                    clientList.add(client);
                } catch (Exception e) {
                    if (!serverSocket.isClosed()) {
                        stopServer();
                    }
                    break;
                }
            }
        };

        executorService.submit(runnable);
    }



    void stopServer() {
        try {
            Iterator<Client> iterator = clientList.iterator();
            while(iterator.hasNext()) {
                Client client = iterator.next();
                client.socket.close();
                iterator.remove();
            }
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            if(executorService != null && !executorService.isShutdown()) {
                executorService.shutdown();
            }
        } catch (Exception e) {}
        System.out.println("stop server");
    }
}
