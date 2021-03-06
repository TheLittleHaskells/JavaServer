import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Map;

/**
 * Created by cg on 2/25/15.
 */
public class IncomingMessageHandler implements Runnable{
    public static final int UPDATETIME = 250; // Time between checking socket list for new messages in ms
    Listener listener;
    Boolean running;

    public IncomingMessageHandler(Listener l){
        this.listener = l;
        this.running = false;
    }

    /**
     * Iterates through the message list looking for new messages from connected clients, then passes
     * any messages that it finds to readMessage to be handled.
     *
     * TODO: Maybe disconnect users when IOException is caught?
     */
    @Override
    public void run() {
        this.running = true;
        while(running){
            for(Map.Entry<String, Socket> entry : listener.getSocketList().entrySet()){
                try {
                    BufferedReader br = new BufferedReader(new InputStreamReader(entry.getValue().getInputStream()));
                    String input = null;

                    if(br.ready() && (input = br.readLine()) != null){
                        readMessage(entry.getKey(), input);
                    }
                } catch (IOException e) {
                    Server.displayError("Trouble talking to: " + entry.getKey());
                    e.printStackTrace();
                }
            }
            try {
                Thread.sleep(UPDATETIME);
            } catch (InterruptedException e) {
                // an InterruptedException was thrown (which we don't really care about)
            }
        }
    }

    /**
     * Stop the main loop from running.
     */
    public void stop(){
        this.running = false;
    }

    /**
     * readMessage takes in a username and a message and handles the message
     * appropriately (sending to UI, relaying to other clients, etc...)
     *
     * @param username username of sender
     * @param msg message in string format
     */
    public void readMessage(String username, String msg){
        String[] tokens = msg.split("@");
        String payload = msg.substring(5);

        // List users
        if(tokens[0].matches("LIST")){
            Server.sendUserList(username);

            // Login messages
        }else if(tokens[0].matches("GTFI")){
            Server.displayUserLoggedIn(payload);
            payload = username+" has logged on.";
            for(Map.Entry<String, Socket> entry : listener.getSocketList().entrySet()){
                //System.out.println("Hit relay chat loop with outbound username " + entry.getKey());
                if(!entry.getKey().matches(username)){
                    Socket toSendTo = entry.getValue();
                    Server.sendMessage("CHAT", payload, toSendTo);
                }
            }
            // Logoff messages
        }else if(tokens[0].matches("GTFO")){
            Server.displayUserLoggedOff(payload);
            for(Map.Entry<String, Socket> entry : listener.getSocketList().entrySet()){
                //System.out.println("Hit relay chat loop with outbound username " + entry.getKey());
                payload = username+" has logged off.";
                if(!entry.getKey().matches(username)){
                    Socket toSendTo = entry.getValue();
                    Server.sendMessage("CHAT", payload, toSendTo);
                }
            }
            try {
                this.listener.getSocketList().get(username).close();
                this.listener.getSocketList().remove(username);
            }catch (Exception e){
                e.printStackTrace();
            }

            // Chat messages
        }else if(tokens[0].matches("CHAT")){
            payload = username + ": " + payload;
            Server.displayChatMessage(payload);
            for(Map.Entry<String, Socket> entry : listener.getSocketList().entrySet()){
                if(!entry.getKey().matches(username)){
                    Socket toSendTo = entry.getValue();
                    Server.sendMessage("CHAT", payload, toSendTo);
                }
            }
            // Other messages
        }else{
            Server.displayError("Invalid message received: " + msg);
        }
    }
}
