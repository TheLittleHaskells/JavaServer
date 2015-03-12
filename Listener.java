import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by schweepup on 2/11/15.
 */
public class Listener implements Runnable{
    int port;
    volatile static ConcurrentHashMap<String,Socket> socketList;
    ServerSocket listeningServer;

    public Listener(int port){
        this.port = port;
        this.socketList = new ConcurrentHashMap<String, Socket>();
        try {
            this.listeningServer = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void run() {
        Socket clientSocket = null;
        while(true){
            try {
                clientSocket = listeningServer.accept();
                handle(clientSocket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void handle(Socket sockyj){
        Handshake.serverShake(socketList, sockyj);
        System.out.println("Added new used to list.");
    }

    public ConcurrentHashMap<String, Socket> getSocketList() {
        return socketList;
    }
}
