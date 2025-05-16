
/** 
 * POSRunner.java
 * Initiates the program
 * Determines whether to be a client/server based on whether a server exists
 */

import java.io.IOException;

public class POSRunner {
    /*
     * Can be set to other files within the directory,
     * such as the demo files used in the video
     */
    public static final String ORDERS_FILENAME = "data/orders.txt";
    public static final String MENU_FILENAME = "data/menu.txt";

    public static void main(String args[]) throws IOException {
        Client client = new Client("127.0.0.1", 5000);
        if (client.getSocket() == null) {
            System.out.println("No server found. Becoming server.");
            Server server = new Server(5000);
        }
    }
}