
/** 
 * POS.java
 * Manages a database of Orders (both txt and ArrayList forms)
 * @see Order.java
 */

import java.io.DataOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;

import javax.swing.table.DefaultTableModel;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

public class POS {
    private ArrayList<Order> orderList; // list of orders within POS
    private OrdersTableModel ordersTableModel; // table model (through which GUI accesses order data)
    private String ordersSaveStr; // JSON string of save data to deserialize into ordersList
    private String menuSaveStr; // JSON string of save data to deserialize into menuInstance's categoriesList
    private GUI GUIinstance;  // manages POS's GUI
    private Menu menuInstance; // embodies POS's Menu
    private Socket socket; // network socket to send order data across
    private boolean belongsToServer; // if it does, save data to Server device's files

    public POS(String ordersSaveStr, String menuSaveStr, Socket socket, boolean belongsToServer) throws IOException {
        this.ordersSaveStr = ordersSaveStr;
        this.menuSaveStr = menuSaveStr;
        this.socket = socket;
        this.belongsToServer = belongsToServer;

        try {
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.setPrettyPrinting();
            gsonBuilder.registerTypeAdapter(Order.class, new OrderDeserializer());
            Gson gson = gsonBuilder.create();
            Type listType = new TypeToken<ArrayList<Order>>(){}.getType();
            orderList = gson.fromJson(ordersSaveStr, listType);
        } catch (JsonSyntaxException e) {
            System.out.println(POSRunner.ORDERS_FILENAME + " has invalid JSON data.");
            Path oldPath = Paths.get(POSRunner.ORDERS_FILENAME);
            Path newPath = Paths.get(POSRunner.ORDERS_FILENAME + ".corrupted");
            try {
                int num = 1;
                // loop until the file name is unique
                while (newPath.toFile().exists()) {
                    // append the number to the file name
                    String newFileName = POSRunner.ORDERS_FILENAME + ".corrupted" + num;
                    // update the target path
                    newPath = Paths.get(newFileName);
                    // increment number
                    num++;
                }
                Files.move(oldPath, newPath, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Old orders save file renamed successfully to " + newPath);
            } catch (IOException io) {
                System.out.println("File could not be renamed.");
                io.printStackTrace();
            }
            Server.updateOrdersDataStringToSend();
        } catch (Exception e) {
            System.out.println("Error reading/parsing " + POSRunner.ORDERS_FILENAME + " because of " + e);
        } finally {
            if (orderList == null) {
                orderList = new ArrayList<Order>();
            }
            ordersTableModel = new OrdersTableModel(this);
            menuInstance = new Menu(menuSaveStr, socket, belongsToServer);
            GUIinstance = new GUI(this);
        }
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
        menuInstance.setSocket(socket);
    }

    public void addOrder(Order order) {
        orderList.add(order);
    }

    public void sortByDateTime() {
        for (int i = 1; i < orderList.size(); i++) {

            // Copy first unsorted element.
            Order unsorted = orderList.get(i);

            // Create a marker to hold the last element of the sorted portion of the array,
            // the remaining portion of the array is unsorted
            int marker = i - 1;

            // Loop backwards through the sorted portion starting at marker

            // PARSE THE INT
            // OR SORT BY LETTERS

            while (marker >= 0) {
                if ((unsorted.getDateTime().isBefore(orderList.get(marker).getDateTime()))) {
                    orderList.set(marker + 1, orderList.get(marker));
                    orderList.set(marker, unsorted);
                } else {
                    // element is in correct place so stop iterating
                    marker = 0;
                }
                marker--;
            }
        }
    }

    public void sortByID() {
        for (int i = 1; i < orderList.size(); i++) {

            // Copy first unsorted element.
            Order unsorted = orderList.get(i);

            // Create a marker to hold the last element of the sorted portion of the array,
            // the remaining portion of the array is unsorted
            int marker = i - 1;

            // Loop backwards through the sorted portion starting at marker

            // PARSE THE INT
            // OR SORT BY LETTERS

            while (marker >= 0) {
                if (unsorted.getID() < (orderList.get(marker).getID())) {
                    orderList.set(marker + 1, orderList.get(marker));
                    orderList.set(marker, unsorted);
                } else {
                    // element is in correct place so stop iterating
                    marker = 0;
                }
                marker--;
            }
        }
    }

    public ArrayList<Order> getOrders() {
        return orderList;
    }

    public DefaultTableModel getOrdersTableModel() {
        return ordersTableModel;
    }

    public Menu getMenu() {
        return menuInstance;
    }

    public GUI getGUI() {
        return GUIinstance;
    }

    public int searchByOrderIDInOrdersList(int orderID) {
        int left_bound = 0;
        int right_bound = orderList.size() - 1;
        int middleIndex = (left_bound + right_bound) / 2;
        while (left_bound <= right_bound) {
            // System.out.println(left_bound + ", " + right_bound);
            if (orderList.get(middleIndex).getID() == (orderID)) {
                return middleIndex;
            } else if (orderList.get(middleIndex).getID() < (orderID)) {
                left_bound = middleIndex + 1;
                middleIndex = (left_bound + right_bound) / 2;
            } else if (orderList.get(middleIndex).getID() > (orderID)) {
                right_bound = middleIndex - 1;
                middleIndex = (left_bound + right_bound) / 2;
            }
        }
        return -1;
    }

    public int searchByOrderIDInOrdersTableModel(int orderID) {
        int left_bound = 0;
        int right_bound = ordersTableModel.getRowCount() - 1;
        int middleIndex = (left_bound + right_bound) / 2;
        while (left_bound <= right_bound) {
            // Order ID is now in column 7
            Object idObj = ordersTableModel.getValueAt(middleIndex, 7);
            if (idObj == null) { // Should not happen if table is populated correctly
                return -1;
            }
            int currentRowID = (int) idObj;
            // System.out.println(left_bound + ", " + right_bound);
            if (currentRowID == (orderID)) {
                return middleIndex;
            } else if (currentRowID < (orderID)) {
                left_bound = middleIndex + 1;
                middleIndex = (left_bound + right_bound) / 2;
            } else if (currentRowID > (orderID)) {
                right_bound = middleIndex - 1;
                middleIndex = (left_bound + right_bound) / 2;
            }
        }
        return -1;
    }

    public String toString() {
        String outputStr = "";
        for (Order o : orderList) {
            outputStr += o.toString() + "\n";
        }
        return outputStr;
    }

    // Saves orders to file if POS instance is Server's
    public void saveOrders() {
        if (belongsToServer) {
            try {
                System.out.println("Saving Orders");
                GsonBuilder gsonBuilder = new GsonBuilder();
                gsonBuilder.setPrettyPrinting();
                gsonBuilder.registerTypeAdapter(LocalDateTime.class, new DateTimeGsonTypeAdapter());
                Gson gson = gsonBuilder.create();
                String saveStr = gson.toJson(orderList);
                FileWriter wr = new FileWriter(POSRunner.ORDERS_FILENAME);
                wr.write(saveStr);
                wr.close();
            } catch (Exception e) {
                System.out.println("Error reading or parsing " + POSRunner.ORDERS_FILENAME + " because of " + e);
            }
        }
    }

    // Sends Sendable data via socket
    public void sendData(Sendable sentObj) {
        if (socket != null) {
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.setPrettyPrinting();
            gsonBuilder.registerTypeAdapter(LocalDateTime.class, new DateTimeGsonTypeAdapter());
            Gson gson = gsonBuilder.create();
            String sendStr = gson.toJson(sentObj);
            // sends output to the socket
            try {
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                out.writeUTF(sendStr);
            } catch (IOException i) {
                System.out.println(i);
            }
        }
    }
}
