import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * The Server entry point.
 */
public class Server {
    static int port;   // port


    public static void main(String args[]){

        // Read in config file
        parseConfig(args[0]);


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
