import java.util.UUID;

public class StockableComponent {
    private String id;
    private String name;
    private int stockQuantity;
    private boolean trackStock;

    // Constructor for creating a new component with a generated ID
    public StockableComponent(String name, int initialStockQuantity, boolean trackStock) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.stockQuantity = initialStockQuantity;
        this.trackStock = trackStock;
    }

    // Constructor for loading from persistence (or when ID is already known)
    public StockableComponent(String id, String name, int stockQuantity, boolean trackStock) {
        this.id = id;
        this.name = name;
        this.stockQuantity = stockQuantity;
        this.trackStock = trackStock;
    }

    public String getId() {
        return id;
    }

    // No setter for ID as it should be immutable after creation or loading

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        if (stockQuantity < 0) {
            this.stockQuantity = 0; // Prevent negative stock
        } else {
            this.stockQuantity = stockQuantity;
        }
    }

    public boolean isTrackStock() {
        return trackStock;
    }

    public void setTrackStock(boolean trackStock) {
        this.trackStock = trackStock;
    }

    public void decrementStock(int amount) {
        if (trackStock && amount > 0) {
            this.stockQuantity -= amount;
            if (this.stockQuantity < 0) {
                this.stockQuantity = 0; // Ensure stock doesn't go below zero
            }
        }
    }

    public void incrementStock(int amount) {
        if (trackStock && amount > 0) {
            this.stockQuantity += amount;
        }
    }

    @Override
    public String toString() {
        return name + " (Stock: " + (trackStock ? stockQuantity : "Not Tracked") + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StockableComponent that = (StockableComponent) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
