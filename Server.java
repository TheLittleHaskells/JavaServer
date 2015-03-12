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
    static IncomingMessageHandler imh;
    static Thread listenerThread;
    static Thread imhThread;


    public static void main(String args[]){
        // Read in config file
        if(args.length == 0 || args.length > 1){
            System.err.println("Usage: JavaServer <config file>");
            System.exit(1);
        }
        parseConfig(args[0]);

        // Start Listener to listen for new connections from clients
        listener = new Listener(port);
        listenerThread = new Thread(listener);
        listenerThread.start();

        System.out.printf("Server started and listening for connections on port: %d\n", port);

        // Start incoming message handler
        imh = new IncomingMessageHandler(listener);
        imhThread = new Thread(imh);
        imhThread.start();

        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run(){
                for(Map.Entry<String, Socket> entry : listener.getSocketList().entrySet()){
                    Socket toSendTo = entry.getValue();
                    sendMessage("GTFO", "SERVER", toSendTo);
                }
            }
        });

        // start ui stuff
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String input;
        while(true){
            try {
                displayPrompt();
                input = br.readLine();
                if(input.matches("/.*")){
                    processCommand(input.substring(1)); // cut off the / and pass to processcommand
                }else {
                    // send to all clients here.
                    // DEBUG
                    for(Map.Entry<String, Socket> entry : listener.getSocketList().entrySet()){
                    }
                    // END DEBUG
                    for(Map.Entry<String, Socket> entry : listener.getSocketList().entrySet()){
                        Socket toSendTo = entry.getValue();
                        sendMessage("CHAT", "SERVER: " + input, toSendTo);
                    }
                }
            } catch (IOException e) {
            }
        }
    }

    /**
     * Processes a command entered by the user
     *
     * @param command The command (expected WITHOUT the /)
     */
    public static void processCommand(String command){
        String userList;
        if(command.toLowerCase().matches("list")){
            userList = "";
            if(listener.getSocketList().size() == 0){
                System.out.println("No users connected.");
                return;
            }
            for(Map.Entry<String, Socket> entry : listener.getSocketList().entrySet()){
                String user = entry.getKey();
                userList += user + ',';
            }
            userList = userList.substring(0,userList.length()-1);
            System.out.printf("\t%s", userList);
        }
        if(command.toLowerCase().matches("quit")){
            System.exit(0);
        }
    }

    public static void sendMessage(String type, String payload, Socket client){
        String message = type + "@" + payload;
        //send message using socket
        try {
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(client.getOutputStream()), true);
            pw.println(message);
        }catch(IOException e){
            String toRemove = null;
            for(Map.Entry<String, Socket> entry : listener.getSocketList().entrySet()){
                if(entry.getValue() == client){
                    toRemove = entry.getKey();
                    break;
                }
            }
            System.err.printf("\t%s has disconnected.", toRemove);
            listener.getSocketList().remove(toRemove);
            for(Map.Entry<String, Socket> entry : listener.getSocketList().entrySet()){
                sendMessage("CHAT", toRemove + " has disconnected.", entry.getValue());
            }
        }
    }


    /**
     * Sends a string of usernames separated by '@' symbols
     */
    public static void sendUserList(String username){
        String userList = "";
        for(Map.Entry<String, Socket> entry : listener.getSocketList().entrySet()){
            String user = entry.getKey();
            userList += user + '@';
        }
        userList = userList.substring(0,userList.length()-1);
        //for(Map.Entry<String, Socket> entry : listener.getSocketList().entrySet()){
        Socket entry = listener.getSocketList().get(username);
        sendMessage("LIST",userList,entry);
        //}
        //userlist = list.split("@");
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
     * @param message message
     */
    public static void displayChatMessage( String message){
        System.out.printf("\n\t %s", message);
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

    /**
     * Shows an error to the screen
     *
     * @param s the error message
     */
    public static void displayError(String s){
        System.err.println(s);
    }
}
