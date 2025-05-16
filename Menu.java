
/** 
 * Menu.java
 * Embodies the menu
 * Composed of a multi-dimensional ArrayList structure
 * Loads from the menu save file
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
import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

public class Menu {
    private ArrayList<Category> categoriesList; // a list of food categories in menu
    private ArrayList<StockableComponent> inventoryItems; // a list of all stockable components
    private double taxRate; // Configurable tax rate
    // menuSaveStr is no longer a field, it's processed in constructor
    private Socket socket; // network socket to send menu data across
    private boolean belongsToServer; // if it does, save data to Server device's files

    private static final double DEFAULT_TAX_RATE = 0.0825;

    // Wrapper class for GSON serialization/deserialization
    private static class MenuData {
        ArrayList<Category> categoriesList;
        ArrayList<StockableComponent> inventoryItems;
        Double taxRate; // Use Double object to distinguish missing from 0.0

        // Constructor for default initialization if needed
        public MenuData() {
            this.categoriesList = new ArrayList<>();
            this.inventoryItems = new ArrayList<>();
            // taxRate will be null by default if not set
        }
    }

    public Menu(String menuSaveStr, Socket socket, boolean belongsToServer) throws IOException {
        this.socket = socket;
        this.belongsToServer = belongsToServer;
        this.taxRate = DEFAULT_TAX_RATE; // Initialize with default
        this.categoriesList = new ArrayList<>(); // Initialize with empty list
        this.inventoryItems = new ArrayList<>(); // Initialize with empty list

        if (menuSaveStr != null && !menuSaveStr.trim().isEmpty()) {
            try {
                GsonBuilder gsonBuilder = new GsonBuilder();
                gsonBuilder.setPrettyPrinting();
                gsonBuilder.registerTypeAdapter(Category.class, new CategoryDeserializer());
                gsonBuilder.registerTypeAdapter(FoodItem.class, new FoodItemDeserializer());
                gsonBuilder.registerTypeAdapter(Modifier.class, new ModifierDeserializer());
                Gson gson = gsonBuilder.create();
                
                MenuData loadedData = null;
                boolean parsedAsMenuDataCorrectly = false;

                // Attempt to parse as new MenuData format first
                // A simple heuristic: if it doesn't start with '[', it's likely not the old array format.
                if (!menuSaveStr.trim().startsWith("[")) {
                    try {
                        loadedData = gson.fromJson(menuSaveStr, MenuData.class);
                        if (loadedData != null) { // Check if fromJson returned a non-null object
                           parsedAsMenuDataCorrectly = true;
                        }
                    } catch (JsonSyntaxException e) {
                        // It wasn't MenuData, or was malformed MenuData. Will try old format or fail.
                        System.out.println("Could not parse as MenuData object, will try old array format or fail. Error: " + e.getMessage());
                    }
                }

                if (parsedAsMenuDataCorrectly) {
                    if (loadedData.categoriesList != null) {
                        this.categoriesList = loadedData.categoriesList;
                    }
                    if (loadedData.inventoryItems != null) {
                        this.inventoryItems = loadedData.inventoryItems;
                    }
                    if (loadedData.taxRate != null) { // taxRate is Double, so check for null
                        this.taxRate = loadedData.taxRate; // Assign if present in JSON
                    } else {
                        // taxRate was not in the JSON, so this.taxRate keeps its DEFAULT_TAX_RATE
                        System.out.println("TaxRate field missing in MenuData from JSON, using default: " + this.taxRate);
                    }
                    System.out.println("Loaded menu using MenuData wrapper. Categories: " + (this.categoriesList != null ? this.categoriesList.size() : 0) + ", Inventory Items: " + (this.inventoryItems != null ? this.inventoryItems.size() : 0) + ", Tax rate: " + this.taxRate);
                } else if (menuSaveStr.trim().startsWith("[")) { // Try parsing as old format (ArrayList<Category>)
                    System.out.println("Attempting to parse as old menu format (ArrayList<Category>)...");
                    Type oldListType = new TypeToken<ArrayList<Category>>() {}.getType();
                    this.categoriesList = gson.fromJson(menuSaveStr, oldListType);
                    // For old format, inventoryItems remains empty and taxRate is default
                    this.inventoryItems = new ArrayList<>(); 
                    this.taxRate = DEFAULT_TAX_RATE; 
                    System.out.println("Loaded old menu format. Categories: " + (this.categoriesList != null ? this.categoriesList.size() : 0) + ". Inventory Items: 0. Using default tax rate: " + this.taxRate);
                } else { 
                    // Neither new MenuData format nor old array format, or fromJson for MenuData returned null.
                    throw new JsonSyntaxException("Menu file content is not a recognized format (old JSON array or new MenuData object).");
                }
            } catch (JsonSyntaxException e) {
                System.out.println(POSRunner.MENU_FILENAME + " has invalid JSON data. Error: " + e.getMessage());
                // Move corrupted file logic
                Path oldPath = Paths.get(POSRunner.MENU_FILENAME);
                Path newPath = Paths.get(POSRunner.MENU_FILENAME + ".corrupted");
                try {
                    int num = 1;
                    while (newPath.toFile().exists()) {
                        String newFileName = POSRunner.MENU_FILENAME + ".corrupted" + num;
                        newPath = Paths.get(newFileName);
                        num++;
                    }
                    Files.move(oldPath, newPath, StandardCopyOption.REPLACE_EXISTING);
                    System.out.println("Old menu save file renamed successfully to " + newPath);
                } catch (IOException io) {
                    System.out.println("File could not be renamed: " + io.getMessage());
                }
                Server.updateMenuDataStringToSend(); 
            } catch (Exception e) { // Catch other potential exceptions during loading
                System.out.println("Error processing menu file " + POSRunner.MENU_FILENAME + ": " + e.getMessage());
            }
        }
        // Ensure categoriesList is never null after constructor
        if (this.categoriesList == null) {
            this.categoriesList = new ArrayList<Category>();
        }
        if (this.inventoryItems == null) {
            this.inventoryItems = new ArrayList<StockableComponent>();
        }
        // taxRate is already defaulted or loaded by this point.
    }
    
    // Helper to check if taxRate was in the JSON (conceptual, actual implementation might vary)
    // This helper is no longer strictly needed with the Double wrapper approach for loading.
    // private boolean isTaxRateExplicitlySetInJson(String jsonStr) {
    // if (jsonStr == null || jsonStr.trim().isEmpty() || jsonStr.trim().startsWith("[")) {
    // return false;
    // }
    // return jsonStr.contains("\"taxRate\":");
    // }


    public ArrayList<Category> getCategories() {
        return categoriesList;
    }

    public double getTaxRate() {
        // The loading logic now ensures taxRate is either the loaded value or the default.
        return this.taxRate;
    }

    public void setTaxRate(double taxRate) {
        if (taxRate >= 0 && taxRate <= 1) { // Basic validation
            this.taxRate = taxRate;
        } else {
            System.err.println("Invalid tax rate provided: " + taxRate + ". Must be between 0.0 and 1.0.");
        }
    }

    public String toString() {
        String outputStr = "Tax Rate: " + taxRate + "\n";
        for (Category c : categoriesList) {
            outputStr += c.toString() + "\n";
        }
        return outputStr;
    }

    public void addCategory(Category category) {
        if (categoriesList == null) {
            categoriesList = new ArrayList<>();
        }
        categoriesList.add(category);
    }

    /**
     * Searches for a category's index by its ID within categoriesList
     * Uses binary search algorithm to efficiently find the index.
     *
     * @param categoryID The ID of the category to search for.
     * @return The index of the category if found, otherwise -1.
     */
    public int searchByCategoryID(int categoryID) {
        int leftBound = 0;
        int rightBound = categoriesList.size() - 1;
        int middleIndex = (leftBound + rightBound) / 2;
        while (leftBound <= rightBound) {
            if (categoriesList.get(middleIndex).getID() == (categoryID)) { // element at middle index is equal to search
                                                                           // value
                return middleIndex;
            } else if (categoriesList.get(middleIndex).getID() < (categoryID)) { // element at middle index is before
                                                                                 // search value
                leftBound = middleIndex + 1;
                middleIndex = (leftBound + rightBound) / 2;
            } else if (categoriesList.get(middleIndex).getID() > (categoryID)) { // element at middle index is after
                                                                                 // search value
                rightBound = middleIndex - 1;
                middleIndex = (leftBound + rightBound) / 2;
            }
        }
        return -1;
    }

    public void saveMenu() {
        if (belongsToServer) {
            try {
                System.out.println("Saving Menu with Tax Rate: " + this.taxRate);
                GsonBuilder gsonBuilder = new GsonBuilder();
                gsonBuilder.setPrettyPrinting();
                // No need to register adapters for MenuData itself if its fields use standard types or already registered adapters
                Gson gson = gsonBuilder.create();
                
                MenuData dataToSave = new MenuData();
                dataToSave.categoriesList = this.categoriesList;
                dataToSave.inventoryItems = this.inventoryItems;
                dataToSave.taxRate = this.taxRate;
                
                String saveStr = gson.toJson(dataToSave);
                FileWriter wr = new FileWriter(POSRunner.MENU_FILENAME); // Use constant from POSRunner
                wr.write(saveStr);
                wr.close();
                System.out.println("Menu saved successfully to " + POSRunner.MENU_FILENAME);
            } catch (Exception e) {
                System.out.println("Error saving menu to " + POSRunner.MENU_FILENAME + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public void sendData(Sendable sentObj) {
        if (socket != null) {
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.setPrettyPrinting();
            Gson gson = gsonBuilder.create();
            String sendStr = gson.toJson(sentObj);
            // sends output to the socket
            try {
                DataOutputStream output = new DataOutputStream(socket.getOutputStream());
                output.writeUTF(sendStr);
            } catch (IOException i) {
                System.out.println(i);
            }
        }
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    // Methods for managing StockableComponents
    public ArrayList<StockableComponent> getAllStockableComponents() {
        if (this.inventoryItems == null) {
            this.inventoryItems = new ArrayList<>(); // Ensure it's initialized
        }
        return this.inventoryItems;
    }

    public StockableComponent getStockableComponentById(String id) {
        if (this.inventoryItems == null) return null;
        for (StockableComponent component : this.inventoryItems) {
            if (component.getId().equals(id)) {
                return component;
            }
        }
        return null;
    }

    public void addStockableComponent(StockableComponent component) {
        if (this.inventoryItems == null) {
            this.inventoryItems = new ArrayList<>();
        }
        if (component != null && getStockableComponentById(component.getId()) == null) {
            this.inventoryItems.add(component);
        } else if (component != null) {
            System.out.println("StockableComponent with ID " + component.getId() + " already exists. Not adding.");
        }
    }

    public void updateStockableComponent(StockableComponent componentToUpdate) {
        if (this.inventoryItems == null || componentToUpdate == null) return;
        for (int i = 0; i < this.inventoryItems.size(); i++) {
            if (this.inventoryItems.get(i).getId().equals(componentToUpdate.getId())) {
                this.inventoryItems.set(i, componentToUpdate);
                return;
            }
        }
        System.out.println("StockableComponent with ID " + componentToUpdate.getId() + " not found for update.");
    }

    public void deleteStockableComponent(String id) {
        if (this.inventoryItems == null || id == null) return;
        this.inventoryItems.removeIf(component -> component.getId().equals(id));
    }
}
