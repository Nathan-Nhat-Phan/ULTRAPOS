public class RecipeItem {
    private String componentId; // ID of a StockableComponent
    private int quantityPerItem;  // How many units of the component are needed for one FoodItem

    public RecipeItem(String componentId, int quantityPerItem) {
        this.componentId = componentId;
        this.quantityPerItem = quantityPerItem;
    }

    public String getComponentId() {
        return componentId;
    }

    public void setComponentId(String componentId) {
        this.componentId = componentId;
    }

    public int getQuantityPerItem() {
        return quantityPerItem;
    }

    public void setQuantityPerItem(int quantityPerItem) {
        if (quantityPerItem < 0) {
            this.quantityPerItem = 0; // Prevent negative quantity
        } else {
            this.quantityPerItem = quantityPerItem;
        }
    }

    @Override
    public String toString() {
        return "RecipeItem{" +
               "componentId='" + componentId + '\'' +
               ", quantityPerItem=" + quantityPerItem +
               '}';
    }

    // Consider adding equals and hashCode if RecipeItems will be stored in sets or used as map keys directly.
    // For now, basic getters and setters are sufficient for list storage.
}
