import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

/**
 * The Server entry point.
 */
public class Server {
    static int port;   // port
    static String[] userlist;

    public static void main(String args[]){
        // Read in config file
        parseConfig(args[0]);
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

        // Chat messages
        }else if(tokens[0].matches("CHAT")){
            displayChatMessage(username, payload);

        // Other messages
        }else if(tokens[0].matches("REQU")){
            // todo: sendUserlist(username);

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
