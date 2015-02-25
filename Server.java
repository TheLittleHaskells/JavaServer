import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

/**
 * The Server entry point.
 */
public class Server {
    static int port;   // port
    static String[] userlist;
    static Listener listener;
    static Thread listenerThread;


    public static void main(String args[]){
        // Read in config file
        parseConfig(args[0]);

        // Start
        listener = new Listener(port);
        listenerThread = new Thread(listener);
        listenerThread.start();

        System.out.printf("Server started and listening on port: %d\n", port);

        // start ui stuff
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String input;
        while(true){
            try {
                displayPrompt();
                input = br.readLine();
                if(input.matches("/.*")){
                    processCommand();
                }else {
                    // send to all clients here.
                    for(Map.Entry<String, Socket> entry : listener.getSocketList().entrySet()){
                        Socket toSendTo = entry.getValue();
                        sendMessage("CHAT", input, toSendTo);
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Something went went.");
            }
        }
    }

    public static void sendMessage(String type,String payload, Socket client){
        String message = type + "@" + payload;

        //send message using socket
        try {
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(client.getOutputStream()), true);
            pw.println(message);
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public static void readMessage(String username, String msg){
        String[] tokens = msg.split("@");
        String payload = msg.substring(5);

        // List users
        if(tokens[0].matches("LIST")){
            updateUserList(payload);

        // Login messages
        }else if(tokens[0].matches("GTFI")){
            displayUserLoggedIn(payload);

        // Logoff messages
        }else if(tokens[0].matches("GTFO")){
            displayUserLoggedOff(payload);
            try {
                listener.getSocketList().get(username).close();
            }catch (Exception e){

            }
            listener.getSocketList().remove(username);

        // Chat messages
        }else if(tokens[0].matches("CHAT")){
            displayChatMessage(username, payload);
            for(Map.Entry<String, Socket> entry : listener.getSocketList().entrySet()){
                if(entry.getKey().matches(username)){
                    Socket toSendTo = entry.getValue();
                    sendMessage("CHAT", payload, toSendTo);
                    break;
                }
            }
        // Userlist request messages
        }else if(tokens[0].matches("REQU")){
            Socket toSendTo = null;
            for(Map.Entry<String, Socket> entry : listener.getSocketList().entrySet()){
                if(entry.getKey().matches(username)){
                    toSendTo = entry.getValue();
                    break;
                }
            }
            if(toSendTo == null){
                displayError("Tried to send a userlist to a user who doens't exist.");
                return;
            }
            String userlistString = generateUserList(listener.getSocketList());
            sendMessage("LIST", userlistString, toSendTo);
            // Other messages
        }else{
            displayError("Invalid message received: " + msg);
        }
    }

    /**
     * updates the stored list of users
     *
     * @param list formatted userlist string (should be delimited by @)
     */
    public static void updateUserList(String list){
        userlist = list.split("@");
    }

    /**
     * Displays a message saying a user has logged on.
     *
     * @param username username of person who logged in
     */
    public static void displayUserLoggedIn(String username){
        System.out.printf("\n\t%s has logged on.", username);
        displayPrompt();
    }

    /**
     * Displays a message saying a user has logged off.
     *
     * @param username username of person who logged off
     */
    public static void displayUserLoggedOff(String username){
        System.out.printf("\n\t%s has logged off.", username);
        displayPrompt();
    }

    /**
     * Displays a message from a user to the screen
     *
     * @param username source username
     * @param message message
     */
    public static void displayChatMessage(String username, String message){
        System.out.printf("\n\t%s: %s", username, message);
        displayPrompt();
    }

    /**
     * displayPrompt shows a newline and a prompt to the user.
     */
    public static void displayPrompt(){
        System.out.printf("\n~> ");
    }

    public static String generateUserList(HashMap<String,Socket> userMap){
        String payload ="";
        for(Map.Entry<String, Socket> entry: userMap.entrySet()) {
            String user = entry.getKey();
            payload = payload + "@" + user;
        }
        return payload.substring(1);
    }


    /**
     * Reads in the config file
     *
     * Formatted like <option>=<value>
     *
     */
    public static void parseConfig(String filename){
        File f = new File(filename);
        if(!f.exists()){
            System.err.println("Config file didn't exist.");
            return;
        }
        try {
            BufferedReader br = new BufferedReader(new FileReader(f));
            String input = null;

            // read in a line, and if it's not null, do what's inside the loop
            while((input = br.readLine()) != null) {
                String[] tokens = input.split("=");
                if(tokens.length != 2){
                    displayError("Invalid config file..");
                    return;
                }

                String key = tokens[0];
                String value = tokens[1];

                if(key.toUpperCase().matches("PORT")){
                    port = Integer.parseInt(value);
                    continue;
                }else{
                    displayError("Invalid port value");
                }
            }


        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private static void displayError(String s){
        System.err.println(s);
    }
}
