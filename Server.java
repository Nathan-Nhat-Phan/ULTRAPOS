
/** 
 * Server.java
 * Interacts with Client via networking
 * Loads/saves data to/from save files
 * Sends save data to Client
 */

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.ArrayList;

import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Server {
    private Socket socket = null;
    private ServerSocket server = null;
    private DataInputStream in = null;
    private DataOutputStream out = null;
    private POS POSInstance = null;

    private static String ordersData;
    private static String menuData;

    public Server(int port) {
        // starts server and waits for a connection
        try {
            // Initialize POS instance
            ordersData = "";
            menuData = "";
            updateOrdersDataStringToSend();
            updateMenuDataStringToSend();
            POSInstance = new POS(ordersData, menuData, null, true);
            server = new ServerSocket(port);
            System.out.println("Server started");

            System.out.println("Waiting for a client ...");

            socket = server.accept();
            System.out.println("Client accepted");

            POSInstance.setSocket(socket);
            in = new DataInputStream(
                    new BufferedInputStream(socket.getInputStream()));

            // sends output to the socket
            out = new DataOutputStream(socket.getOutputStream());

            // Send the data to the client
            updateOrdersDataStringToSend();
            out.writeUTF(ordersData);
            updateMenuDataStringToSend();
            out.writeUTF(menuData);
            System.out.println("Sent");

            String data = "";
            while (!data.equals("close")) {
                try {
                    data = in.readUTF();
                    final String readData = data; // Declare a final variable
                    System.out.println(data);
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            interpretSentString(readData);
                        }
                    });
                } catch (IOException i) {
                    System.out.println(i);
                    System.out.println("Closing connection");

                    // close connection
                    socket.close();
                    in.close();
                    out.close();
                    data = "close";
                }
            }
            System.out.println("Closing connection");

            // close connection
            socket.close();
            in.close();
            out.close();
        } catch (IOException i) {
            System.out.println(i);
        }
    }

    // Interprets the JSON string a Server/Client receives and acts accordingly
    // Updates Orders/Menu databases and GUI
    public void interpretSentString(String sentStr) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setPrettyPrinting();
        gsonBuilder.registerTypeAdapter(Sendable.class, new CustomDeserializer());
        Gson gson = gsonBuilder.create();

        Menu menu = POSInstance.getMenu();
        MenuCreator menuCreator = POSInstance.getGUI().getMgmtView().getMenuCreator();

        // Object proposed = gson.fromJson(sentStr, Object.class); instanceof fails to
        // recognize Objects that are Category

        Sendable proposed = gson.fromJson(sentStr, Sendable.class);
        if (proposed instanceof Category) {
            Category proposedCategory = (Category) proposed;
            System.out.println("Category armed");
            if (proposedCategory != null) {
                int categoryID = proposedCategory.getID();
                DefaultTableModel categoriesTableModel = menuCreator.getCategoriesTableModel();
                int indexWithinMenu = menu.searchByCategoryID(categoryID);
                if (indexWithinMenu == -1) // not in menu, therefore new category
                {
                    menu.getCategories().add(proposedCategory);
                    Object[] categoryRow = { proposedCategory, proposedCategory.getID() };
                    categoriesTableModel.addRow(categoryRow);
                    System.out.println("Added Category");
                } else if (menu.getCategories().get(indexWithinMenu).equals(proposedCategory)) // if they are the same,
                                                                                               // remove
                {
                    if (menuCreator.isShowingFoodItems()
                            && menuCreator.getSelectedCategory().equals(proposedCategory)) {
                        menuCreator.detoggleOpenFoodButton();
                        menuCreator.closeFoodItems();
                        menuCreator.closeModifiers();
                    }
                    menu.getCategories().remove(menu.getCategories().get(indexWithinMenu));
                    categoriesTableModel.removeRow(indexWithinMenu);
                    System.out.println("Removed Category");
                } else // if a different category for the same position, then it means there is a
                       // change
                {
                    menu.getCategories().set(indexWithinMenu, proposedCategory);
                    categoriesTableModel.setValueAt(proposedCategory, indexWithinMenu, 0);
                    System.out.println("Overwrote Category");
                }
                menu.saveMenu();
            }
        } else if (proposed instanceof FoodItem) {
            FoodItem proposedFoodItem = (FoodItem) proposed;
            System.out.println("Food item armed");

            int categoryID = proposedFoodItem.getCategoryID();
            int foodItemID = proposedFoodItem.getID();
            DefaultTableModel foodTableModel = menuCreator.getFoodTableModel();
            int categoryIndexWithinMenu = menu.searchByCategoryID(categoryID);
            Category categoryWithinMenu = menu.getCategories().get(categoryIndexWithinMenu);
            int foodItemIndexWithinCategory = categoryWithinMenu.searchByFoodItemID(foodItemID);
            // food items are shown and they belong to the same category as the proposed
            // food item, therefore should be updated
            boolean shouldUpdateFoodTable = menuCreator.isShowingFoodItems()
                    && menuCreator.getSelectedCategory().equals(categoryWithinMenu);

            if (foodItemIndexWithinCategory == -1) // not in category, therefore new food item
            {
                categoryWithinMenu.addFoodItem(proposedFoodItem);
                if (shouldUpdateFoodTable) {
                    Object[] foodItemRow = { proposedFoodItem, proposedFoodItem.getPrice(), proposedFoodItem.getID() };
                    foodTableModel.addRow(foodItemRow);
                }
                System.out.println("Added Food Item");
            } else if (categoryWithinMenu.getFoodItems().get(foodItemIndexWithinCategory).equals(proposedFoodItem)) 
            // if they are the same, remove
            {
                categoryWithinMenu.getFoodItems()
                        .remove(categoryWithinMenu.getFoodItems().get(foodItemIndexWithinCategory));
                if (menuCreator.isShowingModifiers() && menuCreator.getSelectedFoodItem().equals(proposedFoodItem)) {
                    menuCreator.detoggleOpenModifiersButton();
                    menuCreator.closeModifiers();
                }
                if (shouldUpdateFoodTable) {
                    foodTableModel.removeRow(foodItemIndexWithinCategory);
                }
                System.out.println("Removed Food Item");
            } else // if a different food item for the same position, then it means there is a change
            {
                categoryWithinMenu.getFoodItems().set(foodItemIndexWithinCategory, proposedFoodItem);
                if (shouldUpdateFoodTable) {
                    foodTableModel.setValueAt(proposedFoodItem, foodItemIndexWithinCategory, 0);
                    foodTableModel.setValueAt(proposedFoodItem.getPrice(), foodItemIndexWithinCategory, 1);
                    foodTableModel.setValueAt(proposedFoodItem.getID(), foodItemIndexWithinCategory, 2);
                }
                System.out.println("Overwrote Food Item");
            }
            menu.saveMenu();
        } else if (proposed instanceof Modifier) {
            Modifier proposedModifier = (Modifier) proposed;
            System.out.println("Modifier armed");

            int categoryID = proposedModifier.getCategoryID();
            int foodItemID = proposedModifier.getFoodItemID();
            int modifierID = proposedModifier.getID();
            DefaultTableModel modifiersTableModel = menuCreator.getModifiersTableModel();

            int categoryIndexWithinMenu = menu.searchByCategoryID(categoryID);
            Category categoryWithinMenu = menu.getCategories().get(categoryIndexWithinMenu);

            int foodItemIndexWithinCategory = categoryWithinMenu.searchByFoodItemID(foodItemID);
            FoodItem foodItemWithinCategory = categoryWithinMenu.getFoodItems().get(foodItemIndexWithinCategory);

            int modifierIndexWithinFoodItem = foodItemWithinCategory.searchByModifierID(modifierID);

            // modifiers are shown and they belong to the same food item as the proposed
            // modifier, therefore should be updated
            boolean shouldUpdateModifiersTable = menuCreator.isShowingModifiers()
                    && menuCreator.getSelectedFoodItem().equals(foodItemWithinCategory);

            if (modifierIndexWithinFoodItem == -1) // not in food item, therefore new modifier
            {
                foodItemWithinCategory.addModifier(proposedModifier);
                if (shouldUpdateModifiersTable) {
                    Object[] modifierRow = { proposedModifier, proposedModifier.getAddCost(),
                            proposedModifier.getID() };
                    modifiersTableModel.addRow(modifierRow);
                }
                System.out.println("Added Modifier");
            } else if (foodItemWithinCategory.getModifiers().get(modifierIndexWithinFoodItem).equals(proposedModifier)) // if
                                                                                                                        // they
                                                                                                                        // are
                                                                                                                        // the
                                                                                                                        // same,
                                                                                                                        // remove
            {
                foodItemWithinCategory.getModifiers()
                        .remove(foodItemWithinCategory.getModifiers().get(modifierIndexWithinFoodItem));
                if (shouldUpdateModifiersTable) {
                    modifiersTableModel.removeRow(modifierIndexWithinFoodItem);
                }
                System.out.println("Removed Modifier");
            } else // if a different food item for the same position, then it means there is a
                   // change
            {
                foodItemWithinCategory.getModifiers().set(modifierIndexWithinFoodItem, proposedModifier);
                if (shouldUpdateModifiersTable) {
                    modifiersTableModel.setValueAt(proposedModifier, modifierIndexWithinFoodItem, 0);
                    modifiersTableModel.setValueAt(proposedModifier.getAddCost(), modifierIndexWithinFoodItem, 1);
                    modifiersTableModel.setValueAt(proposedModifier.getID(), modifierIndexWithinFoodItem, 2);
                }
                System.out.println("Overwrote Modifier");
            }
            menu.saveMenu();
        } else if (proposed instanceof Order) {
            Order proposedOrder = (Order) proposed;
            System.out.println("Order armed");

            int orderIndexWithinOrdersList = POSInstance.searchByOrderIDInOrdersList(proposedOrder.getID());
            int orderIndexWithinOrdersTableModel = POSInstance.searchByOrderIDInOrdersTableModel(proposedOrder.getID()); // unnecessary
            ArrayList<Order> orderList = POSInstance.getOrders();

            if (orderIndexWithinOrdersList == -1) // not in ordersList, therefore new order
            {
                POSInstance.getGUI().getRetailView().saveOrderWithoutSending(proposedOrder);
                System.out.println("Added Order");
            } else if (!orderList.get(orderIndexWithinOrdersList).equals(proposedOrder)) // update the order
            {
                orderList.set(orderIndexWithinOrdersList, proposedOrder);
                Object[] orderRow = OrdersPreparer.prepareOrder(proposedOrder);

                for (int i = 0; i < orderRow.length; i++) {
                    Object colObj = orderRow[i];
                    POSInstance.getOrdersTableModel().setValueAt(colObj, orderIndexWithinOrdersTableModel, i);
                }
                POSInstance.getGUI().getKitchenView().updateTable();
                POSInstance.getGUI().getMgmtView().updateTable();
                POSInstance.saveOrders();
                System.out.println("Modified Order");
            }
        } else {
            System.out.println("Unreadable Data");
        }
    }

    public static void updateOrdersDataStringToSend() throws IOException {
        try {
            ordersData = Files.readString(Paths.get(POSRunner.ORDERS_FILENAME));
        } catch (NoSuchFileException e) {
            System.out.println(POSRunner.ORDERS_FILENAME + "could not be found. Creating a new order save file.");
            File newOrdersFile = new File(POSRunner.ORDERS_FILENAME);
            if (newOrdersFile.createNewFile()) {
                System.out.println(newOrdersFile + " creation successful.");
            } else {
                System.out.println("File already exists.");
            }
        }
    }

    public static void updateMenuDataStringToSend() throws IOException {
        try {
            menuData = Files.readString(Paths.get(POSRunner.MENU_FILENAME));
        } catch (NoSuchFileException e) {
            System.out.println(POSRunner.MENU_FILENAME + "could not be found. Creating a new menu save file.");
            File newOrdersFile = new File(POSRunner.MENU_FILENAME);
            if (newOrdersFile.createNewFile()) {
                System.out.println(newOrdersFile + " creation successful.");
            } else {
                System.out.println("File already exists.");
            }
        }
    }
}
