import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

/**
 * Handshakes between Client and Server
 */
public class Handshake {
    /**
     * Handshake to be called by server
     *
     */
    public static void serverShake(HashMap<String, Socket> socketMap, Socket client){
        StringWriter sw = new StringWriter();

        int i = 0;
        for(Map.Entry<String,Socket> entry : socketMap.entrySet()){
            Socket entrySocket = entry.getValue();
            sw.write(String.valueOf(entrySocket.getPort()));
            if(i++ < socketMap.entrySet().size()) sw.write(",");
        }

        String usedPortString = sw.toString();

        Socket newSocket = null;
        String username = null;

        // Handshake
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(client.getOutputStream()), true);

            String input = null;
            while((input = br.readLine()) == null) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    // wake up
                }
            }
            username = input;

            // Send the port list.
            pw.println(usedPortString);

            // Get the port.
            input = null;
            while((input = br.readLine()) != null) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    // wake up
                }
            }
            int portToUse = Integer.valueOf(input);

            pw.close();
            br.close();
            client.close();

            ServerSocket newServerPort = new ServerSocket(portToUse);
            newSocket = newServerPort.accept();

            // Add new socket to map
            socketMap.put(username, newSocket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handshake to be called by client
     *
     */
    public static Socket clientShake(String username, Socket server){
        Socket newSock = null;
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(server.getInputStream()));
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(server.getOutputStream()), true);

            // Send our username
            pw.println(username);

            // Get a list of used ports
            String input = null;
            while((input = br.readLine()) != null) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    // wake up
                }
            }
            String[] tokens = input.split(",");
            List<Integer> usedPortList = new ArrayList<Integer>();
            for(String s :  tokens){
                usedPortList.add(Integer.parseInt(s));
            }

            // Generate a randomly not used one.
            Random r = new Random();
            int portToUse = r.nextInt(63000) + 1024;
            while(usedPortList.contains(portToUse)){
                r = new Random();
                portToUse = r.nextInt(63000) + 1024;
            }

            // Send it back.
            pw.println(String.valueOf(portToUse));

            // todo: Chatroom negotiation here.

            InetAddress serverAddress = server.getInetAddress();
            pw.close();
            br.close();
            server.close();

            // Let
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            newSock = new Socket(serverAddress, portToUse);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return newSock;
    }
}
